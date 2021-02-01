package com.example.monodepthestimation;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Jay
 * @date 2020/12/2
 * File description.
 */
public class Customcamera extends AppCompatActivity implements SurfaceHolder.Callback {
    private Camera mCamera;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private int cameraId = 0;//声明cameraId属性，ID为1调用前置摄像头，为0调用后置摄像头。此处因有特殊需要故调用前置摄像头
    private Button btn_cancel_aca, btn_ok_aca, btn_photo_aca;
    //定义照片保存并显示的方法
    private Camera.PictureCallback mpictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            String mFilePath = Environment.getExternalStorageDirectory().getPath();
            // 保存图片的文件名
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHH:mm:ss");// HH:mm:ss
            //获取当前时间
            Date date = new Date(System.currentTimeMillis());
            mFilePath = mFilePath + "/" + simpleDateFormat.format(date) + "mytest.jpg";
            File tempfile = new File(mFilePath);//新建一个文件对象tempfile，并保存在某路径中
            try {
                FileOutputStream fos = new FileOutputStream(tempfile);
                fos.write(data);//将照片放入文件中
                fos.close();//关闭文件
                Intent intent = new Intent(Customcamera.this, CameraResult.class);//新建信使对象
                intent.putExtra("picpath", mFilePath);//打包文件给信使
                startActivity(intent);//打开新的activity，即打开展示照片的布局界面
                Customcamera.this.finish();//关闭现有界面
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customcamera);
        initView();//代码初始化
        setClick();//设置点击方法
        mHolder = mPreview.getHolder();
        mHolder.addCallback(this);

    }

    /**
     * 设置点击方法
     */
    private void setClick() {
        mPreview.setOnClickListener(new CustomcameraClick());
        btn_cancel_aca.setOnClickListener(new CustomcameraClick());
        btn_ok_aca.setOnClickListener(new CustomcameraClick());
        btn_photo_aca.setOnClickListener(new CustomcameraClick());
    }

    /**
     * 代码初始化
     */
    private void initView() {
        mPreview = findViewById(R.id.preview);//初始化预览界面
        btn_cancel_aca = findViewById(R.id.btn_cancel_aca);//取消按钮
        btn_ok_aca = findViewById(R.id.btn_ok_aca);//确定按钮
        btn_photo_aca = findViewById(R.id.btn_photo_aca);//拍照按钮
    }

    //定义“拍照”方法
    public void takePhoto() {
        //配置如下：
        Camera.Parameters parameters = mCamera.getParameters();// 获取相机参数集
        List<Camera.Size> SupportedPreviewSizes =
                parameters.getSupportedPreviewSizes();// 获取支持预览照片的尺寸
        Camera.Size previewSize = SupportedPreviewSizes.get(0);// 从List取出Size
        parameters.setPreviewSize(previewSize.width, previewSize.height);//
        //  设置预览照片的大小
        List<Camera.Size> supportedPictureSizes =
                parameters.getSupportedPictureSizes();// 获取支持保存图片的尺寸
        Camera.Size pictureSize = supportedPictureSizes.get(0);// 从List取出Size
        parameters.setPictureSize(pictureSize.width, pictureSize.height);//
        // 设置照片的大小
        mCamera.setParameters(parameters);

        //摄像头聚焦
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    mCamera.takePicture(null, null, mpictureCallback);
                }
            }
        });

    }

    //activity生命周期在onResume是界面应是显示状态
    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {//如果此时摄像头值仍为空
            mCamera = getCamera();//则通过getCamera()方法开启摄像头
            if (mHolder != null) {
                setStartPreview(mCamera, mHolder);//开启预览界面
            }
        }
    }

    //activity暂停的时候释放摄像头
    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    //onResume()中提到的开启摄像头的方法
    private Camera getCamera() {
        Camera camera;//声明局部变量camera
        try {
            camera = Camera.open(cameraId);
        }//根据cameraId的设置打开前置摄像头
        catch (Exception e) {
            camera = null;
            e.printStackTrace();
        }
        return camera;
    }

    //开启预览界面
    private void setStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);//如果没有这行你看到的预览界面就会是水平的
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //定义释放摄像头的方法
    private void releaseCamera() {
        if (mCamera != null) {//如果摄像头还未释放，则执行下面代码
            mCamera.stopPreview();//1.首先停止预览
            mCamera.setPreviewCallback(null);//2.预览返回值为null
            mCamera.release(); //3.释放摄像头
            mCamera = null;//4.摄像头对象值为null
        }
    }

    //定义新建预览界面的方法
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setStartPreview(mCamera, mHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();//如果预览界面改变，则首先停止预览界面
        setStartPreview(mCamera, mHolder);//调整再重新打开预览界面
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();//预览界面销毁则释放相机
    }

    private class CustomcameraClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.preview://点击预览界面聚焦
                    mCamera.autoFocus(null);
                    break;
                case R.id.btn_cancel_aca://取消按钮
                    finish();
                    break;
                case R.id.btn_ok_aca:
                    Toast.makeText(Customcamera.this, "确定按钮", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btn_photo_aca://拍照按钮
                    takePhoto();
                    break;
                default:
                    break;
            }
        }
    }
}