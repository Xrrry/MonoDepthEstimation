package com.example.monodepthestimation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.monodepthestimation.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Home extends AppCompatActivity {
    private Button btn_takephoto_amn, btn_takephotocam_amn;
    private ImageView iv_photo_ato;
    private int REQUEST_CODE = 1;
    public static final int REQUEST_CODE_READ = 3;
    public static final int REQUEST_CODE_WRITE = 2;
    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();//代码初始化
        setClick();//设置点击方法
        getInfo();//获取其他界面传递过来的信息
    }

    /**
     * 获取其他界面传递过来的信息
     */
    private void getInfo() {
        try {
            String path = getIntent().getStringExtra("picpath");//通过值"picpath"得到照片路径
            FileInputStream fis = new FileInputStream(path);//通过path把照片读到文件输入流中
            Bitmap bitmap = BitmapFactory.decodeStream(fis);//将输入流解码为bitmap
            Matrix matrix = new Matrix();//新建一个矩阵对象
            matrix.setRotate(90);//矩阵旋转操作让照片可以正对着你。但是还存在一个左右对称的问题
//新建位图，第2个参数至第5个参数表示位图的大小，matrix中是旋转后的位图信息，并使bitmap变量指向新的位图对象
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            iv_photo_ato.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置点击方法
     */
    private void setClick() {
        btn_takephoto_amn.setOnClickListener(new MainActivityClick());
        btn_takephotocam_amn.setOnClickListener(new MainActivityClick());
    }

    /**
     * 代码初始化
     */
    private void initView() {
        iv_photo_ato = findViewById(R.id.iv_photo_ato);//展示拍照图片的控件
        btn_takephoto_amn = findViewById(R.id.btn_takephoto_amn);//调用系统组件拍照
        btn_takephotocam_amn = findViewById(R.id.btn_takephotocam_amn);//调用自定义组件拍照
    }

    /**
     * 设置点击方法
     */
    private class MainActivityClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_takephoto_amn://拍照按钮的点击方法
//                    Toast.makeText(MainActivity.this, "拍照按钮", Toast.LENGTH_SHORT).show();
                    checkPermissions();//获取用户权限
                    break;
                case R.id.btn_takephotocam_amn://进入自定义拍照界面
                    checkotherPermissions();//获取用户权限
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 自定义拍照界面获取权限
     */
    private void checkotherPermissions() {
        boolean canWrite = isWriteStoragePermissionGranted();//获取写的权限
//        Log.d(TAG, " canWrite :" + canWrite);
        if (canWrite) {
            boolean canRead = isReadStoragePermissionGranted();//获取读的权限
//            Log.d(TAG, " canRead :" + canRead);
            if (canRead) {
//                Log.d(TAG, " Now you are ready to access camera :");
                startActivity(new Intent(Home.this, Customcamera.class));
            }
        }

    }

    /**
     * 获取用户权限
     */
    private void checkPermissions() {
        boolean canWrite = isWriteStoragePermissionGranted();//获取写的权限
//        Log.d(TAG, " canWrite :" + canWrite);
        if (canWrite) {
            boolean canRead = isReadStoragePermissionGranted();//获取读的权限
//            Log.d(TAG, " canRead :" + canRead);
            if (canRead) {
//                Log.d(TAG, " Now you are ready to access camera :");
                callCamera();
            }
        }
    }


    /**
     * 打开照相机
     */
    private void callCamera() {
        mFilePath = Environment.getExternalStorageDirectory().getPath();
        // 保存图片的文件名
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");// HH:mm:ss
        //获取当前时间
        Date date = new Date(System.currentTimeMillis());
        mFilePath = mFilePath + "/" + simpleDateFormat.format(date) + "mytest.jpg";
        Uri mUri = Uri.fromFile(new File(mFilePath));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            UserTypeMore7((new File(mFilePath)).getAbsolutePath());//使用另一种方法调用摄像头
        } else {
            Intent intent = new Intent();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);//设置图片保存路径
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQUEST_CODE);
        }

    }

    /**
     * 使用另一种方法调用摄像头
     *
     * @param absolutePath
     */
    private void UserTypeMore7(String absolutePath) {
        Uri mCameraTempUri;
        try {
            ContentValues values = new ContentValues(1);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            values.put(MediaStore.Images.Media.DATA, absolutePath);
            mCameraTempUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            if (mCameraTempUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraTempUri);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            }
            intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//设置照片的横竖样式
            startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_READ);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    private boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
//                Log.v(TAG, "Permission is granted2 write");
                return true;
            } else {
//                Log.v(TAG, "Permission is revoked write ");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CODE_WRITE);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
//            Log.v(TAG, "Permission is granted write");
            return true;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {//照相机方法返回的数据
            if (resultCode == Activity.RESULT_OK) {//照相机返回成功
                FileInputStream is = null;
                try {
                    // 获取输入流
                    is = new FileInputStream(mFilePath);
                    if (null == is) {//判空操作
                        return;

                    }
                    // 把流解析成bitmap,此时就得到了清晰的原图
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmapOptions.inSampleSize = 5;  //压缩为原来的5分之一
                    Bitmap bitmap = BitmapFactory.decodeStream(is, null, bitmapOptions);
                    iv_photo_ato.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(Home.this, "用户取消了拍照功能", Toast.LENGTH_SHORT).show();
            }
        }
    }
}