package com.moredian.utils;

import com.google.gson.Gson;

/**
 * Created by zk on 2018/9/5.
 */

public class GsonUtils {
    private static Gson gson = new Gson();
    public static Gson getGson(){
        return gson;
    }
}
