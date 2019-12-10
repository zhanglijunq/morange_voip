package com.moredian.factory;

import android.app.Application;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.moredian.bean.CallHistoryBean;
import com.moredian.common.SipCode;
import com.moredian.factory.bean.CustomMessage;
import com.moredian.factory.interfaces.PushCallBack;
import com.moredian.factory.interfaces.SipCallBack;
import com.moredian.factory.network.NetWorkMonitorManager;
import com.moredian.factory.network.NetWorkState;
import com.moredian.intercom.BuildConfig;
import com.moredian.intercom.ISipAidlInterface;
import com.moredian.intercom.R;
import com.moredian.intercom.SCallBackListener;
import com.moredian.service.PjsipService;
import com.moredian.utils.LogUtils;
import com.moredian.utils.SharedPreferencesUtil;
import com.moredian.utils.ThreadPoolManager;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import org.android.agoo.huawei.HuaWeiRegister;
import org.android.agoo.xiaomi.MiPushRegistar;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2019/12/05
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class MorangeVoip {
    private String TAG = MorangeVoip.class.getSimpleName();
    public static final String UPDATE_STATUS_ACTION = "com.umeng.message.example.action.UPDATE_STATUS";
    private Application mApplication;
    private static MorangeVoip instance=null;
    private MorangeDirector mMorangeDirector;
    private ConcreteBuilder mConcreteBuilder;
    private PushCollocation mCollocation;
    private PushCallBack mPushCallBack;
    private Handler handler;

    private Intent serviceIntent;
    private ISipAidlInterface iSipAidlInterface;
    private SipCallBack mSipCallBack;
    private String sipServcer,sipAccount,sipPassword;
    private boolean mIsRegister,isAcceptCall;
    private String mobile;

    public static MorangeVoip getInstance(Application application) {
        if (instance == null) {
            instance = new MorangeVoip(application);
        }
        return instance;
    }

    public MorangeVoip(Application application) {
        this.mApplication = application;
        mConcreteBuilder = new ConcreteBuilder();
        mMorangeDirector = new MorangeDirector(mConcreteBuilder);
    }

    public void setUmeng(String umengAppKey,String umengSecret,String umengChannel){
        mMorangeDirector.bindUmeng(umengAppKey, umengSecret, umengChannel);
    }

    public void setXiaomi(String xiaomiId,String xiaomiKey){
        mMorangeDirector.bindXiaomi(xiaomiId, xiaomiKey);
    }
    public void setHw(String hwId,String hwSecret){
        mMorangeDirector.bindHw(hwId, hwSecret);
    }

    public void setPushCallBack(PushCallBack pushCallBack) {
        mPushCallBack = pushCallBack;
    }

    public void setSipCallBack(SipCallBack sipCallBack) {
        mSipCallBack = sipCallBack;
    }

    public void init(){
        mCollocation = mConcreteBuilder.create();
        initUmeng();
        initHuawei();
        initXiaoMi();
        NetWorkMonitorManager.getInstance().init(mApplication);
    }

    // 参数一：当前上下文context；
    // 参数二：应用申请的Appkey（需替换）；
    // 参数三：渠道名称；
    // 参数四：设备类型，必须参数，传参数为UMConfigure.DEVICE_TYPE_PHONE则表示手机；传参数为UMConfigure.DEVICE_TYPE_BOX则表示盒子；默认为手机；
    // 参数五：Push推送业务的secret 填充Umeng Message Secret对应信息（需替换）
    private void initUmeng() {
        handler = new Handler(Looper.myLooper());
        UMConfigure.setLogEnabled(false);
        UMConfigure.init(mApplication, mCollocation.getUmengAppKey(), mCollocation.getUmengChannel(), UMConfigure.DEVICE_TYPE_PHONE, mCollocation.getUmengSecret());
        //获取消息推送代理示例
        PushAgent mPushAgent = PushAgent.getInstance(mApplication);
        mPushAgent.setNotificationPlayVibrate(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);

        UmengMessageHandler messageHandler = new UmengMessageHandler() {

            /**
             * 通知的回调方法（通知送达时会回调）
             */
            @Override
            public void dealWithNotificationMessage(Context context, UMessage msg) {
                //调用super，会展示通知，不调用super，则不展示通知。
                super.dealWithNotificationMessage(context, msg);
            }

            /**
             * 自定义消息的回调方法
             */
            @Override
            public void dealWithCustomMessage(final Context context, final UMessage msg) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        // 对自定义消息的处理方式，点击或者忽略
                        boolean isClickOrDismissed = true;
                        if (isClickOrDismissed) {
                            //自定义消息的点击统计
                            UTrack.getInstance(context).trackMsgClick(msg);
                        } else {
                            //自定义消息的忽略统计
                            UTrack.getInstance(context).trackMsgDismissed(msg);
                        }
                        Log.e(TAG, msg.custom);
                        Toast.makeText(context, msg.custom, Toast.LENGTH_LONG).show();
                    }
                });
            }

            /**
             * 自定义通知栏样式的回调方法
             */
            @Override
            public Notification getNotification(Context context, UMessage msg) {
                switch (msg.builder_id) {
                    case 1:
                        Notification.Builder builder = new Notification.Builder(context);
                        RemoteViews myNotificationView = new RemoteViews(context.getPackageName(), R.layout.notification_view);
                        myNotificationView.setTextViewText(R.id.notification_title, msg.title);
                        myNotificationView.setTextViewText(R.id.notification_text, msg.text);
                        myNotificationView.setImageViewBitmap(R.id.notification_large_icon, getLargeIcon(context, msg));
                        myNotificationView.setImageViewResource(R.id.notification_small_icon,
                                getSmallIconId(context, msg));
                        builder.setContent(myNotificationView)
                                .setSmallIcon(getSmallIconId(context, msg))
                                .setTicker(msg.ticker)
                                .setAutoCancel(true);

                        return builder.getNotification();
                    default:
                        //默认为0，若填写的builder_id并不存在，也使用默认。
                        return super.getNotification(context, msg);
                }
            }
        };
        mPushAgent.setMessageHandler(messageHandler);

        /**
         * 自定义行为的回调处理，参考文档：高级功能-通知的展示及提醒-自定义通知打开动作
         * UmengNotificationClickHandler是在BroadcastReceiver中被调用，故
         * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
         * */
        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {

            @Override
            public void launchApp(Context context, UMessage msg) {
                super.launchApp(context, msg);
            }

            @Override
            public void openUrl(Context context, UMessage msg) {
                super.openUrl(context, msg);
            }

            @Override
            public void openActivity(Context context, UMessage msg) {
                super.openActivity(context, msg);
            }

            @Override
            public void dealWithCustomAction(Context context, UMessage msg) {
                LogUtils.e("dealWithCustomAction-"+msg.custom);
                try {
                    CustomMessage customMessage = new CustomMessage(msg.getRaw());
                    mPushCallBack.dealWithCustomAction(customMessage);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        //使用自定义的NotificationHandler
        mPushAgent.setNotificationClickHandler(notificationClickHandler);

        //注册推送服务 每次调用register都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                Log.e(TAG, "device token: " + deviceToken);
                mPushCallBack.onRegisterCallBack(true,deviceToken);
//                umengDeviceToken = deviceToken;
                String umengDtn = SharedPreferencesUtil.getString(mApplication,SharedPreferencesUtil.UMENG_TOKEN,"");
                if (!TextUtils.equals(deviceToken,umengDtn)){
                    SharedPreferencesUtil.putString(mApplication,SharedPreferencesUtil.UMENG_TOKEN,deviceToken);
                }
                mApplication.sendBroadcast(new Intent(UPDATE_STATUS_ACTION));
            }

            @Override
            public void onFailure(String s, String s1) {
                Log.i(TAG, "register failed: " + s + " " + s1);
                mPushCallBack.onRegisterCallBack(false,"");
                mApplication.sendBroadcast(new Intent(UPDATE_STATUS_ACTION));
            }
        });
    }

    private void initHuawei() {
        HuaWeiRegister.register(mApplication);
    }

    private void initXiaoMi() {
        MiPushRegistar.register(mApplication, mCollocation.getXiaomiId(), mCollocation.getXiaomiKey());
    }

    public void initSipService(String sipServer,String sipAccount,String sipPassword,String mobile){
        this.sipServcer = sipServer;
        this.sipAccount = sipAccount;
        this.sipPassword = sipPassword;
        this.mobile = mobile;
        SharedPreferencesUtil.putString(mApplication,SharedPreferencesUtil.MOBILE,mobile);
    }

    public void registerAcceptCall(boolean isAcceptCall){
        this.isAcceptCall = isAcceptCall;
    }

    public void registerSip(){
        bindSipService();
        NetWorkMonitorManager.getInstance().register(this);
    }

    public void unregisterSip(){
        if (iSipAidlInterface!=null){
            try {
                iSipAidlInterface.unRegisterSip();
                iSipAidlInterface.unregisterListener(stub);
                iSipAidlInterface = null;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (serviceConnection!=null){
            mApplication.unbindService(serviceConnection);
        }
        NetWorkMonitorManager.getInstance().unregister(this);
    }

    public void startCall(long orgId,long roomConstructId,String mobile,String fromAddress,String fromNo,String toAddress,String toNum,boolean isVideoCall){
        if (iSipAidlInterface!=null){
            try {
                iSipAidlInterface.call(orgId,roomConstructId,mobile,fromAddress,fromNo,toAddress,toNum,isVideoCall,true,true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void bindSipService() {
        if (serviceIntent==null){
            serviceIntent = new Intent(mApplication, PjsipService.class);
            mApplication.bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE);
        }
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.e("mainactivity onServiceConnected");
            iSipAidlInterface = ISipAidlInterface.Stub.asInterface(service);
            // Log.e("registerListener", iMyAidlInterface.toString());
            try {
                iSipAidlInterface.init(sipServcer);
                iSipAidlInterface.registerListener(stub);
                iSipAidlInterface.registerSip(sipAccount,sipPassword,sipServcer);
                iSipAidlInterface.registerAcceptConditions(isAcceptCall);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.e("onServiceDisconnected name.getClassName()"+name.getClassName());
            iSipAidlInterface=null;
            mIsRegister = false;
        }
    };

    SCallBackListener stub = new SCallBackListener.Stub() {
        @Override
        public void callBack(int code, CallHistoryBean bean) throws RemoteException {
            LogUtils.e("pisipsevice call back code==="+code);
            if (mSipCallBack!=null){
                mSipCallBack.onVoipEndCallBack(bean);
            }
        }

        @Override
        public void heartBeat(int code, boolean isRegister) {
            LogUtils.e("sip heartBeat"+isRegister);
            mIsRegister = isRegister;
            if (isRegister){
                LogUtils.d("mainactivity register sip Success");
            }else {
                LogUtils.d("mainactivity register sip Failed");
                mHandler.sendEmptyMessageDelayed(SipCode.REGISTER_SIP,5*1000);
            }
        }

        @Override
        public void doorStatusCallBack(boolean isOpen) throws RemoteException {
            LogUtils.e("id open door ? "+isOpen);
        }
    };

    //不加注解默认监听所有的状态，方法名随意，只需要参数是一个NetWorkState即可
    //@NetWorkMonitor(monitorFilter = {NetWorkState.GPRS})//只接受网络状态变为GPRS类型的消息
    public void onNetWorkStateChange(NetWorkState netWorkState) {
        LogUtils.e("onNetWorkStateChange >>> :" + netWorkState.name());
        if (netWorkState.NONE!=netWorkState){
            if (mIsRegister && iSipAidlInterface!=null){
                LogUtils.e(MorangeVoip.class.getSimpleName()+"onNetWorkStateChange >>> : unRegisterSip" );
                try {
                    iSipAidlInterface.unRegisterSip();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else {
                bindSipService();
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case SipCode.REGISTER_SIP:
                    mHandler.removeMessages(SipCode.REGISTER_SIP);
                    if (iSipAidlInterface!=null){
                        try {
                            iSipAidlInterface.registerSip(sipAccount,sipPassword,sipServcer);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
            return false;
        }
    });

    public void registerPush() {
        String deviceToken = SharedPreferencesUtil.getString(mApplication,SharedPreferencesUtil.UMENG_TOKEN,"");
        String mobile = SharedPreferencesUtil.getString(mApplication,SharedPreferencesUtil.MOBILE,"");
        Map<String,String> params = new HashMap<>();
        params.put("appPackageName","com.moredian.morange");
        params.put("deviceToken",deviceToken);
        params.put("channelVendor",android.os.Build.BRAND);
        params.put("operateSystemType","android");
        params.put("mobile",mobile);
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                RegisterPushDiretor.postRequest(BuildConfig.APP_HOST+"community/push/deviceToken/save",params);
            }
        });
    }

    public void unRegisterPush() {
        String mobile = SharedPreferencesUtil.getString(mApplication,SharedPreferencesUtil.MOBILE,"");
        Map<String,String> params = new HashMap<>();
        params.put("mobile",mobile);
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                RegisterPushDiretor.postRequest(BuildConfig.APP_HOST+"community/push/deviceToken/cancel",params);
            }
        });
    }
}
