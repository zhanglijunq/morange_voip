package com.moredian.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.support.annotation.Nullable;

import com.moredian.bean.SipMessage;
import com.moredian.bean.UserInfoBean;
import com.moredian.common.CommunicateConstants;
import com.moredian.common.RtcServerConfiguration;
import com.moredian.common.SipCode;
import com.moredian.event.CallStatusEvent;
import com.moredian.event.CallTimeEvent;
import com.moredian.event.OpenDoorEvent;
import com.moredian.intercom.R;
import com.moredian.rtcengine.NativeRTC;
import com.moredian.utils.GsonUtils;
import com.moredian.utils.LogUtils;
import com.moredian.utils.SoundPoolUtil;
import com.moredian.utils.TimeUtil;
import com.moredian.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 拨号等待和通话页面
 *
 * @author zk
 * @date 2018/8/29
 */

public class CommunicateActivity extends AppCompatActivity implements View.OnClickListener,SurfaceHolder.Callback {
    private static final String STATUS = "currentStatus";
    private static final String REMOTE_IP = "remoteIp";

    private int currentStatus;
    private SurfaceViewRenderer localSurfaceView, remoteSurfaceView;
    private TextView nameTv;
    private AppCompatButton cancelAcb;
    private LinearLayout callingLl,callFailLl;
    private ImageView callAgainIv;
    private TextView statusTv;
    private TextView duringTv;
    private TextView callEndTv;
    private LinearLayout switchAudioLl;
    private AppCompatButton switchAudioAcb;
    private AppCompatButton turnOffAcb;

    /**
     * 接听方
     */
    private TextView incomeNameTv;
    private TextView incomeStatusTv;
    private TextView incomeCallEndTv;
    private AppCompatButton refuseAcb,answerAcb;
    private LinearLayout incomeSwitchAudioLl;
    private LinearLayout openDoorLl;
    private AppCompatButton openDoorAcb;
    private EglBase eglBase;
    private long localRender, remoteRender;
    private View incomingLayout;
    private AudioManager audioManager;
    /**
     * 是否正在通话
     */
    private int currentStreamId;

    private boolean isVideoCall;
    private boolean isAcceptCall;
    private boolean isCallOut;
    private Timer mTimer;
    private int callTime=0;
    private UserInfoBean mUserInfoBean;
    private boolean isEnding = false;
    private long openTime;

    /**
     * @param context
     * @param status     拨号界面还是通话界面
     */
    public static void getInstance(Context context, int status,UserInfoBean userInfoBean,String strRemoteIP) {
        Intent intent = new Intent(context, CommunicateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.putExtra(STATUS, status);
        intent.putExtra(REMOTE_IP, strRemoteIP);
        intent.putExtra(UserInfoBean.class.getSimpleName(),userInfoBean);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_communicate);
        EventBus.getDefault().register(this);
        initView();
        initData();
        initListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void initView() {
        incomingLayout = findViewById(R.id.layout_incoming);
        incomeNameTv = findViewById(R.id.tv_income_name);
        incomeCallEndTv = findViewById(R.id.tv_income_call_end);
        incomeStatusTv = findViewById(R.id.tv_income_status);
        incomeSwitchAudioLl = findViewById(R.id.ll_income_switch_audio);
        answerAcb = findViewById(R.id.acb_answer);
        refuseAcb = findViewById(R.id.acb_refuse);


        localSurfaceView = findViewById(R.id.surface_view_local);
        localSurfaceView.getHolder().addCallback(this);
        remoteSurfaceView = findViewById(R.id.surface_view_remote);
        remoteSurfaceView.getHolder().addCallback(this);

        nameTv = findViewById(R.id.tv_name);
        openDoorLl = findViewById(R.id.ll_open_door);
        openDoorAcb = findViewById(R.id.acb_open_door);
        turnOffAcb = findViewById(R.id.acb_turn_off);
        cancelAcb = findViewById(R.id.acb_cancel);
        callingLl = findViewById(R.id.ll_calling);
        callFailLl = findViewById(R.id.ll_call_fail);
        callAgainIv = findViewById(R.id.iv_call_again);
        statusTv = findViewById(R.id.tv_status);
        duringTv = findViewById(R.id.tv_during);
        callEndTv = findViewById(R.id.tv_call_end);
        switchAudioLl = findViewById(R.id.ll_switch_audio);
        switchAudioAcb = findViewById(R.id.acb_switch_audio);
    }

    private void initData() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        getIntentData();
        callAgainIv.setBackgroundResource(isVideoCall?R.drawable.icon_vido_float:R.drawable.icon_voice_call);
//        NativeRTC.setEventListener(this);
//        NativeRTC.register("moditest04","192.168.7.235", "123456") ;
        eglBase = EglBase.create();
        localSurfaceView.init(eglBase.getEglBaseContext(), null);
        localSurfaceView.setMirror(false);
        //两个surfaceview重叠
        localSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        localSurfaceView.setZOrderOnTop(true);
        //设置不遮挡挂断按钮
        localSurfaceView.setZOrderMediaOverlay(true);
        remoteSurfaceView.init(eglBase.getEglBaseContext(), null);
        remoteSurfaceView.setMirror(false);
        //初始化本地surface

        isCallOut = SipCode.STATUS_DIAL == currentStatus;
        incomingLayout.setVisibility(isCallOut ? View.GONE : View.VISIBLE);

        if (isCallOut) {
            currentStreamId=1;
            makeCall();
        } else {
            currentStreamId=2;
            initAnswerCall();
        }
        SoundPoolUtil.getInstance().play(currentStreamId,-1);
        //默认使用听筒
        audioManager.setSpeakerphoneOn(true);
//        NativeRTC.setPreCallParams("X-modi-info", "{orgid:\"123456\",suborgid:\"234\",fromname:\"1��1��Ԫ\",fromnumber:\"1#1\"}");
    }



