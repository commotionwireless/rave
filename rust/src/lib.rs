#[macro_use]
extern crate futures;
extern crate mdp;
#[macro_use]
extern crate nom;
#[macro_use]
extern crate cookie_factory;
extern crate bytes;
#[macro_use]
extern crate log;

use std::cell::Cell;
use std::cmp::{PartialOrd, Ord, Ordering};
use std::time::Instant;
use std::string::String;
use std::{error, fmt, result};
use futures::prelude::*;
use futures::future;
use mdp::addr::{ADDR_BROADCAST, Addr, SocketAddr};
use mdp::socket::{Framed, Decoder, Encoder, Socket, State};
use mdp::error::Error as MdpError;
use nom::{be_u8, be_u16};
use cookie_factory::GenError;
use bytes::BytesMut;

const RAVE_VERSION: [u8; 1] = [1];
pub const RAVE_PORT: u32 = 5555;
const INITIAL_USERS: usize = 32;
const INITIAL_MSGS: usize = INITIAL_USERS * 10;
const EVERYONE: usize = 0;
const ME: usize = 1;


#[derive(Debug)]
pub enum Error {
    ParseError(nom::ErrorKind),
    ParseIncomplete(nom::Needed),
    EncodeError(GenError),
    Mdp(MdpError),
    NoSuchUser,
}

impl fmt::Display for Error {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match *self {
            Error::ParseError(ref err) => fmt::Debug::fmt(err, f),
            Error::EncodeError(ref err) => fmt::Debug::fmt(err, f),
            Error::ParseIncomplete(i) => match i {
                nom::Needed::Unknown => {
                    write!(f, "Missing unknown amount of bytes while deserializing.")
                }
                nom::Needed::Size(s) => {
                    write!(f, "Missing {:?} bytes of data while deserializing.", s)
                }
            },
            Error::Mdp(ref err) => fmt::Debug::fmt(err,f),
            Error::NoSuchUser => write!(f, "No such user exists."),
        }
    }
}

impl error::Error for Error {
    fn description(&self) -> &str {
        match *self {
            Error::ParseError(ref err) => err.description(),
            Error::EncodeError(_) => "Error while serializing to buffer.",
            Error::ParseIncomplete(_) => "Missing bytes while deserializing.",
            Error::Mdp(ref err) => err.description(),
            Error::NoSuchUser => "No such user exists.",
        }
    }

    fn cause(&self) -> Option<&error::Error> {
        match *self {
            Error::ParseError(_) => None,
            Error::EncodeError(_) => None,
            Error::ParseIncomplete(_) => None,
            Error::Mdp(ref err) => Some(err),
            Error::NoSuchUser => None,
        }
    }
}

impl<'a> From<nom::Err<&'a [u8]>> for Error {
    fn from(err: nom::Err<&'a [u8]>) -> Error {
        match err {
            nom::Err::Incomplete(needed) => Error::ParseIncomplete(needed),
            nom::Err::Error(e) | nom::Err::Failure(e) => Error::ParseError(e.into_error_kind())
        }
    }
}

impl From<GenError> for Error {
    fn from(err: GenError) -> Error {
        Error::EncodeError(err)
    }
}

impl From<MdpError> for Error {
    fn from(err: MdpError) -> Error {
        Error::Mdp(err)
    }
}

impl From<Error> for () {
    fn from(_: Error) -> () {}
}

pub type Result<T> = result::Result<T, Error>;

