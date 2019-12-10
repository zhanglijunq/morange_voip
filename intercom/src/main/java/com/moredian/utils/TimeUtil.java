package com.moredian.utils;

import java.util.Locale;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2018/11/07
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class TimeUtil {
    public static String formatSeconds(int seconds){
        String standardTime;
        if (seconds <= 0){
            standardTime = "00:00";
        } else if (seconds < 60) {
            standardTime = String.format(Locale.getDefault(), "00:%02d", seconds % 60);
        } else if (seconds < 3600) {
            standardTime = String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
        } else {
            standardTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", seconds / 3600, seconds % 3600 / 60, seconds % 60);
        }
        return standardTime;
    }

}