    private void initAnswerCall() {
        statusTv.setVisibility(View.GONE);
        if (localRender != 0) {
            LogUtils.e("initAnswerCall destroy localRender");
            NativeRTC.destroyRender(localRender);
        }
        localRender = NativeRTC.createRender(localSurfaceView);
        LogUtils.e("create localRender");
        NativeRTC.setPreviewRender(localRender);
        //初始化远端
        if (remoteRender != 0) {
            LogUtils.e("initAnswerCall destroy remoteRender");
            NativeRTC.destroyRender(remoteRender);
        }
        remoteRender = NativeRTC.createRender(remoteSurfaceView);
        LogUtils.e("create remoteRender");
        NativeRTC.setStreamRender(remoteRender, 0);
        nameTv.setText(mUserInfoBean.getFromName()+mUserInfoBean.getFromNum());
        incomeNameTv.setText(mUserInfoBean.getFromName()+mUserInfoBean.getFromNum());
        incomeStatusTv.setText(isVideoCall?getString(R.string.invite_you_accept_video_call):getString(R.string.invite_you_accept_audio_call));
        incomeSwitchAudioLl.setVisibility(isVideoCall?View.VISIBLE:View.GONE);
        refuseAcb.setBackgroundResource(isVideoCall?R.drawable.selector_video_turn_off:R.drawable.selector_turn_off);
        answerAcb.setBackgroundResource(isVideoCall?R.drawable.selector_video_call:R.drawable.selector_audio_call);
        turnOffAcb.setBackgroundResource(isVideoCall?R.drawable.selector_video_turn_off:R.drawable.selector_turn_off);
        if (2==mUserInfoBean.getCallDeviceType()){
            openDoorLl.setVisibility(View.VISIBLE);
        }
    }

