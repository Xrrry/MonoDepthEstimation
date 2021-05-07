package com.example.monodepthestimation.camera1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;
import com.example.monodepthestimation.MyApplication;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
                    is.close();
                    Integer leftVolumn = Integer.valueOf(response.header("Left-Volumn"));
                    Integer rightVolumn = Integer.valueOf(response.header("Right-Volumn"));
                    Integer level = Integer.valueOf(response.header("Depth-Level"));
                    MyApplication.level = level;
                    MyApplication.leftVolumn = leftVolumn;
                    MyApplication.rightVolumn = rightVolumn;
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
