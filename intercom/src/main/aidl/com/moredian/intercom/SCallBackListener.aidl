// SCallBackListener.aidl
package com.moredian.intercom;
import com.moredian.bean.CallHistoryBean;
// Declare any non-default types here with import statements

interface SCallBackListener {
    void callBack(int code,in CallHistoryBean bean);
    void heartBeat(int code,boolean isRegister);
    void doorStatusCallBack(boolean isOpen);
}
