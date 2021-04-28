package com.example.monodepthestimation;

import android.app.Application;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MyApplication extends Application {
    public static String time = "";
    public static Boolean has = false;
    public static Integer leftVolumn = 10;
    public static Integer rightVolumn = 10;
    public static Integer level = 0;
}
