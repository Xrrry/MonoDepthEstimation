package com.example.monodepthestimation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Sound extends AppCompatActivity {

    SoundPool sp;
    HashMap<Integer, Integer> sounddata;
    Integer nowSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        // 设置状态栏字体颜色 黑色
        Window window = getWindow();
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            } catch (Exception e) {

            }
        }
        setContentView(R.layout.activity_sound);
        InitSound();
//        findViewById(R.id.s10).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(nowSound!=null) {
//                    sp.stop(nowSound);
//                }
//                playSound(1, 0, 10,  0);
//            }
//        });
//        findViewById(R.id.s1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(nowSound!=null) {
//                    sp.stop(nowSound);
//                }
//                playSound(2, 0, 8, 3);
//            }
//        });
        findViewById(R.id.s05).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nowSound!=null) {
                    sp.stop(nowSound);
                }
                playSound(1, 0, 10, 10);
            }
        });
        findViewById(R.id.s025).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nowSound!=null) {
                    sp.stop(nowSound);
                }
                playSound(2, 1, 5, 10);
            }
        });
        findViewById(R.id.s0125).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nowSound!=null) {
                    sp.stop(nowSound);
                }
                playSound(3, 3, 10, 5);
            }
        });
        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nowSound!=null) {
                    sp.stop(nowSound);
                }
            }
        });
    }

    public void InitSound() {
        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        sounddata = new HashMap<Integer, Integer>();
//        sounddata.put(1, sp.load(this, R.raw.sine_tone_600hz_10s, 1));
//        sounddata.put(2, sp.load(this, R.raw.sine_tone_600hz_1s, 1));
        sounddata.put(1, sp.load(this, R.raw.sine_tone_600hz_05s, 1));
        sounddata.put(2, sp.load(this, R.raw.sine_tone_600hz_025s, 1));
        sounddata.put(3, sp.load(this, R.raw.sine_tone_600hz_0125s, 1));
    }

    public void playSound(int sound, int number, int leftVolumn, int rightVolumn) {
        AudioManager am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volumnCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = volumnCurrent / audioMaxVolumn;
        System.out.println(volumnCurrent);
        System.out.println(volumnRatio);

        nowSound = sp.play(sounddata.get(sound),
                volumnRatio/10*leftVolumn,// 左声道音量
                volumnRatio/10*rightVolumn,// 右声道音量
                1, // 优先级
                number,// 循环播放次数
                1);// 回放速度，该值在0.5-2.0之间 1为正常速度
    }
}
