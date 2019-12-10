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
public class OpenDoorEvent {
    private boolean isOpen;
    public OpenDoorEvent(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }
}
