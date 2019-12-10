package com.moredian.common;

/**
 * 一般100或100以上的是状态变化，是正常的，
 *  * 200或200以上的返回正常状态，比如注册成功，呼叫建立都是返回200,
 *  * 300或300以上，有一些warning吧，
 *  * 400以上的表示内部有问题，应该中断通话或者查找问题，
 *  * 500以上的表示服务器有问题，
 *  * 600以上的对方拒绝或者对方忙碌等原因引起通话中断
 *
 * @author zk
 * @date 2018/8/31
 */

public interface CommunicateConstants {
    //以下是呼叫的时候返回
    /**
     * 正在尝试
     */
    int TRING = 100;
    /**
     * 正在响铃
     */
    int RING = 180;
    /**
     *呼叫正在转接
     */
    int CALL_FORWARDING =  181;
    /**
     *正在排队
     */
    int QUEUE = 182;
    /**
     *正在处理
     */
    int PROGRESS = 183;

    /**
     * ok
     */
    int OK = 200;
    /**
     * 已经接受
     */
    int ACCEPT = 202;
    /**
     *多重选择
     */
    int MULTIPLE =300;
    /**
     * 永久移动
     */
    int MOVE_PERMANENTLY =301;
    /**
     * 临时移动
     */
    int MOVE_TEMPORARY =302;
    /**
     * 用户代理
     */
    int USE_PROXY = 305;
    /**
     * 用户代理
     */
    int ALTERNATIVE_SERVICE = 380;
    /**
     * 错误请求，一般是信令出现语法错误
     */
    int ERROR = 400;
    /**
     * 一般是表示呼叫的时候，对方注册不在线
     */
    int TEMPORARY_UNAVAILABLE = 480;
    /**
     * 呼叫对方不存在， 也可能是对方注册不在线
     */
    int NEGATION = 481;
    /**
     * 对方忙（占线）
     */
    int REMOTE_BUSY =486;
    /**
     * 请求终止
     */
    int REQUEST_TERMINATED = 487;
//    int
    /**
     * 都忙
     */
    int BOTH_BUSY =600;
    /**
     * 对方拒绝
     */
    int REMOTE_REFUSE =603;
    /**
     * 切换语音
     */
    int SWITCH_AUDIO =604;
}
