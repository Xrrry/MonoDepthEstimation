package com.example.monodepthestimation.camera1;


import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.monodepthestimation.MyApplication;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

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

    MyApplication application = new MyApplication();
    Handler handler = new Handler();
    public void bitToImage(Bitmap bitmap, ImageView imageView) {
        imageView.setImageBitmap(bitmap);
    }

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

    public void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("TLS");
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

}
