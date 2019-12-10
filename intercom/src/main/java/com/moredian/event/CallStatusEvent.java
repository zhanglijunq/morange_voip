package com.moredian.event;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2018/10/18
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class CallStatusEvent {
    public static final int CALL_START=0,CALL_STOP=1;
    private int code;
    private int value;

    public CallStatusEvent(int code, int value) {
        this.code = code;
        this.value = value;
    }

    public int getCode() {
        return code;
    }

    public int getValue() {
        return value;
    }
}
