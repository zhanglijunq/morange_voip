package com.moredian.factory.interfaces;

import com.moredian.factory.bean.CustomMessage;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2019/12/05
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public interface PushCallBack {
    void onRegisterCallBack(boolean isSuccess, String deviceToken);
    void dealWithCustomAction(CustomMessage customMessage);
}
