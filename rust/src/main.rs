extern crate env_logger;
extern crate bytes;
extern crate futures;
extern crate tokio;
extern crate mdp;
extern crate rave;
extern crate clap;

use mdp::protocol::{Protocol, PORT_LINKSTATE};
use mdp::overlay::udp::Interface;
use mdp::addr::LocalAddr;
use mdp::services::Routing;
use tokio::net::UdpSocket;
use std::time::Duration;
use futures::Future;
use rave::{RAVE_PORT, RaveService};
use clap::{App, Arg};

fn main() {
    let matches = App::new("RAVE prototype relay")
        .version("0.1.0")
        .author("Josh King <jking@chambana.net>")
        .about("Relays RAVE messages between mobile devices and across LoRA interfaces.")
        .arg(Arg::with_name("ip")
             .short("a")
             .long("address")
             .help("Specify an address:port combination to bind to.")
             .value_name("IP")
             .takes_value(true)
             .required(true))
        .get_matches();
    let ip = matches.value_of("ip").unwrap();
    drop(env_logger::init());

    let udp = UdpSocket::bind(&ip.parse().unwrap()).unwrap();
    udp.set_broadcast(true).unwrap();

    let addr = LocalAddr::new();

    let interface = Interface::new(udp).unwrap();

    let mut protocol = Protocol::new(&addr);

    protocol.interface(interface);

    let routing = Routing::from(protocol.bind(&addr, PORT_LINKSTATE).unwrap());
    let rave = RaveService::from(protocol.bind(&addr, RAVE_PORT).unwrap());

    println!("Starting RAVE.");
    tokio::run(protocol.run(Duration::new(1, 0)).then(|_| {
        tokio::spawn(routing);
        tokio::spawn(rave);
        Ok(())
    }));
}
