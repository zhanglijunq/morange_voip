package com.moredian.rtcengine;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONObject;



import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class UDPListen {
	private static UDPListen uniqInstance;
	private static int mListenPort;
	private static List<OnUDPPacketReceivedListener> allListeners;
	private static DatagramSocket mServerSocket;
	private static Handler handler = new Handler();
	private static final String TAG = "UDPListen";
	private static boolean mbExitThread = false;
	private UDPListen() {
		allListeners = new ArrayList<OnUDPPacketReceivedListener>();
	}

	public static UDPListen getInstance() {
		if (uniqInstance == null) {
			uniqInstance = new UDPListen();
		}
		return uniqInstance;
	}
	public enum UDPWriterErrors {
		UnknownHostException, IOException, otherProblem, OK
	}
	public UDPWriterErrors init(int port) {
		setListenPort(port);
		InitUDPListenTask task = new InitUDPListenTask();
		task.start();
//		task.execute(new Void[0]);
		mbExitThread = false;
		return UDPWriterErrors.OK;
	}
	public static void closeStreams() {
		// TODO Auto-generated method stub
		mbExitThread = true;
		try {
			mServerSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static UDPWriterErrors writeToSocket(JSONObject obj) {
		try {
//			out.write(obj.toString() + System.getProperty("line.separator"));
//			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return UDPWriterErrors.OK;

	}
	
	public static void addListener(OnUDPPacketReceivedListener listener) {
		allListeners.add(listener);
	}

	public static int getServerPort() {
		return mListenPort;
	}

	public static void setListenPort(int listenPort) {
		UDPListen.mListenPort = listenPort;
	}
	public class InitUDPListenTask extends Thread {
		public InitUDPListenTask() {

		}
		@Override
		public void run() {

			try {
				mServerSocket = new DatagramSocket(mListenPort);
//				mServerSocket.setBroadcast(true);
				mServerSocket.setReuseAddress(true);
	            byte data[] = new byte[4096];
	            final DatagramPacket packet = new DatagramPacket(data, data.length);
	            while (!mbExitThread) {
	            	mServerSocket.receive(packet);
	            	InetAddress clientAddress = packet.getAddress();
	            	int clientPort = packet.getPort();
	            	byte[] sendData = {0x00,0x01};
	            	DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
	            	handler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							for (OnUDPPacketReceivedListener listener : allListeners)
								listener.onUDPPacketReceived(packet);

						}
					});                
	            }

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}
