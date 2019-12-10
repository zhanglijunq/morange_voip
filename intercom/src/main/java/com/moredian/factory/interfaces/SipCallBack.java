package com.moredian.factory.interfaces;

import com.moredian.bean.CallHistoryBean;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2019/12/05
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public interface SipCallBack {
    void onVoipEndCallBack(CallHistoryBean bean);
}
