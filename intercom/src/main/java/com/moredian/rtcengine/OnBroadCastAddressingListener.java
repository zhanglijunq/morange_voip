package com.moredian.rtcengine;

public interface OnBroadCastAddressingListener {
	public void onRemoteAddressReceived(String strLocalIP, String strRemoteIP, String remoteNumber);
//	public void onRemoteUdpCmd_RegisterByP2P(String strLocalIP, String strRemoteIP, boolean bNeedP2P);
}
