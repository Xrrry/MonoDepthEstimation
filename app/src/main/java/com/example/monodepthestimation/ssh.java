package com.example.monodepthestimation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;
import com.example.monodepthestimation.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import lombok.ToString;

public class ssh extends AppCompatActivity {

    Handler handler = new Handler();
    SoundPool sp;
    HashMap<Integer, Integer> sounddata;

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
        setContentView(R.layout.activity_ssh);
        EditText editText = findViewById(R.id.text);
        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = editText.getText().toString();
                System.out.println(s);
                SharedPreferences sp = getSharedPreferences("default", getApplicationContext().MODE_PRIVATE);
                sp.edit()
                        .clear()
                        .apply();
                sp.edit()
                        .putString("ssh",s)
                        .apply();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "更新成功" ,Toast.LENGTH_LONG).show();
                    }
                });
                finish();
            }
        });

        InitSound();
//        playSound(1,1);
    }


    public void InitSound() {
        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 5);
        sounddata = new HashMap<Integer, Integer>();
        sounddata.put(1, sp.load(this, R.raw.tone_600hz, 1));
        sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener(){
            @Override
            public void onLoadComplete(SoundPool sound,int sampleId,int status){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "声音加载成功" ,Toast.LENGTH_SHORT).show();
                    }
                });
                playSound(1,0);
            }
        });
    }

    public void playSound(int sound, int number) {
        AudioManager am = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volumnCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        float volumnRatio = volumnCurrent / audioMaxVolumn;

        sp.play(sounddata.get(sound),
                volumnRatio,// 左声道音量
                volumnRatio,// 右声道音量
                1, // 优先级
                number,// 循环播放次数
                1);// 回放速度，该值在0.5-2.0之间 1为正常速度
    }
}
