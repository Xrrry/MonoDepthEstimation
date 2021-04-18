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
    public static Integer level = 3;
//    private static String time = "";
//    private static Boolean has = false;
//    private static Integer leftVolumn = 10;
//    private static Integer rightVolumn = 10;
//    private static Integer level = 3;
}