    private void makeCall() {
        nameTv.setText(mUserInfoBean.getToName()+mUserInfoBean.getToNum());
        statusTv.setText(isVideoCall?getString(R.string.waiting_for_video_accept):getString(R.string.waiting_for_voice_accept));
        if (isVideoCall){
            if (localRender != 0) {
                NativeRTC.destroyRender(localRender);
            }
            localRender = NativeRTC.createRender(localSurfaceView);
            NativeRTC.setPreviewRender(localRender);
            //初始化远端
            if (remoteRender != 0) {
                NativeRTC.destroyRender(remoteRender);
            }
            remoteRender = NativeRTC.createRender(remoteSurfaceView);
            NativeRTC.setStreamRender(remoteRender, 0);
            localSurfaceView.setVisibility(View.VISIBLE);
            switchAudioLl.setVisibility(View.VISIBLE);
        }
        new Thread(() -> {
            String callStr = GsonUtils.getGson().toJson(mUserInfoBean);
            LogUtils.e(callStr);
            NativeRTC.setPreCallParams(NativeRTC.kUserDefineCallKey,callStr);
            int a = NativeRTC.startCall("remote", RtcServerConfiguration.getInstance().getRtcServerAddress(), isVideoCall,isVideoCall);
            LogUtils.e("拨号情况:" + (a == 0 ? "正常" : "异常")+a);
        }).start();
        callingLl.setVisibility(View.VISIBLE);
        callFailLl.setVisibility(View.GONE);
        mHandler.sendEmptyMessageDelayed(SipCode.IS_ACCEPT,20*1000);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initListener() {
        turnOffAcb.setOnClickListener(this);
        cancelAcb.setOnClickListener(this);
        callAgainIv.setOnClickListener(this);
        switchAudioAcb.setOnClickListener(this);
        refuseAcb.setOnClickListener(this);
        answerAcb.setOnClickListener(this);
        incomeSwitchAudioLl.setOnClickListener(this);
        openDoorAcb.setOnClickListener(v -> {
            if (0!=openTime && System.currentTimeMillis()-openTime<2000){
                ToastUtils.getInstance().showToast("请不要短时间多次开门哦");
                return;
            }
            openTime = System.currentTimeMillis();
            SipMessage sipMessage = new SipMessage();
            sipMessage.setMessageCode(SipCode.OPEN_DOOR);
            NativeRTC.sendMessage(GsonUtils.getGson().toJson(sipMessage));
            LogUtils.e("点击开门");
        });
//        voiceInputButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                //true扬声器
//                LogUtils.e("is" + isChecked);
//
////                audioManager.setMode(isChecked?AudioManager.MODE_NORMAL:AudioManager.MODE_IN_CALL);
//                if (!isChecked) {
//                    audioManager.setSpeakerphoneOn(true);
//                } else {
//                    audioManager.setSpeakerphoneOn(false);//关闭扬声器
//                    audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
//                    setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
//                    //把声音设定成Earpiece（听筒）出来，设定为正在通话中
//                    audioManager.setMode(AudioManager.MODE_IN_CALL);
//                }
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        eglBase.release();
        if (localRender != 0) {
            LogUtils.e("onDestroy destroy localRender");
            NativeRTC.destroyRender(localRender);
            localRender=0;
            NativeRTC.setPreviewRender(0);
        }
        if (remoteRender != 0) {
            LogUtils.e("onDestroy destroy remoteRender");
            NativeRTC.destroyRender(remoteRender);
            remoteRender=0;
            NativeRTC.setStreamRender(0,0);
        }
        SoundPoolUtil.getInstance().pause();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        LogUtils.e("surface初始化");
//        if (holder.equals(localSurfaceView.getHolder())){
//            LogUtils.e("本地surfaceViewchu");
//            localSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
//            localSurfaceView.setZOrderOnTop(true);
//        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.acb_turn_off) {
            NativeRTC.stopCall();
            showCallEndView();
        }else if (i==R.id.acb_cancel){
            NativeRTC.stopCall();
            showCallEndView();
        }
        else if (i == R.id.acb_answer) {
//            ringMediaPlayer.stop();
            SoundPoolUtil.getInstance().stop();
            incomingLayout.setVisibility(View.GONE);
            NativeRTC.answerCall(true);
            switchPager();
        } else if (i == R.id.acb_refuse) {
            NativeRTC.rejectCall();
        }
//        else if (i == R.id.btn_open_door) {
//            LogUtils.e("调用开门方法");
//            NativeRTC.sendMessage(CommunicateConstants.OPEN_DOOR);
//        }
        else if (i==R.id.iv_call_again ) {
            makeCall();
        }else if (i==R.id.acb_switch_audio ) {
            LogUtils.e("切换语音通话");
            isVideoCall = false;
            switchAudioLl.setVisibility(View.GONE);
            remoteSurfaceView.setVisibility(View.GONE);
            localSurfaceView.setVisibility(View.GONE);
            if (!isAcceptCall){
                statusTv.setText(getString(R.string.waiting_for_voice_accept));
            }else {
                statusTv.setText(getString(R.string.is_during_call));
            }
            NativeRTC.setMuteVideo(false);
            SipMessage sipMessage = new SipMessage();
            sipMessage.setMessageCode(SipCode.SWITCH_AUDIO);
            NativeRTC.sendMessage(GsonUtils.getGson().toJson(sipMessage));
        }else if (i==R.id.ll_income_switch_audio) {
            LogUtils.e("切换语音通话");
//            ringMediaPlayer.stop();
            SoundPoolUtil.getInstance().stop();
            isVideoCall = false;
            incomingLayout.setVisibility(View.GONE);
            NativeRTC.setMuteVideo(false);
            NativeRTC.answerCall(false);
            SipMessage sipMessage = new SipMessage();
            sipMessage.setMessageCode(SipCode.SWITCH_AUDIO);
            NativeRTC.sendMessage(GsonUtils.getGson().toJson(sipMessage));
        }
    }

