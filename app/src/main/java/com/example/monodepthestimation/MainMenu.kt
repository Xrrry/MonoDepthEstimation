package com.example.monodepthestimation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.monodepthestimation.camera1.CameraActivity
import com.example.monodepthestimation.util.PermissionUtils
import com.example.monodepthestimation.util.PermissionUtils.PERMISSION_REQUEST_CODE
import com.example.monodepthestimation.util.PermissionUtils.PERMISSION_SETTING_CODE
import kotlinx.android.synthetic.main.activity_main_menu.*

class MainMenu : AppCompatActivity() {

    private val permissionsList = arrayOf(Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setContentView(R.layout.activity_main_menu)

        btCapture.setOnClickListener {
            PermissionUtils.checkPermission(this, permissionsList, Runnable {
                startActivity(Intent(this, CaptureActivity::class.java))
            })
        }
        btCamera.setOnClickListener {
            PermissionUtils.checkPermission(this, permissionsList, Runnable {
                val intent = Intent(this, CameraActivity::class.java)
                intent.putExtra(CameraActivity.TYPE_TAG, CameraActivity.TYPE_CAPTURE)
                startActivity(intent)
            })
        }
        btCameraRecord.setOnClickListener {
            PermissionUtils.checkPermission(this, permissionsList, Runnable {
                val intent = Intent(this, CameraActivity::class.java)
                intent.putExtra(CameraActivity.TYPE_TAG, CameraActivity.TYPE_RECORD)
                startActivity(intent)
            })
        }

        PermissionUtils.checkPermission(this, permissionsList, Runnable {
        })
    }


    /**
     * 第四步，请求权限的结果回调
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        log("onRequestPermissionsResult ")

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                var allGranted = true

                grantResults.forEach {
                    if (it != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false
                    }
                }

                if (allGranted) {  //已获得全部权限
                    log("onRequestPermissionsResult 已获得全部权限")
                } else {
                    log("权限请求被拒绝了,不能继续依赖该权限的相关操作了，展示setting ")

                    // 权限请求被拒绝了,不能继续依赖该权限的相关操作了
                    PermissionUtils.showPermissionSettingDialog(this)
                }
            }
        }
    }


    /**
     * 当从设置权限页面返回后，重新请求权限
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PERMISSION_SETTING_CODE -> { //第五步，当从设置权限页面返回后，重新请求权限
                log("从设置权限页面返回后，重新请求权限")
                PermissionUtils.checkPermission(this, permissionsList, Runnable {
                    val intent = Intent(this, CameraActivity::class.java)
                    startActivity(intent)
                })
            }

        }
    }
}

