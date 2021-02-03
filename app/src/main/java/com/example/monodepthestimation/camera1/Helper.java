package com.example.monodepthestimation.camera1;


import android.os.Handler;
import android.widget.TextView;

import com.example.monodepthestimation.util.BitmapUtils;

import java.io.ByteArrayInputStream;
import java.io.File;

import kotlin.UByteArray;
import okio.BufferedSink;
import okio.ByteString;

public class Helper {
    Handler handler = new Handler();
    public void helperTest(byte[] data, TextView textView) {
        System.out.println("123123");
        handler.post(new Runnable() {
            @Override
            public void run() {
                textView.setText("22222");
            }
        });
    }
}
