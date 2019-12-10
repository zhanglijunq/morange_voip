package com.moredian.factory;

public class ConcreteBuilder extends Builder {
    private PushCollocation mCollocation = new PushCollocation();

    @Override
    public void bindUmeng(String umengAppKey, String umengSecret, String umengChannel) {
        mCollocation.setUmengAppKey(umengAppKey);
        mCollocation.setUmengSecret(umengSecret);
        mCollocation.setUmengChannel(umengChannel);
    }

    @Override
    public void bindXiaomi(String xiaomiId, String xiaomiKey) {
        mCollocation.setXiaomiId(xiaomiId);
        mCollocation.setXiaomiKey(xiaomiKey);
    }

    @Override
    public void bindHw(String hwId, String hwSecret) {
        mCollocation.setHwId(hwId);
        mCollocation.setHwSecret(hwSecret);
    }

    @Override
    public PushCollocation create() {
        return mCollocation;
    }
}