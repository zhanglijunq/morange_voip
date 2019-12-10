package com.moredian.morange_voip;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.moredian.morange_voip.bean.Body;
import com.moredian.morange_voip.bean.CustomPushMessage;
import com.moredian.morange_voip.bean.PushActionConstant;
import com.moredian.morange_voip.bean.PushMessage;
import com.moredian.utils.GsonUtils;
import com.moredian.utils.LogUtils;
import com.umeng.message.UmengNotifyClickActivity;

import org.android.agoo.common.AgooConstants;

public class MipushTestActivity extends UmengNotifyClickActivity {

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_mipush);
    }
    @Override
    public void onMessage(Intent intent) {
        super.onMessage(intent);  //此方法必须调用，否则无法统计打开数
        String message = intent.getStringExtra(AgooConstants.MESSAGE_BODY);
        LogUtils.e(message);
        PushMessage pushMessage = GsonUtils.getGson().fromJson(message,PushMessage.class);
        if (pushMessage!=null && pushMessage.body!=null){
            Body body = pushMessage.body;
            CustomPushMessage customMsg = body.custom;
            if (customMsg!=null){
                int value = customMsg.customFieldType;
                switch (value){
                    case PushActionConstant.OPEN_MAIN_ACTIVITY:
                        String className = (((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1).get(0)).topActivity.getClassName();
                        LogUtils.e(className);
                        if (className.equals("com.moredian.morange_voip.MipushTestActivity")){
                            Intent mainIntent = new Intent();
                            mainIntent.setClass(this,MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mainIntent);
                        }else if (!className.contains("com.moredian")){
                            SystemHelper.setTopApp(this);
                        }
                        break;
                }
            }
        }
        finish();
    }
}