package com.moredian.rtcengine;

public interface RTCVideoCaptureListener{
	public abstract void onFrame(byte[] data, int len, int rotation, long timestamp);
}