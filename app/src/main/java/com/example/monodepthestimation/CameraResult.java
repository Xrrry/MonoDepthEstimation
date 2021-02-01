package com.example.monodepthestimation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author Jay
 * @date 2020/12/2
 * File description.
 * 显示照相机结果的界面
 */
public class CameraResult extends AppCompatActivity {
    private Button btn_again_act, btn_userphoto_act;
    private ImageView iv_photo_act;
    private String mFilePath = "";
    // 縮放控制
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    // 不同状态的表示：
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    // 定义第一个按下的点，两只接触点的重点，以及出事的两指按下的距离：
    private PointF startPoint = new PointF();
    private PointF midPoint = new PointF();
    private float oriDis = 1f;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameraresult);
        initView();//代码初始化
        getInfo();//获取上一个界面传递过来的信息
        setClick();//设置点击方法
    }

    /**
     * 设置点击方法
     */
    private void setClick() {
        btn_again_act.setOnClickListener(new CameraResultClick());
        btn_userphoto_act.setOnClickListener(new CameraResultClick());
        iv_photo_act.setOnTouchListener(new PhotoClick());//设置图片的点击方法
    }

    /**
     * 获取上一个界面传递过来的信息
     */
    private void getInfo() {
        mFilePath = getIntent().getStringExtra("picpath");//通过值"picpath"得到照片路径
        try {
            FileInputStream fis = new FileInputStream(mFilePath);//通过path把照片读到文件输入流中
            Bitmap bitmap = BitmapFactory.decodeStream(fis);//将输入流解码为bitmap
            Matrix matrix = new Matrix();//新建一个矩阵对象
            matrix.setRotate(90);//矩阵旋转操作让照片可以正对着你。但是还存在一个左右对称的问题
//新建位图，第2个参数至第5个参数表示位图的大小，matrix中是旋转后的位图信息，并使bitmap变量指向新的位图对象
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            iv_photo_act.setImageBitmap(bitmap);

            fixPhotoShowError();//修复图片一开始焦距不正常的问题
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修复一开始焦距不正常的问题,里面的数据可以随意调试
     */
    private void fixPhotoShowError() {
        Matrix photomatrix=new Matrix();
        photomatrix.postScale(Float.parseFloat("0.4"),Float.parseFloat("0.4"));
        iv_photo_act.setImageMatrix(photomatrix);
    }

    /**
     * 代码初始化
     */
    private void initView() {
        btn_again_act = findViewById(R.id.btn_again_act);//重拍按钮
        btn_userphoto_act = findViewById(R.id.btn_userphoto_act);//使用按钮
        iv_photo_act = findViewById(R.id.iv_photo_act);//展示图片的控件
    }

    /**
     * 设置点击方法
     */
    private class CameraResultClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_again_act://重拍
                    startActivity(new Intent(CameraResult.this, Customcamera.class));
                    finish();
                    break;
                case R.id.btn_userphoto_act://使用照片
                    startActivity(new Intent(CameraResult.this, MainActivity.class).putExtra("picpath", mFilePath));
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 图片的点击方法
     */
    private class PhotoClick implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView view = (ImageView) v;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 单指
                case MotionEvent.ACTION_DOWN:
                    matrix.set(view.getImageMatrix());
                    savedMatrix.set(matrix);
                    startPoint.set(event.getX(), event.getY());
                    mode = DRAG;
                    break;
                // 双指
                case MotionEvent.ACTION_POINTER_DOWN:
                    oriDis = distance(event);
                    if (oriDis > 10f) {
                        savedMatrix.set(matrix);
                        midPoint = middle(event);
                        mode = ZOOM;
                    }
                    break;
                // 手指放开
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
                // 单指滑动事件
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        // 是一个手指拖动
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - startPoint.x, event.getY() - startPoint.y);
                    } else if (mode == ZOOM) {
                        // 两个手指滑动
                        float newDist = distance(event);
                        if (newDist > 10f) {
                            matrix.set(savedMatrix);
                            float scale = newDist / oriDis;
                            matrix.postScale(scale, scale, midPoint.x, midPoint.y);
                        }
                    }
                    break;
            }
            // 设置ImageView的Matrix
            view.setImageMatrix(matrix);
            return true;//返回值修改为true
        }
    }


    // 计算两个触摸点之间的距离
    private float distance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Float.valueOf(String.valueOf(Math.sqrt(x * x + y * y)));
    }

    // 计算两个触摸点的中点
    private PointF middle(MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        return new PointF(x / 2, y / 2);
    }
}