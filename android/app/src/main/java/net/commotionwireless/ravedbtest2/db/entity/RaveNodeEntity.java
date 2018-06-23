package net.commotionwireless.ravedbtest2.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import net.commotionwireless.ravedbtest2.model.RaveNode;

@Entity(tableName = "raveNodes")
public class RaveNodeEntity implements RaveNode{
    @PrimaryKey
    private int address;
    private String alias;

    @Override
    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public RaveNodeEntity(){}

    public RaveNodeEntity(int address, String alias) {
        this.address = address;
        this.alias = alias;
    }

    public RaveNodeEntity(RaveNode raveNode) {
        this.address = raveNode.getAddress();
        this.alias = raveNode.getAlias();
    }
}
