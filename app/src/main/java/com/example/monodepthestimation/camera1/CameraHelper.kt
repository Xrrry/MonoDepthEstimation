package com.example.monodepthestimation.camera1

import android.app.Activity
import android.graphics.*
import android.hardware.Camera
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.ImageView
import android.widget.Toast
import com.example.monodepthestimation.MyApplication
import com.example.monodepthestimation.log
import com.example.monodepthestimation.util.BitmapUtils
import com.example.monodepthestimation.util.FileUtil
import okio.buffer
import okio.sink
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class CameraHelper(activity: Activity, surfaceView: SurfaceView, imageView: ImageView, ssh: String) {

    private var mCamera: Camera? = null                   //Camera对象
    private lateinit var mParameters: Camera.Parameters   //Camera对象的参数
    private var mSurfaceView: SurfaceView = surfaceView   //用于预览的SurfaceView对象
    private var mimageView: ImageView = imageView
    private var mSurfaceHolder: SurfaceHolder                     //SurfaceHolder对象
    private var mHelper: Helper = Helper()
    private var sssh: String = ssh

    private var mActivity: Activity = activity

    private var mCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK  //摄像头方向
    private var mDisplayOrientation: Int = 0    //预览旋转的角度


    private fun init() {
        mSurfaceHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                releaseCamera()
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                if (mCamera == null) {
                    openCamera(mCameraFacing)
                }
                startPreview()
            }
        })
    }


    //打开相机
    private fun openCamera(cameraFacing: Int = Camera.CameraInfo.CAMERA_FACING_BACK): Boolean {
        val supportCameraFacing = supportCameraFacing(cameraFacing)
        if (supportCameraFacing) {
            try {
                mCamera = Camera.open(cameraFacing)
                initParameters(mCamera!!)
//                mCamera?.setPreviewCallback(this)
            } catch (e: Exception) {
                e.printStackTrace()
                toast("打开相机失败!")
                return false
            }
        }
        return supportCameraFacing
    }

    //配置相机参数
    private fun initParameters(camera: Camera) {
        try {
            mParameters = camera.parameters
            mParameters.previewFormat = ImageFormat.NV21

            //获取与指定宽高相等或最接近的尺寸
            //设置预览尺寸
            val bestPreviewSize = getBestSize(mSurfaceView.width, mSurfaceView.height, mParameters.supportedPreviewSizes)
            bestPreviewSize?.let {
                mParameters.setPreviewSize(it.width, it.height)
            }
            //设置保存图片尺寸
            val bestPicSize = getBestSize(mimageView.width, mimageView.height, mParameters.supportedPictureSizes)
            bestPicSize?.let {
                mParameters.setPictureSize(it.width, it.height)
            }
            //对焦模式
            if (isSupportFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                mParameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE

            camera.parameters = mParameters
        } catch (e: Exception) {
            e.printStackTrace()
            toast("相机初始化失败!")
        }
    }
    //获取与指定宽高相等或最接近的尺寸
    private fun getBestSize(targetWidth: Int, targetHeight: Int, sizeList: List<Camera.Size>): Camera.Size? {
        var bestSize: Camera.Size? = null
        val targetRatio = (targetWidth.toDouble() / targetHeight)  //目标大小的宽高比
        var minDiff = targetRatio

        for (size in sizeList) {
            val supportedRatio = (size.width.toDouble() / size.height)
            log("系统支持的尺寸 : ${size.width} * ${size.height} ,    比例$supportedRatio")
        }

        for (size in sizeList) {
            if (size.width == targetWidth && size.height == targetHeight) {
                bestSize = size
                break
            }

            val supportedRatio = (size.width.toDouble() / size.height)
            if (Math.abs(supportedRatio - targetRatio) < minDiff) {
                minDiff = Math.abs(supportedRatio - targetRatio)
                bestSize = size
            }
        }
        log("目标尺寸 ：$targetWidth * $targetHeight ，   比例  $targetRatio")
        log("最优尺寸 ：${bestSize?.width} * ${bestSize?.height}")
        return bestSize
    }

    //开始预览
    fun startPreview() {
        mCamera?.let {
            it.setPreviewDisplay(mSurfaceHolder)
            setCameraDisplayOrientation(mActivity)
            it.startPreview()
        }
    }

    fun startGetPreviewImage() {
        mCamera!!.setPreviewCallback { data, camera ->
            val size = camera.parameters.previewSize
            try {
                val image = YuvImage(data, ImageFormat.NV21, size.width, size.height, null)
                if (image != null) {
                    var time = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    var miTime = SimpleDateFormat("SSS").format(Date()).substring(0, 1).toInt()
                    var lastTime = MyApplication.time
                    var has = MyApplication.has
//                    if(time!=lastTime) {
//                        println("$lastTime  $time  $miTime")
//                        MyApplication.time = time
//                        MyApplication.has = false
//                        val stream = ByteArrayOutputStream()
//                        image.compressToJpeg(Rect(0, 0, size.width, size.height), 80, stream)
//                        val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
////                        rotateMyBitmap(bmp)
//                        savePreviewPic(bmp, mimageView)
//                        stream.close()
//                    }
//                    else if(miTime>=5&&!has) {
//                        println("$lastTime  $time  $miTime")
//                        MyApplication.has = true
//                        val stream = ByteArrayOutputStream()
//                        image.compressToJpeg(Rect(0, 0, size.width, size.height), 80, stream)
//                        val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
////                        rotateMyBitmap(bmp)
//                        savePreviewPic(bmp, mimageView)
//                        stream.close()
//                    }
                    if(time!=lastTime || (miTime>=5&&!has)) {
                        println("$lastTime  $time  $miTime")
                        if(time!=lastTime) {
                            MyApplication.time = time
                            MyApplication.has = false
                        }
                        else if(miTime>=5&&!has) {
                            MyApplication.has = true
                        }
                        val stream = ByteArrayOutputStream()
                        image.compressToJpeg(Rect(0, 0, size.width, size.height), 80, stream)
                        val bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size())
                        savePreviewPic(bmp, mimageView)
                        stream.close()
                    }
                }
            } catch (ex: java.lang.Exception) {
                Log.e("Sys", "Error:" + ex.message)
            }
        }
    }

    fun stopGetPreviewImage() {
        mCamera!!.stopPreview()
    }

    private fun rotateMyBitmap(bmp: Bitmap) {
        //*****旋转一下
        val matrix = Matrix()
        matrix.postRotate(0f)
//        val bitmap = Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
        val nbmp2 = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        savePreviewPic(nbmp2, mimageView)
    }

    private fun savePreviewPic(data: Bitmap, mimageView: ImageView) {
        val picFile = FileUtil.createCameraFile()
        thread {
            try {
                picFile!!.sink().buffer().write(BitmapUtils.toByteArray(data)).close()
                println(picFile?.absolutePath)
                mHelper.getPrediction(picFile, mimageView, sssh)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //判断是否支持某一对焦模式
    private fun isSupportFocus(focusMode: String): Boolean {
        var autoFocus = false
        val listFocusMode = mParameters.supportedFocusModes
        for (mode in listFocusMode) {
            if (mode == focusMode)
                autoFocus = true
            log("相机支持的对焦模式： $mode")
        }
        return autoFocus
    }

    //切换摄像头
    fun exchangeCamera() {
        releaseCamera()
        mCameraFacing = if (mCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK)
            Camera.CameraInfo.CAMERA_FACING_FRONT
        else
            Camera.CameraInfo.CAMERA_FACING_BACK

        openCamera(mCameraFacing)
        startPreview()
    }

    //释放相机
    fun releaseCamera() {
        if (mCamera != null) {
            // mCamera?.stopFaceDetection()
            mCamera?.stopPreview()
            mCamera?.setPreviewCallback(null)
            mCamera?.release()
            mCamera = null
        }
    }

    //设置预览旋转的角度
    private fun setCameraDisplayOrientation(activity: Activity) {
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(mCameraFacing, info)
        val rotation = activity.windowManager.defaultDisplay.rotation

        var screenDegree = 0
        when (rotation) {
            Surface.ROTATION_0 -> screenDegree = 0
            Surface.ROTATION_90 -> screenDegree = 90
            Surface.ROTATION_180 -> screenDegree = 180
            Surface.ROTATION_270 -> screenDegree = 270
        }

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mDisplayOrientation = (info.orientation + screenDegree) % 360
            mDisplayOrientation = (360 - mDisplayOrientation) % 360          // compensate the mirror
        } else {
            mDisplayOrientation = (info.orientation - screenDegree + 360) % 360
        }
        mCamera?.setDisplayOrientation(mDisplayOrientation)

        log("屏幕的旋转角度 : $rotation")
        log("setDisplayOrientation(result) : $mDisplayOrientation")
    }

    //判断是否支持某个相机
    private fun supportCameraFacing(cameraFacing: Int): Boolean {
        val info = Camera.CameraInfo()
        for (i in 0 until Camera.getNumberOfCameras()) {
            Camera.getCameraInfo(i, info)
            if (info.facing == cameraFacing) return true
        }
        return false
    }


    private fun toast(msg: String) {
        Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show()
    }


    init {
        mSurfaceHolder = mSurfaceView.holder
        init()
    }
}