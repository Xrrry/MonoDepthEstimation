package com.example.monodepthestimation.camera1

import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.RectF
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.monodepthestimation.R
import com.example.monodepthestimation.log
import com.example.monodepthestimation.toast
import com.example.monodepthestimation.util.BitmapUtils
import com.example.monodepthestimation.util.FileUtil
import kotlinx.android.synthetic.main.activity_camera.*
import okio.buffer
import okio.sink
import kotlin.concurrent.thread


/**
 * author :  chensen
 * data  :  2018/3/18
 * desc :
 */
class CameraActivity : AppCompatActivity() {
    companion object {
        const val TYPE_TAG = "type"
        const val TYPE_CAPTURE = 0
        const val TYPE_RECORD = 1
    }

//    var lock = false //控制MediaRecorderHelper的初始化
    private lateinit var mCameraHelper: CameraHelper
    private var mMediaRecorderHelper: MediaRecorderHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        // 隐藏标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        // 隐藏状态栏
        window.statusBarColor = Color.TRANSPARENT
        // 设置状态栏字体颜色 黑色
        val window = window
        if (window != null) {
            val clazz: Class<*> = window.javaClass
            try {
                var darkModeFlag = 0
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag) //状态栏透明且黑色字体
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                    getWindow().decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            } catch (e: Exception) {
            }
        }
        setContentView(R.layout.activity_camera)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        mCameraHelper = CameraHelper(this, surfaceView, imageView)
        mCameraHelper.addCallBack(object : CameraHelper.CallBack {
//            override fun onFaceDetect(faces: ArrayList<RectF>) {
//                faceView.setFaces(faces)
//            }

            override fun onTakePic(data: ByteArray?) {
                savePic(data)
                btnTakePic.isClickable = true
            }

//            override fun onPreviewFrame(data: ByteArray?) {
//                if (!lock) {
//                    mCameraHelper.getCamera()?.let {
//                        mMediaRecorderHelper = MediaRecorderHelper(this@CameraActivity, mCameraHelper.getCamera()!!, mCameraHelper.mDisplayOrientation, mCameraHelper.mSurfaceHolder.surface)
//                    }
//                    lock = true
//                }
//            }
        })
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        val screenHeight = dm.heightPixels / 2
        val screenWidth = (screenHeight/4*3)
        surfaceView.layoutParams.height = screenHeight
        imageView.layoutParams.height = screenHeight
        surfaceView.layoutParams.width = screenWidth
        imageView.layoutParams.width = screenWidth
        if (intent.getIntExtra(TYPE_TAG, 0) == TYPE_RECORD) { //录视频
            btnTakePic.visibility = View.GONE
            btnStart.visibility = View.VISIBLE
        }

        btnTakePic.setOnClickListener {
            mCameraHelper.takePic()
//            val timer = Timer()
//            timer.schedule(timerTask { mCameraHelper.takePic() }, 0,1000)
        }
        ivExchange.setOnClickListener { mCameraHelper.exchangeCamera() }
//        btnStart.setOnClickListener {
//            ivExchange.isClickable = false
//            btnStart.visibility = View.GONE
//            btnStop.visibility = View.VISIBLE
//            mMediaRecorderHelper?.startRecord()
//        }
//        btnStop.setOnClickListener {
//            btnStart.visibility = View.VISIBLE
//            btnStop.visibility = View.GONE
//            ivExchange.isClickable = true
//            mMediaRecorderHelper?.stopRecord()
//        }
    }


    fun savePic(data: ByteArray?) {
        thread {
            try {
                val temp = System.currentTimeMillis()
                val picFile = FileUtil.createCameraFile()
                if (picFile != null && data != null) {
                    val rawBitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                    val resultBitmap = if (mCameraHelper.mCameraFacing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        BitmapUtils.mirror(BitmapUtils.rotate(rawBitmap, 270f))
                    else
                        BitmapUtils.rotate(rawBitmap, 0f)
                    picFile.sink().buffer().write(BitmapUtils.toByteArray(resultBitmap)).close()
                    runOnUiThread {
                        toast("图片已保存! ${picFile.absolutePath}")
                        log("图片已保存! 耗时：${System.currentTimeMillis() - temp}    路径：  ${picFile.absolutePath}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    toast("保存图片失败！")
                }
            }
        }
    }

    override fun onDestroy() {
        mCameraHelper.releaseCamera()
        mMediaRecorderHelper?.let {
            if (it.isRunning)
                it.stopRecord()
            it.release()
        }
        super.onDestroy()
    }

}