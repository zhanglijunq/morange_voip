package com.moredian.event;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2018/11/09
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class CallTimeEvent {
    private final int callTime;

    public CallTimeEvent(int callTime) {
        this.callTime = callTime;
    }

    public int getCallTime() {
        return callTime;
    }
}
