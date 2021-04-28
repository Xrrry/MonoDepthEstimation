package com.example.monodepthestimation.camera1

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.OrientationEventListener
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.monodepthestimation.MyApplication
import com.example.monodepthestimation.Orientation
import com.example.monodepthestimation.R
import kotlinx.android.synthetic.main.activity_camera.*
import java.util.*

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

    private lateinit var mCameraHelper: CameraHelper
    var sp: SoundPool? = null
    var sounddata: HashMap<Int, Int>? = null
    var nowSound: Int? = null
    private var mTimer: Timer? = null
    private var mTimerTask: TimerTask? = null
    private var orientationListener: CameraOrientationListener? = null
    private var mOrientation = 0

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
        println(screenWidth)
        println(screenHeight)
        surfaceView.layoutParams.height = screenHeight
        imageView.layoutParams.height = screenHeight
        surfaceView.layoutParams.width = screenWidth
        imageView.layoutParams.width = screenWidth

        mCameraHelper = CameraHelper(this, surfaceView, imageView, ssh)


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
        orientationListener = CameraOrientationListener(this)
        orientationListener!!.enable()
        startOrientationChangeListener()
    }

    fun InitSound() {
        sp = SoundPool(5, AudioManager.STREAM_MUSIC, 0)
        sounddata = HashMap()
        sounddata!!.put(1, sp!!.load(this, R.raw.sine_tone_600hz_05s, 1))
        sounddata!!.put(2, sp!!.load(this, R.raw.sine_tone_600hz_025s, 1))
        sounddata!!.put(3, sp!!.load(this, R.raw.sine_tone_600hz_0125s, 1))
        sounddata!!.put(4, sp!!.load(this, R.raw.sine_tone_600hz_1s, 1))
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
                            playSound(MyApplication.level, 0, MyApplication.leftVolumn, MyApplication.rightVolumn)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        if (mTimer != null && mTimerTask != null) mTimer!!.schedule(mTimerTask, 0, 1000)
    }

    private fun startOrientationChangeListener() {
        val mOrEventListener: OrientationEventListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(rotation: Int) {
                var rotation = rotation
                rotation = if (rotation >= 0 && rotation <= 45 || rotation > 315) {
                    0
                } else if (rotation > 45 && rotation <= 135) {
                    90
                } else if (rotation > 135 && rotation <= 225) {
                    180
                } else if (rotation > 225 && rotation <= 315) {
                    270
                } else {
                    0
                }
                if (rotation == mOrientation) return
                mOrientation = rotation
            }
        }
        mOrEventListener.enable()
    }

    private class CameraOrientationListener(context: Context?) : OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
        private var mCurrentNormalizedOrientation = 0

        /**
         * 获取当前方向
         *
         * @return
         */
        var rememberedNormalOrientation = 0
            private set

        override fun onOrientationChanged(orientation: Int) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation)
            }
            val str = "当前屏幕手持角度:$orientation°\n当前屏幕手持方向:$mCurrentNormalizedOrientation"
            println(str)
        }

        private fun normalize(degrees: Int): Int {
            if (degrees > 315 || degrees <= 45) {
                return 0
            }
            if (degrees > 45 && degrees <= 135) {
                return 90
            }
            if (degrees > 135 && degrees <= 225) {
                return 180
            }
            if (degrees > 225 && degrees <= 315) {
                return 270
            }
            throw RuntimeException("The physics as we know them are no more. Watch out for anomalies.")
        }

        /**
         * 记录方向
         */
        fun rememberOrientation() {
            rememberedNormalOrientation = mCurrentNormalizedOrientation
        }

    }


    override fun onDestroy() {
        mCameraHelper.releaseCamera()
        if(mTimer!=null) mTimer!!.cancel()
        if(orientationListener!=null) orientationListener!!.disable()
        super.onDestroy()
    }
}