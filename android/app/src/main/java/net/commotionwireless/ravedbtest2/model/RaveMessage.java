package net.commotionwireless.ravedbtest2.model;

import java.util.Date;

public interface RaveMessage {
    int getId();
    int getNodeAddress();
    String getContents();
    Date getTimestamp();
}