    private void switchPager() {
        SoundPoolUtil.getInstance().stop();
        isAcceptCall = true;
        if (View.VISIBLE!=callingLl.getVisibility()){
            callingLl.setVisibility(View.VISIBLE);
            callFailLl.setVisibility(View.GONE);
        }
        switchAudioLl.setVisibility(isVideoCall?View.VISIBLE:View.GONE);
        remoteSurfaceView.setVisibility(isVideoCall?View.VISIBLE:View.GONE);
        localSurfaceView.setVisibility(isVideoCall?View.VISIBLE:View.GONE);
        duringTv.setVisibility(View.VISIBLE);
        statusTv.setVisibility(View.VISIBLE);
        statusTv.setText(getResources().getString(R.string.is_during_call));
        startTimer();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void XXX(CallStatusEvent event) {
        int status = event.getValue();
        int code = event.getCode();
        LogUtils.e("" + status+"----------"+code);
        if (CallStatusEvent.CALL_START == event.getCode()){
            if (CommunicateConstants.OK==status) {
                SoundPoolUtil.getInstance().stop();
                isAcceptCall = true;
                if (View.VISIBLE!=callingLl.getVisibility()){
                    callingLl.setVisibility(View.VISIBLE);
                    callFailLl.setVisibility(View.GONE);
                }
                switchAudioLl.setVisibility(isVideoCall?View.VISIBLE:View.GONE);
                remoteSurfaceView.setVisibility(isVideoCall?View.VISIBLE:View.GONE);
                localSurfaceView.setVisibility(isVideoCall?View.VISIBLE:View.GONE);
                duringTv.setVisibility(View.VISIBLE);
                statusTv.setVisibility(View.VISIBLE);
                statusTv.setText(getResources().getString(R.string.is_during_call));
                startTimer();
            }else if (CommunicateConstants.TRING == status){
                LogUtils.e("正在尝试建立通话");
            }else if (CommunicateConstants.REMOTE_REFUSE == status){
                //对方挂断
                if (currentStatus == SipCode.STATUS_DIAL) {
                    ToastUtils.getInstance().showToast("对方已挂断");
                }
                showCallEndView();
//                callEndTv.setVisibility(View.VISIBLE);
//                incomeCallEndTv.setVisibility(View.VISIBLE);
//                callEndTv.setText(getString(R.string.call_end));
//                incomeCallEndTv.setText(getString(R.string.call_end));
//                mHandler.sendEmptyMessageDelayed(SipCode.FINISH_ACTIVITY,2*1000);
            }else if (CommunicateConstants.TEMPORARY_UNAVAILABLE == status){
                SoundPoolUtil.getInstance().stop();
                if (currentStatus == SipCode.STATUS_DIAL) {
                    ToastUtils.getInstance().showToast("对方注册不在线");
                }
                showCallFailView();
            }else if (CommunicateConstants.REQUEST_TERMINATED == status){
                LogUtils.e("停止拨号请求");
                showCallEndView();
            }else if (CommunicateConstants.REMOTE_BUSY == status){
                ToastUtils.getInstance().showToast("对方正在通话中");
                showCallFailView();
            }else if (CommunicateConstants.SWITCH_AUDIO == status){
                LogUtils.e("切换语音："+status);
                isVideoCall = false;
                if (isAcceptCall){
                    switchAudioLl.setVisibility(View.GONE);
                    remoteSurfaceView.setVisibility(View.GONE);
                    localSurfaceView.setVisibility(View.GONE);
                    NativeRTC.setMuteVideo(false);
                }else {
                    if (!isCallOut){
                        incomeStatusTv.setText(getString(R.string.invite_you_accept_audio_call));
                        answerAcb.setBackgroundResource(R.drawable.icon_voice_call);
                        incomeSwitchAudioLl.setVisibility(View.INVISIBLE);
                    }
                }
            }
            else {
                LogUtils.e("未知错误："+status);
                ToastUtils.getInstance().showToast("未知错误");
                showCallFailView();
            }
        }else if (CallStatusEvent.CALL_STOP == event.getCode()){
            if (isEnding){
                return;
            }
            SoundPoolUtil.getInstance().stop();
            SoundPoolUtil.getInstance().play(3,0);
            LogUtils.e("通话结束" + status);
            mTimer.cancel();
            mTimer=null;
            callEndTv.setVisibility(View.VISIBLE);
            incomeCallEndTv.setVisibility(View.VISIBLE);
            callEndTv.setText(getString(R.string.call_end));
            incomeCallEndTv.setText(getString(R.string.call_end));
            mHandler.sendEmptyMessageDelayed(SipCode.FINISH_ACTIVITY,2*1000);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOpenDoorEvent(OpenDoorEvent event) {
        if (event.isOpen()){
            ToastUtils.getInstance().showLayoutToast(this,getString(R.string.open_door_success));
        }
    }

    private void startTimer() {
        if (mTimer==null){
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.sendEmptyMessage(10086);
                }
            },0,1000);
        }
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            int what = msg.what;
            if (10086==what){
                callEndTv.setVisibility(View.GONE);
                callTime++;
                duringTv.setText(TimeUtil.formatSeconds(callTime));
            }else if (SipCode.FINISH_ACTIVITY == what){
                isEnding = false;
                EventBus.getDefault().post(new CallTimeEvent(callTime));
                finish();
            }else if (SipCode.IS_ACCEPT==what){
                mHandler.removeMessages(SipCode.IS_ACCEPT);
                if (!isAcceptCall){
                    callEndTv.setVisibility(View.VISIBLE);
                    callEndTv.setText(getString(R.string.not_accept_long_time));
                }
            }
            return false;
        }
    });

    /**
     * 展示通话失败画面
     */
    private void showCallFailView() {
        mHandler.removeMessages(SipCode.IS_ACCEPT);
        statusTv.setText(getString(R.string.call_fail));
        callEndTv.setVisibility(View.GONE);
        localSurfaceView.setVisibility(View.GONE);
        callingLl.setVisibility(View.GONE);
        callFailLl.setVisibility(View.VISIBLE);
    }

    /**
     * 展示通话结束画面
     */
    private void showCallEndView() {
        if (isEnding){
            return;
        }
        isEnding = true;
        LogUtils.e("showCallEndView");
        if (mTimer!=null){
            mTimer.cancel();
            mTimer=null;
        }
        SoundPoolUtil.getInstance().stop();
        SoundPoolUtil.getInstance().play(3,0);
        if (View.GONE==callEndTv.getVisibility()){
            callEndTv.setVisibility(View.VISIBLE);
        }
        if (View.GONE==incomeCallEndTv.getVisibility()){
            incomeCallEndTv.setVisibility(View.VISIBLE);
        }
        callEndTv.setText(getString(R.string.call_end));
        incomeCallEndTv.setText(getString(R.string.call_end));
        mHandler.sendEmptyMessageDelayed(SipCode.FINISH_ACTIVITY,2*1000);
    }

    private void getIntentData() {
        if (getIntent() != null) {
            Intent intent = getIntent();
            currentStatus = intent.getIntExtra(STATUS, SipCode.STATUS_DIAL);
            mUserInfoBean = intent.getParcelableExtra(UserInfoBean.class.getSimpleName());
            isVideoCall = mUserInfoBean.getVideoFlag()==0;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }
}
