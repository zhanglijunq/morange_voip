package com.moredian.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.moredian.intercom.R;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2018/10/17
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class ToastUtils{

    private Context mContext; // 上下文对象

    private ToastUtils(){} // 私有化构造

    private static final class Helper {
        static final ToastUtils INSTANCE = new ToastUtils();
    }

    public static ToastUtils getInstance() {
        return Helper.INSTANCE;
    }

    public static void init(@NonNull Context context){
        Helper.INSTANCE.mContext = context;
    }

    public void showToast(@StringRes int strResID) {
        if (mContext == null) {
            throw new RuntimeException("Please init the Context before showToast");
        }
        showToast(mContext.getResources().getText(strResID));
    }

    public void showToast(CharSequence str) { // 根据字符串弹Toast
        if (mContext == null) {
            throw new RuntimeException("Please init the Context before showToast");
        }
        Toast toast = Toast.makeText(mContext,null,Toast.LENGTH_SHORT);
        toast.setText(str);
        toast.show();
    }

    public static void showLayoutToast(Context context, String message) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_toast, null);
        TextView contentTv = (TextView) view.findViewById(R.id.tv_content);
        contentTv.setText(message);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }
}
