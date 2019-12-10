package com.moredian.bean;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2019/04/30
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class SipMessage<T> {
    private int messageCode;
    private T messageContent;

    public int getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(int messageCode) {
        this.messageCode = messageCode;
    }

    public T getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(T messageContent) {
        this.messageContent = messageContent;
    }
}