// Alias to provide an analogue to IResult, except for the encoding pipeline.
pub type GResult<T> = result::Result<T, GenError>;

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct Hello {
    nick: String
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct Nack {
    seq: u8
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct Message {
    seq: u8,
    from: usize,
    channel: usize,
    received: Instant,
    body: String
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub enum Envelope {
    Hello(Hello),
    Nack(Nack),
    Goodbye,
    Message(Message)
}

impl PartialOrd for Message {
    fn partial_cmp(&self, other: &Message) -> Option<Ordering> {
        self.received.partial_cmp(&other.received)
    }
}

impl Ord for Message {
    fn cmp(&self, other: &Message) -> Ordering {
        self.received.cmp(&other.received)
    }
}

#[derive(Debug, Clone, Eq, PartialEq)]
pub struct User {
    last_seq: Cell<u8>,
    src: Addr,
    last_seen: Instant,
    nick: String,
}

named!(decode_hello<Envelope>,
    do_parse!(
        nick: length_bytes!(be_u8) >>
        (Envelope::Hello(Hello { nick: String::from_utf8_lossy(nick).to_string() }))
    )
);
        
named!(decode_nack<Envelope>,
    do_parse!(
        seq: be_u8 >>
        (Envelope::Nack(Nack { seq: seq }))
    )
);

named!(decode_goodbye<Envelope>,
    value!(Envelope::Goodbye)
);

named!(decode_message<Envelope>,
    do_parse!(
        seq: be_u8 >>
        body: length_bytes!(be_u16) >>
        (Envelope::Message(Message { seq: seq, from: 0, channel: 0, received: Instant::now(), body: String::from_utf8_lossy(body).to_string() }))
    )
);

named!(decode_envelope<Envelope>,
    do_parse!(
        _version: tag!(RAVE_VERSION) >>
        envelope: switch!(be_u8,
            0 => call!(decode_hello) |
            1 => call!(decode_nack) |
            2 => call!(decode_goodbye) |
            3 => call!(decode_message)
        ) >>
        ( envelope )
    )
);

fn encode_envelope<'b>(buf: (&'b mut [u8], usize), envelope: Envelope) -> GResult<(&'b mut [u8], usize)> {
    match envelope {
        Envelope::Hello(hello) => {
            do_gen!(
                buf,
                gen_be_u8!(RAVE_VERSION[0]) >>
                gen_be_u8!(0) >>
                gen_be_u8!(hello.nick.len() as u8) >>
                gen_slice!(hello.nick.as_bytes())
            )
        },
        Envelope::Nack(nack) => {
            do_gen!(
                buf,
                gen_be_u8!(nack.seq)
            )
        },
        Envelope::Goodbye => Ok(buf),
        Envelope::Message(msg) => {
            do_gen!(
                buf,
                gen_be_u8!(msg.seq) >>
                gen_be_u16!(msg.body.len()) >>
                gen_slice!(msg.body.as_bytes())
            )
        }
    }
}

struct RaveCodec;

impl Default for RaveCodec {
    fn default() -> RaveCodec { RaveCodec }
}

impl Decoder for RaveCodec {
    type Item = Envelope;
    type Error = Error;

    fn decode(&mut self, buf: &mut BytesMut) -> Result<Option<Self::Item>> {
        if let Ok((_, msg)) = decode_envelope(buf).map_err(|err| Error::ParseError(err.into_error_kind())) {
            Ok(Some(msg))
        } else {
            Ok(None)
        }
    }
}

impl Encoder for RaveCodec {
    type Item = Envelope;
    type Error = Error;

    fn encode(&mut self, item: Self::Item, buf: &mut BytesMut) -> Result<()> {
        match encode_envelope((buf, 0), item).map(|b| b.1) {
            Ok(end) => {
                debug!("Encoded message of size {}.", end);
                Ok(())
            },
            Err(err) => Err(Error::EncodeError(err))
        }
    }
}

pub struct RaveService {
    inner: Framed<RaveCodec>,
    users: Vec<User>,
    messages: Vec<Message>,
}

impl From<Socket> for RaveService {
    fn from(mut s: Socket) -> RaveService {
        s.set_broadcast(true);
        let codec = RaveCodec::default();
        RaveService {
            inner: Framed::new(s, codec),
            users: Vec::with_capacity(INITIAL_USERS),
            messages: Vec::with_capacity(INITIAL_MSGS)
        }
    }
}

impl RaveService {
    pub fn send_msg(&mut self, to: usize, body: &str) -> Result<AsyncSink<(Envelope, SocketAddr, State)>> {
        let user = self.users.get(to).ok_or_else(|| Error::NoSuchUser)?;
        let last_seq =  self.users[ME].last_seq.get();
        let msg = Message {
            seq: last_seq,
            from: ME,
            channel: to,
            received: Instant::now(),
            body: body.to_owned()
        };
        self.users[ME].last_seq.set(last_seq + 1);
        let state = if to == EVERYONE {
            State::Plain
        } else {
            State::Encrypted
        };
        self.messages.push(msg.clone());
        self.inner.start_send((Envelope::Message(msg), (user.src, RAVE_PORT).into(), state))
    }

    pub fn get_user_by_id(&self, id: usize) -> Option<&User> {
        self.users.get(id)
    }

    pub fn get_user_by_nick(&self, nick: &str) -> Option<&User> {
        self.users.iter().find(|ref u| u.nick == nick)
    }

    pub fn get_messages(&mut self, channel: usize) -> Vec<&Message> {
        self.messages.sort();
        self.messages.iter().filter(|ref msg| msg.channel == channel).collect()
    }
}

impl Future for RaveService {
    type Item = ();
    type Error = ();

    fn poll(&mut self) -> Poll<Self::Item, Self::Error> {
        if let Some((envelope, src, state, dst_bcast)) = try_ready!(self.inner.poll()) {
            match envelope {
                Envelope::Hello(ref hello) if state == State::Signed => {
                    if let Some(user) = self.users.iter_mut().find(|ref u| u.src == *src.addr()) {
                        user.last_seen = Instant::now();
                        return future::empty().poll();
                    }
                    self.users.push(User { last_seq: Cell::new(0), src: *src.addr(), last_seen: Instant::now(), nick: hello.nick.to_owned() });
                },
                Envelope::Nack(ref nack) if state == State::Encrypted => {
                    if let Some(msg) = self.messages.iter().find(|ref msg| msg.seq == nack.seq && msg.from == ME) {
                        if msg.channel == EVERYONE {
                            let _ = self.inner.start_send((Envelope::Message(msg.clone()), (ADDR_BROADCAST, RAVE_PORT).into(), State::Plain));
                        } else if let Some(ref user) = self.users.get(msg.channel) {
                            if user.src == *src.addr() {
                                let _ = self.inner.start_send((Envelope::Message(msg.clone()), src, State::Plain));
                            }
                        }
                    }
                },
                Envelope::Goodbye if state == State::Signed => {
                    self.users.retain(|u| u.src != *src.addr());
                },
                Envelope::Message(msg) => {
                    if let Some(id) = self.users.iter().position(|ref u| u.src == *src.addr()) {
                        let mut new = msg.clone();
                        new.from = id;
                        new.channel = if dst_bcast {
                            EVERYONE
                        } else {
                            id
                        };
                        self.messages.push(new);
                        if let Some(user) = self.users.get_mut(id) {
                            if msg.seq > user.last_seq.get() && msg.seq - user.last_seq.get() > 1 {
                                let _ = self.inner.start_send((Envelope::Nack(Nack { seq: user.last_seq.get() }), src, State::Encrypted));
                            }
                            user.last_seq.set(msg.seq);
                            user.last_seen = Instant::now();
                        }
                    }
                },
                _ => return future::empty().poll()
            }
        }
        future::empty().poll()
    }
}
