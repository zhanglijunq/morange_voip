package com.moredian.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.ArrayMap;

import com.moredian.intercom.R;

/**
 * <pre>
 *     author : zhanglj
 *     e-mail : zhanglj@moredian.com
 *     time   : 2018/10/17
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class SoundPoolUtil {
    private Context mContext;
    private ArrayMap<Integer,Integer> soundMaps = new ArrayMap<>();
    private SoundPool soundPool;
    private float volume;
    private int playId;

    private SoundPoolUtil(){

    } // 私有化构造

    private static final class Helper {
        static final SoundPoolUtil INSTANCE = new SoundPoolUtil();
    }

    public static SoundPoolUtil getInstance() {
        return Helper.INSTANCE;
    }

    public void init(Context context){
        mContext = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attr = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION)  // 设置音效使用场景
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();  // 设置音效的类型
            soundPool= new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(attr).build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_RING, 1);
        }
        soundMaps.put(1,soundPool.load(context, R.raw.call_out, 1));
        soundMaps.put(2,soundPool.load(context, R.raw.call_in, 1));
        soundMaps.put(3,soundPool.load(context, R.raw.hang_up, 1));
    }

    public void play(int ringId,int loop) {
        if (soundPool!=null){
            AudioManager am= (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
            float streamVolumeCurrent = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            float streamVolumeMax = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
            volume = streamVolumeCurrent/streamVolumeMax;
            LogUtils.e("streamVolumeCurrent---->"+streamVolumeCurrent+"---streamVolumeMax--->"+streamVolumeMax+"----volume--->"+volume);
           playId = soundPool.play(soundMaps.get(ringId), volume, volume, 1, loop, 1);  // ③
       }
    }

    public void stop() { // 根据字符串弹Toast
        soundPool.stop(playId);
    }

    public void pause() {
        soundPool.autoPause();
    }

    public void release() {
        soundPool.release();
    }
}
