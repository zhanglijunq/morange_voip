package com.moredian.rtcengine;

import java.net.DatagramPacket;

public interface OnUDPPacketReceivedListener {
	public void onUDPPacketReceived(DatagramPacket packet);
}
