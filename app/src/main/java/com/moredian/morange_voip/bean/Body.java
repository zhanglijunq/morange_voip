package com.moredian.morange_voip.bean;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2019/11/14
 *     desc   :
 *     version: 1.0
 * </pre>
 *
 * "body": {
 *         "after_open": "go_custom",
 *                 "play_lights": "true",
 *                 "ticker": "22222",
 *                 "play_vibrate": "true",
 *                 "custom": "com.moredian.morange.MainActivity",
 *                 "text": "333333",
 *                 "title": "22222",
 *                 "play_sound": "true"
 *     },
 */
public class Body {
    public String after_open;
    public String play_lights;
    public String ticker;
    public String play_vibrate;
    public CustomPushMessage custom;
    public String text;
    public String title;
    public String play_sound;
}
