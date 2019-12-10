package com.moredian.rtcengine;

import android.util.Log;

import com.moredian.bean.UserInfoBean;
import com.moredian.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;

public class NativeRTC {

	public static final int kVideoType_I420 = 0;
	public static final int kVideoType_NV21 = 1;
	public static final int kVideoType_NV12 = 2;
	public static final int kVideoType_YV12 = 3;
	public static final int kVideoRotation_0 = 0;
	public static final int kVideoRotation_90 = 90;
	public static final int kVideoRotation_180 = 180;
	public static final int kVideoRotation_270 = 270;
	public static final String  kUserDefineCallKey = "X-modi-info";
	public static final String  kUserDefineJsonFromKey = "fromName";
	public static final String  kUserDefineJsonVideoKey="VideoFlag";
	public static native void main();
	
	public static native int init(String registarURI, boolean extMod, boolean TLS);
	public static native int uninit();
	public static native int register(String username, String domain, String pswd);
	public static native int unregister();
	public static native int setPreviewRender(long rtcrender);
	public static native int setStreamRender(long rtcrender, int rnd_id);//(Object handle, int rnd_id);
	public static native int setCaptureOrient(int cameraIndex,int orient);
	public static native int startCall(String username, String domain, boolean enable_video, boolean enable_LocalVideo);
	public static native int stopCall();
	public static native int answerCall(boolean enable_LocalVideo);
	public static native int rejectCall();
	public static native int initializeAndroidGlobals(Object Context, boolean enable_audio, boolean enable_video, boolean enable_hw);
	public static native long createRender(Object surfaceview);
	public static native void destroyRender(long render);
	public static native void setEGLContext(Object context);
	public static native int sendMessage(String msgdata);
	public static native int setTurnServer(String turnServ, int turnPort, String turnUser, String turnPwd);
	public static native int setTurnServer2(String turnServ);
	public static native int setPreCallParams(String headerKey, String headerValue);
	public static native int setDefaultVideoResolution(int width, int height);
	public static native int setExtVideoCaptureParams(int width, int height, int fps, int videotype);
	public static native int putExtVideoData(byte[] data, int len, int rotation,long timestamp);
	public static native int setMuteAudio(boolean bMute);
	public static native int setMuteVideo(boolean bMute);
	public static native int startDirectCall(String remoteIP, int remotePort, boolean enable_video);
	public static native int reCreateTransport();

	private static List<RTCEngineEventListener> allListeners =  new ArrayList<RTCEngineEventListener>();


	public static void addEventListerer(RTCEngineEventListener listener){
		if(!allListeners.contains(listener))
			allListeners.add(listener);
	}

	public static synchronized void removeListener(RTCEngineEventListener listener) {
		if(allListeners.contains(listener))
			allListeners.remove(listener);
	}

	public static void onRegister(int status) {
		Log.d("NativeRTC", "onRegister, "+status);
		for (RTCEngineEventListener listener : allListeners)
			listener.onRegister(status);
	}
	
	public static void onUnregister(int status) {
		Log.d("NativeRTC", "onUnregister, "+status);
		for (RTCEngineEventListener listener : allListeners)
			listener.onUnregister(status);
	}
	
	public static void onStartCall(int status) {
		Log.d("NativeRTC", "onStartCall, "+status);
		for (RTCEngineEventListener listener : allListeners)
			listener.onCallStart(status);
	}
	
	public static void onStopCall(int status) {
		Log.d("NativeRTC", "onStopCall, "+status);
		for (RTCEngineEventListener listener : allListeners)
			listener.onCallStop(status);
	}
	
	public static void onIncommingCall(String remoteURI, String wholeHeaderMsg) {
//		Log.d("NativeRTC", "onIncommingCall, "+remoteURI+wholeHeaderMsg);
        String strKeyValue[] = wholeHeaderMsg.split("\r\n");
        String strFind="";
        int nVideoFlag=1;
		UserInfoBean userInfoBean=null;
        for(int i=0;i<strKeyValue.length;i++){
			int nFind = strKeyValue[i].indexOf(kUserDefineCallKey);
            if(nFind != -1) {
				String jsonStr = strKeyValue[i].substring(nFind + kUserDefineCallKey.length() + 1);
				try {
					userInfoBean = GsonUtils.getGson().fromJson(jsonStr,UserInfoBean.class);
//					JSONObject jsonObject = new JSONObject(jsonStr);
//					strFind = jsonObject.getString(kUserDefineJsonFromKey);
//					nVideoFlag = jsonObject.getInt(kUserDefineJsonVideoKey);
				}catch (Exception e) {
					Log.e("NativeRTC", "onIncommingCall, json error:"+jsonStr);
					strFind = jsonStr;
				}
			}
        }
        Log.e("NativeRTC", "onIncommingCall 2 find: "+strFind);
		for (RTCEngineEventListener listener : allListeners)
			listener.onIncomingCall(userInfoBean);
	}
	
	public static void onNotifyVideoStreamRenderID(int id) {
		Log.d("NativeRTC", "onNotifyVideoStreamRenderID, "+id);
		for (RTCEngineEventListener listener : allListeners)
			listener.onNotifyVideoStreamRenderID(id);
	}
	
	public static void onServiceDown(int status) {
		for (RTCEngineEventListener listener : allListeners)
			listener.onServiceDown(status);
	}
	
	public static void onRemotePeerDrop() {
		for (RTCEngineEventListener listener : allListeners)
			listener.onRemotePeerDrop();
	}
	
	public static void onRecvMsg(String msgData){
		Log.d("NativeRTC", "onRecvMsg, "+msgData);
		for (RTCEngineEventListener listener : allListeners)
			listener.onRecvMsg(msgData);
	}
//	public static void onNetworkRecover() {
//		mEvnetListener.onNetworkRecover();
//	}
}
