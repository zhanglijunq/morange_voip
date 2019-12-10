package com.moredian.factory;

public class MorangeDirector {
    private Builder mBuild = null;

    public MorangeDirector(Builder build) {
        this.mBuild = build;
    }

    public void bindUmeng(String umengAppKey,String umengSecret,String umengChannel) {
       mBuild.bindUmeng(umengAppKey,umengSecret,umengChannel);
    }

    public void bindXiaomi(String xiaomiId,String xiaomiKey) {
        mBuild.bindXiaomi(xiaomiId,xiaomiKey);
    }

    public void bindHw(String hwId,String hwSecret) {
        mBuild.bindHw(hwId,hwSecret);
    }
}