package com.moredian.rtcengine;

import com.moredian.bean.UserInfoBean;

public interface RTCEngineEventListener {
	public abstract void onRegister(int status);
	public abstract void onUnregister(int status);
	public abstract void onCallStart(int status);
	public abstract void onCallStop(int status);
	public abstract void onIncomingCall(UserInfoBean userInfoBean);
	public abstract void onNotifyVideoStreamRenderID(int id);
	public abstract void onServiceDown(int status);
	public abstract void onRemotePeerDrop();
	public abstract void onRecvMsg(String msgData);
//	public abstract void onNetworkRecover();
}
