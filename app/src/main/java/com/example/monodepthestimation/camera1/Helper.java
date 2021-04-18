package com.example.monodepthestimation.camera1;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.monodepthestimation.MyApplication;
import com.example.monodepthestimation.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Helper {

    Handler handler = new Handler();
//    MyApplication myApplication = new MyApplication();

    public void getPrediction(File file, ImageView imageView, String ssh) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file",file.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), file))
                .build();
        Request request = new Request.Builder()
                .url("http://hz.matpool.com:" + ssh + "/upload")
                .method("POST", body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                System.out.println("------post请求失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("-------post请求成功");
                if(response.isSuccessful()) {
                    System.out.println("------success");
                    InputStream is = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
//                    Matrix matrix = new Matrix();
//                    matrix.postRotate(90f);
//                    Bitmap nbmp2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    is.close();
                    Integer leftVolumn = Integer.valueOf(response.header("Left-Volumn"));
                    Integer rightVolumn = Integer.valueOf(response.header("Right-Volumn"));
                    Integer level = Integer.valueOf(response.header("Depth-Level"));
//                    myApplication.setLeftVolumn(leftVolumn);
//                    myApplication.setRightVolumn(rightVolumn);
//                    myApplication.setLevel(level);
                    MyApplication.level = level;
                    MyApplication.leftVolumn = leftVolumn;
                    MyApplication.rightVolumn = rightVolumn;
//                    System.out.println(myApplication.getLeftVolumn());
//                    System.out.println(myApplication.getRightVolumn());
//                    System.out.println(myApplication.getLevel());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(imageView!=null) {
                                imageView.setImageBitmap(bitmap);
                            }
                        }
                    });
                }
                else {
                    System.out.println("--------responsefail");
                }
            }
        });
    }


}
