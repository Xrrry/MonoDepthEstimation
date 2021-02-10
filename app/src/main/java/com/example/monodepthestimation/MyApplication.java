package com.example.monodepthestimation;

import android.app.Application;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MyApplication extends Application {
    public String time = "";
//    public int fps;
    public Boolean has = false;
}
