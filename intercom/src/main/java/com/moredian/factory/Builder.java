package com.moredian.factory;

public abstract class Builder {
    public abstract void bindUmeng(String umengAppKey,String umengSecret,String umengChannel);
    public abstract void bindXiaomi(String xiaomiId,String xiaomiKey);
    public abstract void bindHw(String hwId,String hwSecret);
    public abstract PushCollocation create();
}