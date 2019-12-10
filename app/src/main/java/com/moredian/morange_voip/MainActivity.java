package com.moredian.morange_voip;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.moredian.factory.MorangeVoip;
import com.moredian.utils.SharedPreferencesUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.tv_token);
        String token = SharedPreferencesUtil.getString(this,SharedPreferencesUtil.UMENG_TOKEN,"获取umengToken失败");
        textView.setText(token);
        MorangeVoip.getInstance(getApplication()).initSipService("192.168.7.235","mobile_mcsapp18758226884","AE40EF0374B06C92DB991DB5C7B52CA2","18758226884");
        MorangeVoip.getInstance(getApplication()).registerAcceptCall(true);
        MorangeVoip.getInstance(getApplication()).registerSip();
        MorangeVoip.getInstance(getApplication()).registerPush();
    }


}
