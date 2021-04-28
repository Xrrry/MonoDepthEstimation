package com.example.monodepthestimation;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationEventListener;

public class Orientation extends AppCompatActivity {

    private final String TAG = Orientation.class.getSimpleName();
    private CameraOrientationListener orientationListener;
    private TextView txt;
    /**
     * 当前屏幕旋转角度
     */
    private int mOrientation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orientation);

        txt = findViewById(R.id.txt);
        orientationListener = new CameraOrientationListener(this);
        orientationListener.enable();

        startOrientationChangeListener();
    }

    /**
     * 启动屏幕朝向改变监听函数 用于在屏幕横竖屏切换时改变保存的图片的方向
     */
    private void startOrientationChangeListener() {
        OrientationEventListener mOrEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int rotation) {
                Log.i(TAG, "当前屏幕手持角度方法:" + rotation + "°");
                if (((rotation >= 0) && (rotation <= 45)) || (rotation > 315)) {
                    rotation = 0;
                } else if ((rotation > 45) && (rotation <= 135)) {
                    rotation = 90;
                } else if ((rotation > 135) && (rotation <= 225)) {
                    rotation = 180;
                } else if ((rotation > 225) && (rotation <= 315)) {
                    rotation = 270;
                } else {
                    rotation = 0;
                }
                if (rotation == mOrientation)
                    return;
                mOrientation = rotation;

            }
        };
        mOrEventListener.enable();
    }

    /**
     * 当方向改变时，将调用侦听器onOrientationChanged(int)
     */
    private class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(final int orientation) {
            Log.i(TAG, "当前屏幕手持角度:" + orientation + "°");
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String str = "当前屏幕手持角度:" + orientation + "°\n当前屏幕手持方向:" + mCurrentNormalizedOrientation;
                    txt.setText(str);
                }
            });
        }

        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }
            if (degrees > 45 && degrees <= 135) {
                return 90;
            }
            if (degrees > 135 && degrees <= 225) {
                return 180;
            }
            if (degrees > 225 && degrees <= 315) {
                return 270;
            }
            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        /**
         * 记录方向
         */
        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        /**
         * 获取当前方向
         *
         * @return
         */
        public int getRememberedNormalOrientation() {
            return mRememberedNormalOrientation;
        }
    }

}
