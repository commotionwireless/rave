package net.commotionwireless.ravedbtest2.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import net.commotionwireless.ravedbtest2.model.RaveMessage;

import java.util.Date;

@Entity(tableName = "raveMessages",
        foreignKeys = {
                @ForeignKey(entity = RaveNodeEntity.class,
                        parentColumns = "address",
                        childColumns = "nodeAddress",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index(value = "nodeAddress")
        })
public class RaveMessageEntity implements RaveMessage {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int nodeAddress;
    private String contents;
    private Date timestamp;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(int nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    @Override
    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public RaveMessageEntity() {
    }

    public RaveMessageEntity(int id, int nodeAddress, String contents, Date timestamp) {
        this.id = id;
        this.nodeAddress = nodeAddress;
        this.contents = contents;
        this.timestamp = timestamp;
    }
}
