package com.example.monodepthestimation.camera1

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.hardware.Camera
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.monodepthestimation.MyApplication
import com.example.monodepthestimation.R
import com.example.monodepthestimation.log
import com.example.monodepthestimation.toast
import com.example.monodepthestimation.util.BitmapUtils
import com.example.monodepthestimation.util.FileUtil
import kotlinx.android.synthetic.main.activity_camera.*
import okio.buffer
import okio.sink
import java.util.*
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

//    var application = MyApplication()
    private lateinit var mCameraHelper: CameraHelper
//    private var mMediaRecorderHelper: MediaRecorderHelper? = null
    var mHelper: Helper = Helper()
    var sp: SoundPool? = null
    var sounddata: HashMap<Int, Int>? = null
    var nowSound: Int? = null
    private var mTimer: Timer? = null
    private var mTimerTask: TimerTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
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
        var sp = this.getSharedPreferences("default", MODE_PRIVATE)
        var ssh = sp.getString("ssh", "null").toString()
        println(ssh)
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
//        val screenHeight = dm.heightPixels / 2
//        val screenWidth = (screenHeight/4*3)
        val screenWidth = dm.widthPixels / 2
        val screenHeight = (screenWidth*2/3)
        println(screenHeight)
        println(screenWidth)
        surfaceView.layoutParams.height = screenHeight
        imageView.layoutParams.height = screenHeight
        surfaceView.layoutParams.width = screenWidth
        imageView.layoutParams.width = screenWidth

        mCameraHelper = CameraHelper(this, surfaceView, imageView, ssh)
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

        if (intent.getIntExtra(TYPE_TAG, 0) == TYPE_RECORD) { //录视频
            btnTakePic.visibility = View.GONE
            btnStart.visibility = View.VISIBLE
        }

        btnTakePic.setOnClickListener {
//            mCameraHelper.takePic()
//            val timer = Timer()
//            timer.schedule(timerTask { mCameraHelper.takePic() }, 0,2000)
        }
        ivExchange.setOnClickListener { mCameraHelper.exchangeCamera() }

        btnStart.setOnClickListener {
            btnStart.visibility = View.GONE
            btnStop.visibility = View.VISIBLE
            mCameraHelper.startGetPreviewImage()
            startTimer()

        }
        btnStop.setOnClickListener {
            btnStart.visibility = View.VISIBLE
            btnStop.visibility = View.GONE
            mCameraHelper.stopGetPreviewImage()
            mTimer!!.cancel()
        }
        InitSound()
    }

    fun InitSound() {
        sp = SoundPool(5, AudioManager.STREAM_MUSIC, 0)
        sounddata = HashMap()
        sounddata!!.put(1, sp!!.load(this, R.raw.sine_tone_600hz_05s, 1))
        sounddata!!.put(2, sp!!.load(this, R.raw.sine_tone_600hz_025s, 1))
        sounddata!!.put(3, sp!!.load(this, R.raw.sine_tone_600hz_0125s, 1))
    }

    fun playSound(sound: Int, number: Int, leftVolumn: Int, rightVolumn: Int) {
        val am = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        val volumnCurrent = am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
        val volumnRatio = volumnCurrent / audioMaxVolumn
        println(volumnCurrent)
        println(volumnRatio)
        nowSound = sounddata!![sound]?.let {
            sp!!.play(it,
                    volumnRatio / 10 * leftVolumn,  // 左声道音量
                    volumnRatio / 10 * rightVolumn,  // 右声道音量
                    1,  // 优先级
                    number, 1f)
        } // 回放速度，该值在0.5-2.0之间 1为正常速度
    }


    private fun startTimer() {
        if (mTimer == null) {
            mTimer = Timer()
        }
        if (mTimerTask == null) {
            mTimerTask = object : TimerTask() {
                override fun run() {
                    try {
                        println("level" + MyApplication.level)
                        println("left" + MyApplication.leftVolumn)
                        println("right" + MyApplication.rightVolumn)
                        if (nowSound != null) {
                            sp!!.stop(nowSound!!)
                        }
                        if(MyApplication.level!=0) {
                            playSound(MyApplication.level, 3, MyApplication.leftVolumn, MyApplication.rightVolumn)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        if (mTimer != null && mTimerTask != null) mTimer!!.schedule(mTimerTask, 0, 1000)
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
                        log("图片已保存! 耗时：${System.currentTimeMillis() - temp}    路径：  ${picFile.absolutePath}")
                    }
//                    println(picFile?.absolutePath)
//                    mHelper.getPrediction(picFile, imageView)
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
//        mMediaRecorderHelper?.let {
//            if (it.isRunning)
//                it.stopRecord()
//            it.release()
//        }
        super.onDestroy()
    }


}