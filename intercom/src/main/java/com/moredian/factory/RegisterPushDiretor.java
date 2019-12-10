package com.moredian.factory;

import android.support.annotation.NonNull;

import com.moredian.utils.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2019/12/05
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class RegisterPushDiretor {

    public static void postRequest(String urlStr, @NonNull Map<String, String> params) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            //获取输出流向里面写入数据一定要在建立连接之前，否则会抛出异常
            OutputStream outputStream = httpURLConnection.getOutputStream();
            PrintWriter printWriter = new PrintWriter(outputStream);
            String paramsString = getPostJsonParams(params);
            LogUtils.e("params:" + paramsString);
            printWriter.write(paramsString);
            printWriter.close();
            outputStream.close();
            httpURLConnection.connect();
            if (HttpURLConnection.HTTP_OK == httpURLConnection.getResponseCode()) {
                //开始发起请求
                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                LogUtils.e("服务端返回数据：" + stringBuilder.toString());
                //依次关闭流对象
                bufferedReader.close();
                inputStreamReader.close();
                inputStream.close();
            } else {
                LogUtils.e("服务端报错");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将Map集合中的数据转换成post提交的格式
     *
     * @param params
     * @return
     */
    private static String getPostParams(Map<String, String> params) {
        String paramsString = null;
        if (params != null) {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                String key = next.getKey();
                String value = next.getValue();
                if (paramsString == null) {
                    paramsString = key + "=" + value;
                } else {
                    paramsString = "&" + key + "=" + value;
                }
            }
        }
        return paramsString;
    }

    /**
     * 将Map集合中的数据转换成post提交的格式
     *
     * @param params
     * @return
     */
    private static String getPostJsonParams(Map<String, String> params) {
        String paramsString = null;
        if (params != null) {
            Set<Map.Entry<String, String>> entrySet = params.entrySet();
            Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
            JSONObject object = new JSONObject();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                String key = next.getKey();
                String value = next.getValue();
                try {
                    object.put(key,value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            paramsString=object.toString();
        }
        return paramsString;
    }
}
