package com.moredian.rtcengine;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BroadCast implements OnUDPPacketReceivedListener {
	private static BroadCast uniqInstance;
	private static final String TAG = "BroadCast";
	private DatagramSocket mSendBCSocket;

	private Context mContext;
	final private int SEND_PORT = 43614;
	final private int LISTEN_PORT = 43615;

	private String mstrCallerKey = "";
	private String mstrCalleeKey = "";
	private String mstrCallerIP = "";
	private String mstrCalleeIP = "";
	private String mstrLocalKey = "";
	private String mstrLocalIP = "";
	private String mstrCallerInfo = "";
	private String mstrCalleeInfo = "";
	private String mstrLocalInfo = "";
	
	private InetAddress mSendbackIP;
	private String mDiscoveryData;
	private DatagramPacket mPingpacket;
	private List<OnBroadCastAddressingListener> allListeners;
	private boolean mbNeedRequestAnswer;
	private static final int UDP_TIMEOUT = 300; // set timeout 300ms
	public static ArrayList<Map<String, CalleeInfo>> calleeList = new ArrayList<Map<String, CalleeInfo>>();

	private class CalleeInfo {
		public String mstrKey;
		public String mstrAddress;
		public String mstrInfo;
		public long mlExpireTime; // set expire time 5 sec

		public CalleeInfo(String strKey, String strAddress, String strInfo) {
			mstrKey = strKey;
			mstrAddress = strAddress;
			mstrInfo = strInfo;
			mlExpireTime = System.currentTimeMillis() + 5000;
		}
	}

	private BroadCast(Context context) {
		mContext = context;
		try {
			if (mSendBCSocket == null) {
				mSendBCSocket = new DatagramSocket(null);
				mSendBCSocket.setBroadcast(true);
				mSendBCSocket.setReuseAddress(true);
			}
		} catch (SocketException e) {
			Log.e(TAG, "Create socket fail:" + e.toString());
		}
		UDPListen writer = UDPListen.getInstance();
		UDPListen.addListener(this);
		writer.init(LISTEN_PORT);
		allListeners = new ArrayList<OnBroadCastAddressingListener>();
	}

	public static BroadCast getInstance(Context context) {
		if (uniqInstance == null) {
			uniqInstance = new BroadCast(context);
		}
		return uniqInstance;
	}

	public static void CloseBroadCast() {
		if (uniqInstance != null) {
			uniqInstance.onCloseBroadCast();
			UDPListen.closeStreams();
			uniqInstance = null;
		}
	}

	public void onCloseBroadCast() {
		if (mSendBCSocket != null) {
			mSendBCSocket.close();
			mSendBCSocket = null;
		}
	}

	public void addListener(OnBroadCastAddressingListener listener) {
		if(!allListeners.contains(listener))
			allListeners.add(listener);
	}

	public void SetBroadCastLocalKey(String strLocalKey,String strLocalInfo) { // key是sip 账号， info是户室名
		mstrLocalKey = strLocalKey;
		mstrLocalInfo = strLocalInfo;
		mstrLocalIP = getLocalAddress();
		Log.e(TAG,"SetBroadCastLocalKey===,mstrLocalKey = " + mstrLocalKey + ",   mstrLocalInfo = " + mstrLocalInfo );
	}

	private synchronized CalleeInfo FindInCalleeList(String strCalleeInfo) {
		CalleeInfo info = null;
		for (Map<String, CalleeInfo> map : calleeList) {
			info = map.get(strCalleeInfo);
			if (info != null) {
				break;
			}
		}
		long curTime = System.currentTimeMillis();
		if (info != null && info.mlExpireTime > curTime) {
			mstrCalleeInfo = strCalleeInfo;
			mstrCalleeKey = info.mstrKey;
			mstrCalleeIP = info.mstrAddress;
			return info;
		}
		return null;
	}

	public void SendBroadCastMsg(String strCallerKey, String strCalleeInfo) {
		Log.e(TAG,"SendBroadCastMsg====,strCallerKey = " + strCallerKey +", strCalleeInfo = " + strCalleeInfo);
		if (FindInCalleeList(strCalleeInfo) != null) {
			new Thread(() -> new onAddressingFound()).start();
			return;
		}

		mstrCallerKey = strCallerKey;
		mstrCalleeKey = "";
		mstrCallerInfo = mstrLocalInfo;
		mstrCalleeInfo = strCalleeInfo;
		mbNeedRequestAnswer = true;
		mstrCallerIP = getLocalAddress();
		Log.e(TAG,"mstrCallerIP = " + mstrCallerIP + ",   mstrCallerInfo = " + mstrCallerInfo + ",   mstrCalleeInfo" + mstrCalleeInfo);
		Runnable runnable = () -> new BroadCastSender();
		new Thread(runnable).start();
	}

	@Override
	public void onUDPPacketReceived(DatagramPacket packet) {
		synchronized (this){
			String msg = "";
			try {
				msg = new String(packet.getData(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Log.i(TAG, "Received: " + msg);
			msg = msg.substring(0, msg.indexOf('\u0000'));

			boolean bRet = ParseJSON(msg);
			if (!bRet)
				return;
			InetAddress address = packet.getAddress();
			String strRemoteIP = address.getHostAddress();

			if ((!strRemoteIP.equals(mstrLocalIP))
					&& (mstrCalleeInfo.equals(mstrLocalInfo))) {
				mSendbackIP = packet.getAddress();
				mstrCallerIP = strRemoteIP;
				Runnable runnable = () -> new onUDPDataSendBack();
				new Thread(runnable).start();
				Log.e(TAG,"SendIP:"+mSendbackIP+",callerIP:"+strRemoteIP+",calleeInfo:"+mstrCalleeInfo+",local Info:"+mstrLocalInfo+"mstrCallerKey:"+mstrCallerKey+"mstrLocalKey:"+mstrLocalKey+"mstrLocalIP:"+mstrLocalIP+mbNeedRequestAnswer);
			}

			if ((mstrCallerKey.equals(mstrLocalKey))
					&& (!strRemoteIP.equals(mstrLocalIP)) && mbNeedRequestAnswer) {
				Log.e(TAG, "has addressing already!!!!!!!!!!!!!!!!!!!!!!!,caller key:"+mstrCallerKey+",Local Key:"+mstrLocalKey+
						",remote IP:"+strRemoteIP+",local IP:"+mstrLocalIP+",get local address:"+getLocalAddress());
				mstrCalleeIP = address.getHostAddress();
				RefreshCalleeList();
				Runnable runnable = () -> new onAddressingFound();
				new Thread(runnable).start();
				mbNeedRequestAnswer = false;
			}
		}
	}

	private synchronized void RefreshCalleeList() {
		CalleeInfo info = null;
		for (Map<String, CalleeInfo> map : calleeList) {
			info = map.get(mstrCalleeKey);
			if (info != null) {
				calleeList.remove(map);
				break;
			}
		}
		info = new CalleeInfo(mstrCalleeKey, mstrCalleeIP, mstrCalleeInfo);
		Map<String, CalleeInfo> map = new HashMap<String, CalleeInfo>();
		map.put(mstrCalleeKey, info);
		calleeList.add(map);
	}

	class onAddressingFound implements Runnable {
		@Override
		public void run() {
			for (OnBroadCastAddressingListener listener : allListeners)
				listener.onRemoteAddressReceived(mstrCallerIP, mstrCalleeIP,
						mstrCalleeKey);
		}
	}

	private JSONObject ConfigureJSON() {
		JSONObject object = new JSONObject();
		try {
			object.put("caller_key", mstrCallerKey);
			object.put("callee_key", mstrCalleeKey);
			object.put("caller_info", mstrCallerInfo);
			object.put("callee_info", mstrCalleeInfo);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return object;
	}

	private boolean ParseJSON(String strJSONText) {
		try {
			JSONObject object = (JSONObject) new JSONObject(strJSONText);
			if (object == null)
				return false;
			mstrCallerKey = object.getString("caller_key");
			mstrCalleeKey = object.getString("callee_key");
			
			mstrCallerInfo = object.getString("caller_info");
			mstrCalleeInfo = object.getString("callee_info");

			return true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	class onUDPDataSendBack implements Runnable {
		@Override
		public void run() {
			mstrCalleeKey = mstrLocalKey;  //鏇存柊sip璐﹀彿淇℃伅
			JSONObject sendbackobject = ConfigureJSON();
			mDiscoveryData = sendbackobject.toString();
			Log.i(TAG, "send data: " + mDiscoveryData + " to opposit side:"
					+ mSendbackIP);
			mPingpacket = new DatagramPacket(mDiscoveryData.getBytes(),
					mDiscoveryData.getBytes().length);
			try {
				mPingpacket.setAddress(mSendbackIP);
				mPingpacket.setPort(LISTEN_PORT);
				mSendBCSocket.send(mPingpacket);
			} catch (SocketException e) {
				Log.e(TAG, "Send back fail:" + e.toString());
			} catch (java.io.IOException e) {
				Log.e(TAG, "Send back fail:" + e.toString());
			}
/*			for (OnBroadCastAddressingListener listener : allListeners)
				listener.onRemoteUdpCmd_RegisterByP2P(mstrCalleeIP,
						mstrCallerIP, mbCallerRegisterStatus
								&& mbCalleeRegisterStatus);*/
		}
	}

	class BroadCastSender implements Runnable {
		@Override
		public void run() {
			int i = 0;
			InetAddress broadCastAddress = null;
			try {
				broadCastAddress = getBroadcast();
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				if (broadCastAddress == null)
					broadCastAddress = getBroadCastAddress();
			}
			// InetAddress localAddress = getLocalAddress();
			// mstrLocalIP = localAddress.getHostAddress();
			Log.i(TAG, "Local address is: " + mstrCallerIP);
			Log.i(TAG, "BoardCast address is: " + broadCastAddress.toString());
			JSONObject sendobject = ConfigureJSON();
			mDiscoveryData = sendobject.toString();
			mPingpacket = new DatagramPacket(mDiscoveryData.getBytes(),
					mDiscoveryData.getBytes().length);

			while (i < 5) {
				if (mbNeedRequestAnswer) {
					try {
						mPingpacket.setAddress(broadCastAddress);
						mPingpacket.setPort(LISTEN_PORT);
						mSendBCSocket.send(mPingpacket);
					} catch (SocketException e) {
						Log.e(TAG, "Send fail:" + e.toString());
					} catch (java.io.IOException e) {
						Log.e(TAG, "Send fail:" + e.toString());
					}

					try {
						Thread.sleep(UDP_TIMEOUT);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					break;
				}

				i++;
			}

		}
	}

	public InetAddress getBroadCastAddress() {
		try {
			final InetAddress defaultAddr = InetAddress
					.getByName("255.255.255.255");
			WifiManager wifiManager = (WifiManager) mContext
					.getSystemService(Context.WIFI_SERVICE);
			DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
			if (null == dhcpInfo) {
				return defaultAddr;
			}
			int boardcast = (dhcpInfo.ipAddress & dhcpInfo.netmask)
					| ~dhcpInfo.netmask;
			byte[] quads = new byte[4];
			for (int i = 0; i < 4; i++) {
				quads[i] = (byte) ((boardcast >> i * 8) & 0xff);
			}
			return InetAddress.getByAddress(quads);
		} catch (UnknownHostException e) {
			Log.e(TAG, e.toString());
		}
		return null;
	}

	// 
	public InetAddress getLocalWifiAddress() {
		try {
			final InetAddress defaultAddr = InetAddress.getByName("0.0.0.0");
			WifiManager wifiManager = (WifiManager) mContext
					.getSystemService(Context.WIFI_SERVICE);
			DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
			if (null == dhcpInfo) {
				return defaultAddr;
			}
			int address = dhcpInfo.ipAddress;
			byte[] quads = new byte[4];
			for (int i = 0; i < 4; i++) {
				quads[i] = (byte) ((address >> i * 8) & 0xff);
			}
			return InetAddress.getByAddress(quads);
		} catch (UnknownHostException e) {
			Log.e(TAG, e.toString());
		}
		return null;
	}

	public InetAddress getLocalNoWifiAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr
						.hasMoreElements();) {
					InetAddress inetAddress = (InetAddress) enumIpAddr
							.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& !inetAddress.isLinkLocalAddress()) {
						return inetAddress;
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(TAG, ex.toString());
		}
		
		InetAddress defaultAddr = null;
		try {
			defaultAddr = InetAddress.getByName("0.0.0.0");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return defaultAddr;
	}

	//
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr
						.hasMoreElements();) {
					InetAddress inetAddress = (InetAddress) enumIpAddr
							.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& !inetAddress.isLinkLocalAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e(TAG, ex.toString());
		}
		return null;
	}

	//
	public String getLocalAddress() {
		return getLocalInetAddress().getHostAddress();
	}

	//
	public InetAddress getLocalInetAddress() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		InetAddress LocalIpAddress;
		boolean bUseWifi = wifiManager.isWifiEnabled()
				&& wifiManager.getWifiState() == wifiManager.WIFI_STATE_ENABLED;
		WifiInfo curConnection = wifiManager.getConnectionInfo();
		if (bUseWifi && (curConnection.getLinkSpeed() > 0)) {
			LocalIpAddress = getLocalWifiAddress();
			return LocalIpAddress;
		} else // no wifi
		{
			LocalIpAddress = getLocalNoWifiAddress();
			return LocalIpAddress;
		}
	}

	public InetAddress getBroadcast() throws SocketException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		for (Enumeration<NetworkInterface> niEnum = NetworkInterface
				.getNetworkInterfaces(); niEnum.hasMoreElements();) {
			NetworkInterface ni = niEnum.nextElement();
			if (!ni.isLoopback() ) {
				for (InterfaceAddress interfaceAddress : ni
						.getInterfaceAddresses()) {
					if (interfaceAddress.getBroadcast() != null) {
						InetAddress inetLocal = getLocalInetAddress();
						if (inetLocal == null)
							continue;
						byte[] inetAddr = inetLocal.getAddress();
						byte[] inetbAddr = interfaceAddress.getBroadcast()
								.getAddress();
						if (inetAddr[0] != inetbAddr[0]
								|| inetAddr[1] != inetbAddr[1])
							continue;
						return interfaceAddress.getBroadcast();// .toString().substring(1);
					}
				}
			}
		}
		return null;
	}
}
