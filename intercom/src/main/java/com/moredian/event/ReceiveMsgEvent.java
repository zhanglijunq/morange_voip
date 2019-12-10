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
public class ReceiveMsgEvent {
    private String msg;

    public ReceiveMsgEvent(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
