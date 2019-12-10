package com.moredian.common;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2018/11/07
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class SipCode {
    public static final int HEART_BEAT=1000;
    public static final int BACK_HISTORY=1001;
    public static final int IS_ACCEPT=1002;
    public static final int FINISH_ACTIVITY=1003;
    public static final int SWITCH_AUDIO = 1006;
    public static final int OPEN_DOOR = 1007;
    public static final int OPEN_DOOR_STATUS = 1008;
    public static final int DOOR_STATUS=10086;
    public static final int BEGIN_LOOP=1009;
    public static final int CHECK_INCOME_ALERT=1010;

    /**
     * 拨号方
     */
    public static final int STATUS_DIAL = 1;
    /**
     * 接听方
     */
    public static final int STATUS_ANSWER = 2;

    /**
     * 视频拨打
     */
    public static final int CALL_AUDIO = 1;
    /**
     * 语音拨打
     */
    public static final int CALL_VIDEO = 2;

    public static final int REGISTER_SIP = 1004;
}
