package com.moredian.utils;

import android.util.Log;

/**
 * Created by zk on 2017/10/12.
 */

public class LogUtils {
    private static final String NULL = "null";
    public static boolean enable = true;
    private static final String WARN = "warn";
    private static final String DEBUG = "debug";
    private static final String ERROR = "error";
    private static final String INFO = "info";
    private static final String ALL = "all";
    private static String lev = ALL;

    public static void d(Object object) {
        if (!enable) return;
        if (object == null) object = NULL;
        if (DEBUG.equals(lev) || ALL.equals(lev))
            Log.d(getCallM(DEBUG) , object.toString());
    }

    public static void e(Object object) {
        if (!enable) return;
        if (object == null) object = NULL;
        if (ERROR.equals(lev) || ALL.equals(lev))
            Log.e(getCallM(ERROR), object.toString());
    }

    public static void e(Object object, Throwable throwable) {
        if (!enable) return;
        if (object == null) object = NULL;
        if (ERROR.equals(lev) || ALL.equals(lev))
            Log.e(getCallM(ERROR) , object.toString(), throwable);
    }

    public static void i(Object object) {
        if (!enable) return;
        if (object == null) object = NULL;
        if (INFO.equals(lev) || ALL.equals(lev))
            Log.i(getCallM(INFO), object.toString());

    }

    public static void i(Object object, Throwable throwable) {
        if (!enable) return;
        if (object == null) object = NULL;
        if (INFO.equals(lev) || ALL.equals(lev))
            Log.i(getCallM(INFO), object.toString(), throwable);
    }

    public static void w(Object object) {
        if (!enable) return;
        if (object == null) object = NULL;
        if (WARN.equals(lev) || ALL.equals(lev))
            Log.w(getCallM(WARN), object.toString());
    }

    private static String getCallM(String s) {
        StackTraceElement element = Thread.currentThread().getStackTrace()[4];
        StringBuilder buf = new StringBuilder();

        buf.append(element.getMethodName());

        if (element.isNativeMethod()) {
            buf.append("(Native Method)");
        } else {
            String fName = element.getFileName();

            if (fName == null) {
                buf.append("(Unknown Source)");
            } else {
                int lineNum = element.getLineNumber();

                buf.append('(');
                buf.append(fName);
                if (lineNum >= 0) {
                    buf.append(':');
                    buf.append(lineNum);
                }
                buf.append(')');
            }
        }

        return buf.toString();
    }
}
