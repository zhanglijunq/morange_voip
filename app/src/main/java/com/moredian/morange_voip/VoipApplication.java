package com.moredian.morange_voip;

import android.app.Application;

import com.moredian.factory.MorangeVoip;
import com.moredian.factory.bean.CustomMessage;
import com.moredian.factory.interfaces.PushCallBack;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2019/12/09
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class VoipApplication extends Application implements PushCallBack {
    @Override
    public void onCreate() {
        super.onCreate();
        initVoipPush();
    }

    private void initVoipPush() {
        MorangeVoip morangeVoip = MorangeVoip.getInstance(this);
        morangeVoip.setUmeng("5dee1629570df305f00000a8","f1a8ad0dcf2cb3fc99499ee8c8b1ff68","voip");
        morangeVoip.setXiaomi("","");
        morangeVoip.setHw("","");
        morangeVoip.setPushCallBack(this);
        morangeVoip.init();
    }

    @Override
    public void onRegisterCallBack(boolean b, String s) {

    }

    @Override
    public void dealWithCustomAction(CustomMessage customMessage) {

    }
}
