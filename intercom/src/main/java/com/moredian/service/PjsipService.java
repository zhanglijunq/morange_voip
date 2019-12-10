package com.moredian.service;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;

import com.moredian.bean.CallHistoryBean;
import com.moredian.bean.SipMessage;
import com.moredian.bean.UserInfoBean;
import com.moredian.common.CommunicateConstants;
import com.moredian.common.RtcServerConfiguration;
import com.moredian.common.SipCode;
import com.moredian.event.CallStatusEvent;
import com.moredian.event.CallTimeEvent;
import com.moredian.event.OpenDoorEvent;
import com.moredian.factory.RegisterPushDiretor;
import com.moredian.intercom.BuildConfig;
import com.moredian.intercom.ISipAidlInterface;
import com.moredian.intercom.SCallBackListener;
import com.moredian.rtcengine.NativeRTC;
import com.moredian.rtcengine.RTCEngineEventListener;
import com.moredian.ui.CommunicateActivity;
import com.moredian.utils.CameraUtils;
import com.moredian.utils.GsonUtils;
import com.moredian.utils.LogUtils;
import com.moredian.utils.RomUtil;
import com.moredian.utils.SharedPreferencesUtil;
import com.moredian.utils.SoundPoolUtil;
import com.moredian.utils.ThreadPoolManager;
import com.moredian.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2018/10/17
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class PjsipService extends Service implements RTCEngineEventListener {
    private Context context;
    private int count = 0;
    private RemoteCallbackList<SCallBackListener> demandList = new RemoteCallbackList<>();
    private UserInfoBean mUserInfoBean;
    private int callStatus;
    private int callType;
    private boolean isRegister;
    private String turnServer="47.99.132.6";
    private long callStartTime,callEndTime;
    private boolean mIsNetConnect=true;
    private boolean isCalling;
    private boolean mIsAcceptCall;
    private LockScreenReceiver mReceiver;
    private int loopCount;
    private boolean isIncomCall;
    private boolean isLock = false;
    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    static {
        System.loadLibrary("modiRTC_jni");
        LogUtils.e("Library loaded");
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (demandList==null){
                return;
            }
            int what = msg.what;
            if (SipCode.HEART_BEAT==what){
                int nums = demandList.beginBroadcast();
                mHandler.removeMessages(SipCode.HEART_BEAT);
                for (int i = 0; i < nums; i++) {
                    try {
                        demandList.getBroadcastItem(i).heartBeat(SipCode.HEART_BEAT,isRegister);
                        LogUtils.e("发送心跳");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                demandList.finishBroadcast();
                mHandler.sendEmptyMessageDelayed(SipCode.HEART_BEAT,10*1000);
            }else if (SipCode.BACK_HISTORY==what){
                mHandler.removeMessages(SipCode.BACK_HISTORY);
                if (mUserInfoBean==null){
                    return;
                }
                int nums = demandList.beginBroadcast();
                int callDuringTime = (int) msg.obj;
                CallHistoryBean callHistoryBean = new CallHistoryBean();
                callHistoryBean.setCallType(callType);
                callHistoryBean.setCallMode(0==mUserInfoBean.getVideoFlag()?1:2);
                callHistoryBean.setFromName(mUserInfoBean.getFromName());
                callHistoryBean.setFromNum(mUserInfoBean.getFromNum());
                callHistoryBean.setToName(mUserInfoBean.getToName());
                callHistoryBean.setToNum(mUserInfoBean.getToNum());
                callHistoryBean.setRoomConstructId(mUserInfoBean.getRoomConstructId());
                callHistoryBean.setCallDeviceType(mUserInfoBean.getCallDeviceType());
                callHistoryBean.setCallStatus(callStatus);
                callHistoryBean.setCallStartTime(callStartTime);
                if (callEndTime<callStartTime){
                    callEndTime = System.currentTimeMillis();
                }
                callHistoryBean.setCallEndTime(callEndTime);
                callHistoryBean.setCallDuringTime(callDuringTime);
                LogUtils.e("demandList.beginBroadcast().size==="+nums+"callStartTime------>"+callStartTime+"---callEndTime---->"+callEndTime);
                for (int i = 0; i < nums; i++) {
                    try {
                        count++;
                        demandList.getBroadcastItem(i).callBack(count,callHistoryBean);
                        LogUtils.e("发送通话记录");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                clearGlobalVariable();
                demandList.finishBroadcast();
            }else if (SipCode.REGISTER_SIP==what){
//                mHandler.removeMessages(SipCode.REGISTER_SIP);
//                registerSip();
            }else if (SipCode.DOOR_STATUS==what){
                mHandler.removeMessages(SipCode.DOOR_STATUS);
                boolean isOpen = (boolean) msg.obj;
                int nums = demandList.beginBroadcast();
                for (int i = 0; i < nums; i++) {
                    try {
                        count++;
                        demandList.getBroadcastItem(i).doorStatusCallBack(isOpen);
                        LogUtils.e("发送通话记录");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                demandList.finishBroadcast();
            }else if (SipCode.CHECK_INCOME_ALERT==what){
                mHandler.removeMessages(SipCode.CHECK_INCOME_ALERT);
                String className = (((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1).get(0)).topActivity.getClassName();
                LogUtils.e("CHECK_INCOME_ALERT className "+className);
                if (!className.contains("com.moredian")){
                    beginLoop();
                }
            }
        }
    };

    private void clearGlobalVariable() {
        mUserInfoBean=null;
        callStatus=-1;
        callStartTime=0;
        callEndTime=0;
    }

    ISipAidlInterface.Stub stub = new ISipAidlInterface.Stub() {
        @Override
        public void registerListener(SCallBackListener listener) throws RemoteException {
            demandList.register(listener);
        }

        @Override
        public void unregisterListener(SCallBackListener listener) throws RemoteException {
            demandList.unregister(listener);
        }

        @Override
        public void init(String domain) throws RemoteException {
            NativeRTC.init("sip:"+domain, false, false);//m_bGoTLS);
            NativeRTC.setTurnServer2(turnServer);
            NativeRTC.setCaptureOrient(CameraUtils.getFrontCamera(),0);
        }

        @Override
        public void registerSip(String account, String password, String domain) throws RemoteException {
            RtcServerConfiguration.getInstance().setRtcAddress(domain);
            RtcServerConfiguration.getInstance().setRtcAccount(account);
            RtcServerConfiguration.getInstance().setRtcPassword(password);
            LogUtils.e("account------>"+account+"--password---->"+password+"---domain--->"+domain);
            PjsipService.this.registerSip();
        }

        @Override
        public void unRegisterSip() throws RemoteException {
            NativeRTC.unregister();
        }

        @Override
        public void registerMainProcess(String processName, int mainPid) throws RemoteException {
            LogUtils.e("mainProcessName------>"+processName+"--mpid---->"+mainPid);
        }

        @Override
        public void registerNetWrokStatus(boolean isConnect) throws RemoteException {
            if(!mIsNetConnect && mIsNetConnect!=isConnect){
                reconnectSip();
            }
            mIsNetConnect = isConnect;
        }

        @Override
        public void registerAcceptConditions(boolean isAcceptCall) throws RemoteException {
            mIsAcceptCall = isAcceptCall;
        }

        @Override
        public void call(long orgId, long roomConstructId,String mobile,String fromName,String fromNum,String toName,String toNum, boolean enable_video, boolean enable_LocalVideo,boolean isNetConnect) throws RemoteException {
            callStartTime=System.currentTimeMillis();
            callType = SipCode.STATUS_DIAL;
            LogUtils.e("callStartTime-------->"+callStartTime);
            int videoFlag = 0;
            if (!enable_video){
                videoFlag=1;
            }
            UserInfoBean userInfoBean = new UserInfoBean(orgId,roomConstructId,mobile,fromName,fromNum,toName,toNum,videoFlag,7);
            mUserInfoBean = userInfoBean;
            if (isNetConnect){
                CommunicateActivity.getInstance(context,SipCode.STATUS_DIAL,userInfoBean,"");
            }
        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.e("pjsipservice bind");
        mHandler.sendEmptyMessageDelayed(SipCode.HEART_BEAT, 10*1000);
        return stub;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        LogUtils.e("pjsipservice onCreate");
        this.context = getApplicationContext();
        SoundPoolUtil.getInstance().init(context);
        ToastUtils.init(context);
        initNativeRtc();
        initLockScreen();
    }

    /**
     * 初始化RTC服务
     */
    private void initNativeRtc() {
        NativeRTC.initializeAndroidGlobals(this, true, true, false);
        NativeRTC.addEventListerer(this);
    }

    private void initLockScreen() {
        mReceiver = new LockScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mReceiver, filter);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        SoundPoolUtil.getInstance().release();
        NativeRTC.removeListener(this);
        NativeRTC.uninit();
        LogUtils.e("pjsipservice onDestroy complete");
        if (mReceiver!=null){
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtils.e("pjsipservice unbind");
        mHandler.removeCallbacksAndMessages(null);
        return super.onUnbind(intent);
    }


    @Override
    public void onRebind(Intent intent) {
        LogUtils.e("pjsipservice onRebind");
        super.onRebind(intent);
    }

    @Override
    public void onRegister(int status) {
        LogUtils.e("onRegister status----->"+status);
        isRegister = (CommunicateConstants.OK==status);
        mHandler.sendEmptyMessage(SipCode.HEART_BEAT);
//        if (!isRegister){
//            mHandler.sendEmptyMessage(SipCode.REGISTER_SIP);
//        }
    }

    private void registerSip() {
        cachedThreadPool.execute(registerRunnable);
    }

    Runnable registerRunnable = () -> NativeRTC.register(RtcServerConfiguration.getInstance().getRtcAccount(), RtcServerConfiguration.getInstance().getRtcServerAddress(), RtcServerConfiguration.getInstance().getRtcPassword());

    @Override
    public void onUnregister(int status) {
        LogUtils.e("onUnregister status----->"+status);
        isRegister = false;
    }

    @Override
    public void onCallStart(int status) {
        LogUtils.e("onCallStart status----->"+status);
        if (status>=300){
            callEndTime = System.currentTimeMillis();
            isCalling = false;
            isIncomCall = false;
            LogUtils.e("onCallStart callEndTime----->"+callEndTime);
        }
        callStatus = status;
        EventBus.getDefault().post(new CallStatusEvent(CallStatusEvent.CALL_START,status));
    }

    @Override
    public void onCallStop(int status) {
        isCalling = false;
        isIncomCall = false;
        callStatus = status;
        callEndTime = System.currentTimeMillis();
        LogUtils.e("onCallStop status----->"+status+"------>callEndTime---->"+callEndTime);
        EventBus.getDefault().post(new CallStatusEvent(CallStatusEvent.CALL_STOP,status));
    }



    @Override
    public void onIncomingCall(UserInfoBean userInfoBean) {
        isIncomCall = true;
        wakeUpAndUnlock(this);
        String className = (((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1).get(0)).topActivity.getClassName();
        LogUtils.e("PjsipService onIncomingCall mIsAcceptCall:"+mIsAcceptCall+";className:"+className);
        if (!mIsAcceptCall){
            Runnable testRunnable= () -> NativeRTC.rejectCall();
            cachedThreadPool.execute(testRunnable);
            return;
        }
        mUserInfoBean = userInfoBean;
        showIncomeView();
    }

    private void beginLoop() {
        cachedThreadPool.execute(loopRunnable);
    }

    Runnable loopRunnable = new Runnable() {
        @Override
        public void run() {
            loopCount = 20;
            while (loopCount>0){
                synchronized (this){
                    if (!isIncomCall){
                        break;
                    }
                    String className = (((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1).get(0)).topActivity.getClassName();
                    if (className.contains("com.moredian")){
                        showIncomeView();
                        LogUtils.e("PjsipService end while showIncomeView");
                        break;
                    }else {
                        loopCount--;
                        LogUtils.e("PjsipService end while loopCount:"+loopCount);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                }
            }
            LogUtils.e("PjsipService end while: "+isIncomCall);
        }
    };

    private void showIncomeView() {
        LogUtils.e("onIncomingCall callStartTime--->"+callStartTime);
        callStartTime = System.currentTimeMillis();
        callType = SipCode.STATUS_ANSWER;
        CommunicateActivity.getInstance(this, SipCode.STATUS_ANSWER,mUserInfoBean,"");
        mHandler.sendEmptyMessageDelayed(SipCode.CHECK_INCOME_ALERT,2*1000);
    }

    @Override
    public void onNotifyVideoStreamRenderID(int id) {
        LogUtils.e("onNotifyVideoStreamRenderID id----->"+id);
    }

    @Override
    public void onServiceDown(int status) {
        LogUtils.e("onServiceDown status----->"+status);
    }

    @Override
    public void onRemotePeerDrop() {
        LogUtils.e("onRemotePeerDrop");
    }

    @Override
    public void onRecvMsg(String msgData) {
        LogUtils.e("onRecvMsg--->"+msgData);
        if (TextUtils.isEmpty(msgData)){
            return;
        }
        String msgdatas[] = msgData.split("messageCode");
        if (msgdatas.length>1){
            SipMessage sipMessage = GsonUtils.getGson().fromJson(msgData,SipMessage.class);
            if (sipMessage!=null){
                int messageCode = sipMessage.getMessageCode();
                if (SipCode.SWITCH_AUDIO==messageCode){
                    EventBus.getDefault().post(new CallStatusEvent(CallStatusEvent.CALL_START,CommunicateConstants.SWITCH_AUDIO));
                }else if (SipCode.OPEN_DOOR_STATUS==messageCode){
                    boolean isOpen = (boolean) sipMessage.getMessageContent();
                    Message message = new Message();
                    message.what = SipCode.DOOR_STATUS;
                    message.obj = isOpen;
                    mHandler.sendMessage(message);
                    EventBus.getDefault().post(new OpenDoorEvent(isOpen));
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCallTime(CallTimeEvent event){
        int callTime = event.getCallTime();
        Message message = new Message();
        message.what = SipCode.BACK_HISTORY;
        message.obj = callTime;
        mHandler.sendMessage(message);
    }

    private void reconnectSip(){
        NativeRTC.reCreateTransport();
    }

    public static void wakeUpAndUnlock(Context context){
        KeyguardManager km= (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
        kl.disableKeyguard();
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,PjsipService.class.getSimpleName());
        wl.acquire();
        wl.release();
    }

    private class LockScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String change = "";
                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    change = "亮屏";
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    isLock = true;
                    change = "锁屏";
                } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    isLock = false;
                    change = "解锁";
                }
                LogUtils.e(change);
            }
        }
    }
}
