package com.moredian.common;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2018/10/18
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RtcServerConfiguration {
    private String rtcServerAddress;
    private String rtcAccount;
    private String rtcPassword;

    //默认baseUrl的单例
    private static class SingletonHolder {
        private static RtcServerConfiguration INSTANCE = new RtcServerConfiguration();
    }

    public static RtcServerConfiguration getInstance() {
        return RtcServerConfiguration.SingletonHolder.INSTANCE;
    }

    public void setRtcAccount(String account) {
        this.rtcAccount = account;
    }

    public void setRtcAddress(String domain) {
        this.rtcServerAddress = domain;
    }

    public void setRtcPassword(String password) {
        this.rtcPassword = password;
    }

    public String getRtcServerAddress() {
        return rtcServerAddress;
    }

    public String getRtcAccount() {
        return rtcAccount;
    }

    public String getRtcPassword() {
        return rtcPassword;
    }
}
