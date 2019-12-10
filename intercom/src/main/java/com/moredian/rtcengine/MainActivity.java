//package com.moredian.rtcengine;
//
////import com.moredian.rtcengine.WebServer.OnHttpServerListener;
//
//
//import android.app.Activity;
//import android.app.AlertDialog.Builder;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.content.res.Configuration;
//import android.media.AudioManager;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.util.Log;
//import android.view.Display;
//import android.view.Surface;
//import android.view.SurfaceHolder;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import org.webrtc.EglBase;
//import org.webrtc.SurfaceViewRenderer;
//
////import org.webrtc.PeerConnectionFactory;
//
//
//public class MainActivity extends Activity implements Button.OnClickListener, RTCEngineEventListener, SurfaceHolder.Callback, RTCVideoCaptureListener {//, OnHttpServerListener {
//	private static final String TAG = "MainActivity";
//
//	private Context mCtx = null;
//
//	private Button m_btnInit;
//	//register UI
//	private Button m_btnRegister;
//	boolean m_btnRegisterState = false;
//	private EditText m_etUserName;
//	private EditText m_etPSWD;
//    private EditText m_etRoomHost;
//	private EditText m_etDomain;
//	//private CheckBox m_dbGoExt;
//	private TextView m_tvRegisterStatus;
//	private EditText m_etClientServer;
//	private EditText m_etAuthURI;
//
//	//Call UI
//	private Button m_btnCall;
//	boolean m_btnCallState = false;
//	private EditText m_etRemote;
//	private EditText m_etRemoteDomain;
//	private TextView m_tvCallStatus;
//	private Button m_btnSendMsg;
//
//	//Answer UI
//	private TextView m_tvIncomming;
//	private Button m_btnAnswer;
//	private Button m_btnReject;
//
//	//window
//	private SurfaceViewRenderer m_svPreview;
//	private SurfaceViewRenderer m_svRemote;
//
//	private VideoCapturePreview mLocalCapturePreview;
//
//	//render
//	private long m_localRender = 0;
//	private long m_remoteRender = 0;
//	private TextView m_tvConfig;
//
//	private Handler mMainHandler = null;
//	private int mVideoStreamRenderID = -1;
//
//	private final static int MSG_MAIN_REGISTER = 1;
//	private final static int MSG_MAIN_START_CALL = 2;
//	private final static int MSG_MAIN_STOP_CALL = 3;
//	private final static int MSG_MAIN_INCOMMING_CALL = 4;
//	private final static int MSG_MAIN_UNREGISTER = 5;
//	private final static int MSG_MAIN_AUTH_READY = 6;
//	private final static int MSG_MAIN_SERVICE_DOWN = 7;
//	private final static int MSG_MAIN_REMOTE_PEER_DROP = 8;
//
//	private final static int CAPTURE_ORIENT_UNKNOWN = 0;
//	private final static int CAPTURE_ORIENT_NATURAL = 1;
//	private final static int CAPTURE_ORIENT_ROTATE_90DEG = 2;
//	private final static int CAPTURE_ORIENT_ROTATE_180DEG = 3;
//	private final static int CAPTURE_ORIENT_ROTATE_270DEG = 4;
//
//	private boolean m_incall = false;
//	private boolean m_bGoExt = false;
//	private boolean m_bGoTLS = false;
//	private boolean m_bGoPSWD = false;		//always true
//	private String domain = "";
//	private String myID = "";
//	private String herID = "";
//	private String pswd = "";		//"" is no password
//    private String room = "";
//    private String roomHost=myID;
//    private int bJoinRoom = 0;
//    private String remoteIP="";
//
//	private EglBase rootEglBase;
//	private boolean m_bTestVideoExternalCapture = false;
//	private MainActivity mThis;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        mCtx = this;
//        mThis = this;
//        setContentView(R.layout.activity_main);
//        m_btnInit = (Button)findViewById(R.id.btnInit);
//        m_btnInit.setOnClickListener(this);
//
//        m_btnRegister = (Button)findViewById(R.id.btnRegister);
//        m_btnRegister.setOnClickListener(this);
//        m_etUserName = (EditText)findViewById(R.id.etUserName);
//        m_etPSWD = (EditText)findViewById(R.id.etPSWD);
//        m_etRoomHost = (EditText)findViewById(R.id.etRoomHost);
//        m_etRoomHost.setText(roomHost);
//        m_etDomain = (EditText)findViewById(R.id.etRegistarURI);
//        //m_dbGoExt = (CheckBox)findViewById(R.id.cbExt);
//        m_tvRegisterStatus = (TextView)findViewById(R.id.tvRegisterStatus);
//        m_tvRegisterStatus.setText("null");
//        m_etAuthURI = (EditText)findViewById(R.id.etAuthURI);
//        m_etClientServer = (EditText)findViewById(R.id.etClientServer);
//
//
//        m_btnCall = (Button)findViewById(R.id.btnCall);
//        m_btnCall.setOnClickListener(this);
//        m_etRemote = (EditText)findViewById(R.id.etRemoteID);
//        m_etRemoteDomain = (EditText)findViewById(R.id.etRemoteDomain);
//        m_tvCallStatus = (TextView)findViewById(R.id.tvCallStatus);
//        m_tvCallStatus.setText("null");
//
//        m_btnSendMsg = (Button)findViewById(R.id.btnSendMsg);
//        m_btnSendMsg.setOnClickListener(this);
//
//        m_tvIncomming = (TextView)findViewById(R.id.tvIncomming);
//        m_btnAnswer = (Button)findViewById(R.id.btnAnswer);
//        m_btnAnswer.setOnClickListener(this);
//        m_btnReject = (Button)findViewById(R.id.btnReject);
//        m_btnReject.setOnClickListener(this);
//
//        m_svPreview = (SurfaceViewRenderer)findViewById(R.id.svPreview);
//        m_svPreview.getHolder().addCallback(this);
//        m_svRemote = (SurfaceViewRenderer)findViewById(R.id.svRemote);
//        m_svRemote.getHolder().addCallback(this);
//
//      //for webrtc specific
//        NativeRTC.initializeAndroidGlobals(this, true, true, false);
//
//        rootEglBase = EglBase.create();
//        if(!m_bTestVideoExternalCapture){
//        	m_svPreview.init(rootEglBase.getEglBaseContext(), null);
//        	m_svPreview.setMirror(true);
//        }
//        m_svRemote.init(rootEglBase.getEglBaseContext(), null);
//        m_svRemote.setMirror(false);
//
//        m_tvConfig = (TextView)findViewById(R.id.tvConfig);
//
//        //do not show keyboard autoly
//        getWindow().setSoftInputMode(
//                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//
//
//        mMainHandler = new Handler() {
//        	@Override
//            public void handleMessage(Message msg) {
//                switch( msg.what ) {
//                    case MSG_MAIN_REGISTER:
//                    {
//                    	int status = msg.arg1;
//                    	if (status == 200)
//                		{
//                			m_btnRegister.setEnabled(true);
//                			m_tvRegisterStatus.setText("OK");
//
//                			m_btnRegister.setText("unregister");
//            				m_btnRegisterState = true;
//                		}
//                    	else {
//                    		m_btnRegister.setEnabled(true);
//                			m_tvRegisterStatus.setText(String.valueOf(status));
//                    	}
//                    }
//                    	break;
//                    case MSG_MAIN_UNREGISTER:
//                    {
//                    	int status = msg.arg1;
//            			m_btnRegister.setEnabled(true);
//            			if (status == 200)
//            				m_tvRegisterStatus.setText("OK");
//            			else
//            				m_tvRegisterStatus.setText(String.valueOf(status));
//
//            			m_btnRegister.setText("register");
//        				m_btnRegisterState = false;
//                    }
//                    	break;
//                    case MSG_MAIN_START_CALL:
//                    {
//                    	int status = msg.arg1;
//                    	if (status == 200) {
//                			m_btnCall.setEnabled(true);
//                			m_tvCallStatus.setText("OK");
//
//                			m_btnCall.setText("stop call");
//            				m_btnCallState = true;
//            				if(m_bTestVideoExternalCapture && mLocalCapturePreview != null)
//            					mLocalCapturePreview.setCaptureListener(mThis);
//
//                		}
//                    	else if(status == 603){
//                    		m_btnCall.setEnabled(true);
//                			m_tvCallStatus.setText(String.valueOf(status));
//                			m_incall = false;
////                			NativeRTC.stopCall();
//							if(m_bTestVideoExternalCapture)
//                				mLocalCapturePreview.stopCapture();
//                    	}
//                    	else {
//                    		m_btnCall.setEnabled(true);
//                			m_tvCallStatus.setText(String.valueOf(status));
//                			m_incall = false;
//                    	}
//
//                    }
//                    	break;
//                    case MSG_MAIN_STOP_CALL:
//                    {
//                    	int status = msg.arg1;
//                		if (status == 200) {
//                			m_btnCall.setEnabled(true);
//                			m_tvCallStatus.setText("OK");
//
//                			m_btnCall.setText("start call");
//            				m_btnCallState = false;
//                		}
//                		else {
//                			m_btnCall.setEnabled(true);
//                			m_tvCallStatus.setText(String.valueOf(status));
//                		}
//                		m_incall = false;
//                    }
//                    	break;
//                    case MSG_MAIN_INCOMMING_CALL:
//                    {
//                    	if (m_incall == true) {
//                    		Log.w(TAG, "[shit]I am in a call, so auto hangup the incomming new call");
//                    		NativeRTC.rejectCall();
//                    		m_incall = false;
//                    	}
//                    	else {
//	                    	String remoteURI = (String)msg.obj;
//	                    	m_tvIncomming.setVisibility(View.VISIBLE);
//	                		m_btnAnswer.setVisibility(View.VISIBLE);
//	                		m_btnReject.setVisibility(View.VISIBLE);
//	                		m_tvIncomming.setText(remoteURI + " incomming call...");
//	                		m_btnCall.setEnabled(false);
//                    	}
//                    }
//                    	break;
//
//                    case MSG_MAIN_AUTH_READY:
//                    {
//                    	m_etPSWD.setText(pswd);
//                    	m_etUserName.setText(myID);
//                    	m_etDomain.setText(domain);
//                    	m_etRemoteDomain.setText(domain);
//                    	m_etDomain.setEnabled(true);
//                        if(bJoinRoom !=0){
//                            m_etRemote.setText(room);
//                        }
//
//                    	NativeRTC.init("sip:"+m_etDomain.getText().toString(), m_bGoExt,false);// m_bGoTLS);
//                    	if(!m_bTestVideoExternalCapture){
//                    		if(m_localRender!=0)
//                    			NativeRTC.destroyRender(m_localRender);
//                    		m_localRender = NativeRTC.createRender(m_svPreview);
//                    		NativeRTC.setPreviewRender(m_localRender);
//                    	}
//
//            			if(m_remoteRender!=0)
//            				NativeRTC.destroyRender(m_remoteRender);
//            			m_remoteRender = NativeRTC.createRender(m_svRemote);
//            			NativeRTC.setStreamRender(m_remoteRender, 0);
// //                   	NativeRTC.setEGLContext(rootEglBase.getEglBaseContext());
//                    	//NativeRTC.setCaptureOrient(CAPTURE_ORIENT_ROTATE_270DEG);		//hardcode for portrait
//                    	m_btnRegister.setEnabled(true);
//                		m_btnCall.setEnabled(true);
//                    }
//                    	break;
//                    case MSG_MAIN_SERVICE_DOWN:
//                    {
//                    	Toast.makeText(mCtx, "Bad news: service is down:" + msg.arg1, Toast.LENGTH_LONG).show();
//                    }
//                    break;
//                    case MSG_MAIN_REMOTE_PEER_DROP:
//                    {
//                    	Toast.makeText(mCtx, "Bad news: remote peer drop", Toast.LENGTH_LONG).show();
//                    }
//                    break;
//                }
//        	}
//        };
//
//
//        //test
//        //NativeRTC.main();
//        NativeRTC.addEventListerer(this);
//
//        m_bGoExt = false;
//        if(m_bTestVideoExternalCapture)
//        	m_bGoExt = true;
//
////        mWebServer = WebServer.getInstance(this);
////        mWebServer.setHttpServerListener(this);
//
//     // set default speak on/off status
//        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
////        if(!audioManager.isSpeakerphoneOn())
////        	audioManager.setSpeakerphoneOn(true);
//
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        WindowManager wm;
//        Display display;
//        int rotation;
//        int orient;
//
//        wm = (WindowManager)this.getSystemService(Context.WINDOW_SERVICE);
//        display = wm.getDefaultDisplay();
//        rotation = display.getRotation();
//        System.out.println("Device orientation changed: " + rotation);
//
//        switch (rotation) {
//        case Surface.ROTATION_0:   // Portrait
//            orient = CAPTURE_ORIENT_ROTATE_270DEG;
//            break;
//        case Surface.ROTATION_90:  // Landscape, home button on the right
//            orient = CAPTURE_ORIENT_NATURAL;
//            break;
//        case Surface.ROTATION_180:
//            orient = CAPTURE_ORIENT_ROTATE_90DEG;
//            break;
//        case Surface.ROTATION_270: // Landscape, home button on the left
//            orient = CAPTURE_ORIENT_ROTATE_180DEG;
//            break;
//        default:
//            orient = CAPTURE_ORIENT_UNKNOWN;
//            break;
//        }
//
//        NativeRTC.setCaptureOrient(1,orient);
//    }
//
//    private void dialog() {
//        Builder builder = new Builder(this);
//        //builder.setIcon(R.mipmap.ic_launcher);
//        //builder.setTitle(R.string.simple_list_dialog);
//
//        final String[] items={"External","TLS", "Auth"};
//        builder.setMultiChoiceItems(items, new boolean[]{false, false, true}, new DialogInterface.OnMultiChoiceClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
//                //Toast.makeText(getApplicationContext(),"You clicked "+items[i]+" "+b,Toast.LENGTH_SHORT).show();
//            	switch(i) {
//            	case 0:
//            		//external
//            		m_bGoExt = b;
//            		break;
//            	case 1:
//            		//TLS
//            		m_bGoTLS = b;
//            		break;
//            	case 2:
//            		//PSWD
//            		m_bGoPSWD = b;
//            		break;
//        		default:
//        			break;
//            	}
//            }
//        });
//
//        builder.setPositiveButton("OK", new OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                //Toast.makeText(getApplicationContext(),"OK",Toast.LENGTH_SHORT).show();
//            	m_btnInit.setEnabled(true);
//            	m_btnRegister.setEnabled(false);
//        		m_btnCall.setEnabled(false);
//
//        		if (m_bGoExt == true) {
//        			m_tvConfig.setText(m_tvConfig.getText()+"External;");
//        		}
//        		if (m_bGoTLS == true) {
//        			m_tvConfig.setText(m_tvConfig.getText()+"TLS;");
//        		}
//        		if (m_bGoPSWD == true) {
//        			m_tvConfig.setText(m_tvConfig.getText()+"Auth;");
// //       			m_etClientServer.setEnabled(true);
// //       			m_etAuthURI.setEnabled(true);
//        		}
//            }
//        });
//
//        builder.setCancelable(false);
// //       AlertDialog dialog=builder.create();
// //       dialog.show();
//
//        m_tvConfig.setText("");
//    }
//
//
//    @Override
//    protected void onPause() {
//    	super.onPause();
//
//    	Log.i(TAG, "onPause, call uninit");
////    	NativeRTC.uninit();
//    }
//
//    @Override
//    protected void onResume() {
//    	super.onResume();
//    	Log.i(TAG, "onResume, call init");
//
//
//        domain = "47.99.132.6";
//    	myID = "moditest08";
//    	herID = "moditest07";
//    	pswd = "123456";		//"" is no password
//        roomHost = "";
//        remoteIP = "192.168.9.207";//"192.168.9.242";//
//        m_etPSWD.setText(pswd);
//    	m_etUserName.setText(myID);
//        m_etRoomHost.setText(roomHost);
//        m_etDomain.setText(domain);
//        m_etRemoteDomain.setText(domain);
//        m_etRemote.setText(herID);
//
//    	m_etDomain.setEnabled(true);
//    	m_btnRegister.setText("register");
//    	m_btnRegister.setEnabled(true);
//		m_btnRegisterState = false;
//		m_etClientServer.setEnabled(false);
//		m_etAuthURI.setEnabled(false);
//		m_btnCall.setText("start call");
//		m_btnCall.setEnabled(true);
//		m_btnCallState = false;
//		m_tvIncomming.setVisibility(View.INVISIBLE);
//		m_btnAnswer.setVisibility(View.INVISIBLE);
//		m_btnReject.setVisibility(View.INVISIBLE);
//
//		if(m_bTestVideoExternalCapture){
//			mLocalCapturePreview = new VideoCapturePreview(1,this);
//			VideoCapturePreview.setLocalPreview(m_svPreview.getHolder());
//		}
//		dialog();
//    }
//
//
//    @Override
//    protected void onDestroy() {
//    	super.onDestroy();
//    	rootEglBase.release();
//    }
//
//
//
//	@Override
//	public void onClick(View v) {
//		// TODO Auto-generated method stub
//		switch(v.getId())
//		{
//		case R.id.btnInit:
//			if (m_bGoPSWD == true) {
//				//[step 1]query auth string by client server's http. Not our job
//
//			}
//			else {
//
//				NativeRTC.init("sip:"+m_etDomain.getText().toString(), m_bGoExt, false);//m_bGoTLS);
//				NativeRTC.setCaptureOrient(1, 0);
//				NativeRTC.setTurnServer("47.99.132.6", 3748, "moredian_ice", "9phTnjR3");
//				if(!m_bTestVideoExternalCapture){
//					if(m_localRender!=0)
//						NativeRTC.destroyRender(m_localRender);
//					m_localRender = NativeRTC.createRender(m_svPreview);
//					NativeRTC.setPreviewRender(m_localRender);
//				}
//				if(m_remoteRender!=0)
//					NativeRTC.destroyRender(m_remoteRender);
//				m_remoteRender = NativeRTC.createRender(m_svRemote);
//				NativeRTC.setStreamRender(m_remoteRender, 0);
////				NativeRTC.setEGLContext(rootEglBase.getEglBaseContext());
//            	//NativeRTC.setCaptureOrient(CAPTURE_ORIENT_ROTATE_270DEG);		//hardcode for portrait
//            	m_btnRegister.setEnabled(true);
//        		m_btnCall.setEnabled(true);
//			}
//
//        	m_btnInit.setEnabled(false);				//never enable it again
//
//			break;
//		case R.id.btnRegister:
//			if (m_btnRegisterState == false) {
//				//todo: call rtc engine
//				if (NativeRTC.register(m_etUserName.getText().toString(), m_etDomain.getText().toString(), m_etPSWD.getText().toString()) != 0)
//				{
//					m_tvCallStatus.setText("error");
//					break;
//				}
//
//				v.setEnabled(false);
//				m_tvRegisterStatus.setText("Waiting...");
//			}
//			else {
//				//todo: call rtc engine
//				NativeRTC.unregister();
//				v.setEnabled(false);
//				m_tvRegisterStatus.setText("Waiting...");
//			}
//			break;
//		case R.id.btnCall:
//			if (m_btnCallState == false) {
//				//todo: call rtc engine
//				if(!m_bTestVideoExternalCapture){
//					if(m_localRender!=0)
//						NativeRTC.destroyRender(m_localRender);
//					m_localRender = NativeRTC.createRender(m_svPreview);
//					NativeRTC.setPreviewRender(m_localRender);
//				}else{
//					mLocalCapturePreview.startCapture(640, 480);
//					NativeRTC.setExtVideoCaptureParams(640,480,18,NativeRTC.kVideoType_NV21);
//				}
//
//				if(m_remoteRender!=0)
//					NativeRTC.destroyRender(m_remoteRender);
//				m_remoteRender = NativeRTC.createRender(m_svRemote);
//				NativeRTC.setStreamRender(m_remoteRender, 0);
//				NativeRTC.setPreCallParams(NativeRTC.kUserDefineCallKey,"{orgid:\"123456\",suborgid:\"234\",from:\"1��1��Ԫ\",fromnumber:\"1#1\"}");
////				NativeRTC.startDirectCall(remoteIP, 6020,true);
//				if (NativeRTC.startCall(m_etRemote.getText().toString(), m_etRemoteDomain.getText().toString(),true,true) != 0)
//				{
//					m_tvCallStatus.setText("error");
//					break;
//				}
//				v.setEnabled(false);
//				m_tvCallStatus.setText("Waiting...");
//				m_incall = true;
//			}
//			else {
//				//todo: call rtc engine
//				NativeRTC.stopCall();
//				if(m_bTestVideoExternalCapture)
//					mLocalCapturePreview.stopCapture();
///*				NativeRTC.setPreviewRender(0);		//stop preview, essential
//				NativeRTC.setStreamRender(0, 0);
//				if(m_localRender!=0)
//    				NativeRTC.destroyRender(m_localRender);
//				m_localRender = 0;
//				if(m_remoteRender!=0)
//					NativeRTC.destroyRender(m_remoteRender);
//				m_remoteRender = 0;*/
//
//				v.setEnabled(false);
//				m_tvCallStatus.setText("Waiting...");
//			}
//			break;
//		case R.id.btnAnswer:
////			//3rd call is comming
////			if (m_incall == true)
////			{
////				Log.i(TAG, "pick up a 2nd call while in 1st call");
////				NativeRTC.setPreviewSurface(null);		//stop preview, essential
////				NativeRTC.stopCall();
////			}
//
//			m_btnCall.setEnabled(true);
//
//        	if(m_localRender!=0)
//        		NativeRTC.destroyRender(m_localRender);
//        	m_localRender = NativeRTC.createRender(m_svPreview);
//        	NativeRTC.setPreviewRender(m_localRender);
//
//			if(m_remoteRender!=0)
//				NativeRTC.destroyRender(m_remoteRender);
//			m_remoteRender = NativeRTC.createRender(m_svRemote);
//			NativeRTC.setStreamRender(m_remoteRender, 0);
//			NativeRTC.answerCall(true);
//			m_tvCallStatus.setText("OK");
//			m_tvIncomming.setVisibility(View.INVISIBLE);
//			m_btnAnswer.setVisibility(View.INVISIBLE);
//			m_btnReject.setVisibility(View.INVISIBLE);
//			break;
//		case R.id.btnReject:
//			m_btnCall.setEnabled(true);
//			NativeRTC.rejectCall();
//			m_tvIncomming.setVisibility(View.INVISIBLE);
//			m_btnAnswer.setVisibility(View.INVISIBLE);
//			m_btnReject.setVisibility(View.INVISIBLE);
//			break;
//		case R.id.btnSendMsg:
//			String msgdata = "Message:opendoor";
//			NativeRTC.sendMessage(msgdata);
//			Log.e(TAG, "send data:"+msgdata);
//			break;
//		}
//	}
//
//
//	@Override
//	public void onRegister(int status) {
//		// TODO Auto-generated method stub
//		mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_MAIN_REGISTER, status, 0));
//	}
//
//	@Override
//	public void onUnregister(int status) {
//		// TODO Auto-generated method stub
//		mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_MAIN_UNREGISTER, status, 0));
//	}
//
//
//	@Override
//	public void onCallStart(int status) {
//		// TODO Auto-generated method stub
//		mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_MAIN_START_CALL, status, 0));
//	}
//
//
//	@Override
//	public void onCallStop(int status) {
//		// TODO Auto-generated method stub
//		mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_MAIN_STOP_CALL, status, 0));
//		mVideoStreamRenderID = -1;
//	}
//
//	@Override
//	public void onServiceDown(int status) {
//		//Log.i(TAG, "onNetworkDown");
//		mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_MAIN_SERVICE_DOWN, status, 0));
//	}
//
////	@Override
////	public void onNetworkRecover() {
////		Log.i(TAG, "onNetworkRecover");
////	}
//
//
//	@Override
//	public void onIncomingCall(String remoteURI, String wholeHeaderMsg) {
//		// TODO Auto-generated method stub
//		mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_MAIN_INCOMMING_CALL, remoteURI));
//	}
//
//	@Override
//	public void onRecvMsg(String msgData){
//		Log.e(TAG, "receiv data:"+msgData);
//	}
//
//	@Override
//	public void surfaceChanged(SurfaceHolder holder, int format, int width,
//			int height) {
//		// TODO Auto-generated method stub
//		if (holder.equals(m_svPreview.getHolder()))
//		{
//			Log.i(TAG, "surfaceChanged, setPreviewSurface");
//		}
//		else if (holder.equals(m_svRemote.getHolder()))
//		{
//
//		}
//	}
//
//
//	@Override
//	public void surfaceCreated(SurfaceHolder holder) {
//		// TODO Auto-generated method stub
//		if (holder.equals(m_svPreview.getHolder()))
//		{
//
//		}
//		else if (holder.equals(m_svRemote.getHolder()))
//		{
//
//		}
//	}
//
//
//	@Override
//	public void surfaceDestroyed(SurfaceHolder holder) {
//		// TODO Auto-generated method stub
//		if (holder.equals(m_svPreview.getHolder()))
//		{
//			NativeRTC.setPreviewRender(0);
//		}
//		else if (holder.equals(m_svRemote.getHolder()))
//		{
//			NativeRTC.setStreamRender(0, -1);
//		}
//	}
//
//
//
//
//	static {
//    	//load essential libs
//
//    	//load app-jni libs
//    	System.loadLibrary("modiRTC_jni");
//
//    	System.out.println("Library loaded");
//    }
//
//
//
//
//	@Override
//	public void onNotifyVideoStreamRenderID(int id) {
//		// TODO Auto-generated method stub
//		if (mVideoStreamRenderID != id) {
//			mVideoStreamRenderID = id;
//			Log.i(TAG, "onNotifyVideoStreamRenderID, rnd_id:"+id+", surface:"+m_svRemote.getHolder().getSurface());
//			if (m_bGoExt == false) {
////				VideoRenderer vr = new VideoRenderer(m_svRemote);
//				if(m_remoteRender!=0)
//					NativeRTC.destroyRender(m_remoteRender);
//				m_remoteRender = NativeRTC.createRender(m_svRemote);
//				NativeRTC.setStreamRender(m_remoteRender, mVideoStreamRenderID);
//			}
//		}
//	}
//
//
//
//	@Override
//	public void onRemotePeerDrop() {
//		// TODO Auto-generated method stub
//		//Log.i(TAG, "onNetworkDown");
//		mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_MAIN_REMOTE_PEER_DROP));
//	}
//
//
//	@Override
//	public void onFrame(byte[] data, int len, int rotation, long timestamp){
//		NativeRTC.putExtVideoData(data, len, rotation, timestamp);
//	}
//
//}
