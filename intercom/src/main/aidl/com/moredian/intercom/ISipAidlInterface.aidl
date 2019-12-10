// ISipAidlInterface.aidl
package com.moredian.intercom;
import com.moredian.intercom.SCallBackListener;
// Declare any non-default types here with import statements

interface ISipAidlInterface {
    void registerListener(SCallBackListener listener);
    void unregisterListener(SCallBackListener listener);
    void init(String domain);
    void registerSip(String account,String password,String domain);
    void unRegisterSip();
    void registerMainProcess(String processName,int mainPid);
    void registerNetWrokStatus(boolean isConnect);
    void registerAcceptConditions(boolean isAcceptCall);
    void call(long orgId,long roomConstructId,String mobile,String fromName,String fromNum,String toName,String toNum,boolean enable_video, boolean enable_LocalVideo,boolean isNetConnect);
}
