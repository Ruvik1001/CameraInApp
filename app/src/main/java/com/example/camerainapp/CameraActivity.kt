package com.example.camerainapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import com.example.camerainapp.databinding.ActivityCameraBinding
import java.io.ByteArrayOutputStream
import kotlin.properties.Delegates

class CameraActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var camera: Camera
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var surfaceView: SurfaceView
    private var rotation by Delegates.notNull<Int>()

    fun bind() {
        camera = Camera.open()
        surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (checkCameraPermission()) {
            bind()
        }

        setCameraDisplayOrientation()

        binding.button.isActivated = true
        binding.button.setOnClickListener {

            Log.d("TTT", "BTN_S!")
            takePicture()
            Log.d("TTT", "BTN_E!")


        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d("TTT", "surfaceCreated_S!")
        try {
            camera.setPreviewDisplay(holder)
            camera.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d("TTT", "surfaceCreated_E!")

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d("TTT", "surfaceChanged_S!")
        camera.stopPreview()

        val parameters = camera.parameters
        val supportedSizes = parameters.supportedPreviewSizes
        val optimalSize = getOptimalPreviewSize(supportedSizes, width, height)
        parameters.setPreviewSize(optimalSize.width, optimalSize.height)
        camera.parameters = parameters

        camera.startPreview()
        Log.d("TTT", "surfaceChanged_E!")

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d("TTT", "surfaceDestroyed_S!")
        camera.stopPreview()
        camera.release()
        Log.d("TTT", "surfaceDestroyed_E!")
    }

    private fun getOptimalPreviewSize(sizes: List<Camera.Size>, w: Int, h: Int): Camera.Size {
        Log.d("TTT", "getOptimalPreviewSize_S!")

        val ASPECT_TOLERANCE = 0.1
        val targetRatio = w.toDouble() / h

        var optimalSize: Camera.Size? = null
        var minDiff = Double.MAX_VALUE

        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            val diff = Math.abs(ratio - targetRatio)
            if (diff < minDiff) {
                optimalSize = size
                minDiff = diff
            }
        }
        Log.d("TTT", "getOptimalPreviewSize_E!")

        return optimalSize ?: sizes[0]
    }

    private fun checkCameraPermission(): Boolean {
        Log.d("TTT", "checkCameraPermission_S!")
        Log.d("TTT", "checkCameraPermission_E!")

        return if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 1)
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            false
        }
    }

    private fun createImageFile(): File? {
        Log.d("TTT", "createImageFile_S!")

        val timeStamp = SimpleDateFormat("MM.dd.yyyy_HH.mm.ss").format(Date())
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "date"
        )

        if (!storageDir.exists()) {
            if (!storageDir.mkdirs()) {
                return null
            }
        }

        val imageFileName = "IMG_$timeStamp.jpg"
        val imageFile = File(storageDir, imageFileName)

        Log.d("TTT", "createImageFile_E!")
        return imageFile
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        Log.d("TTT", "bitmapToByteArray_S!")
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream) // You can change the format and quality as needed
        return stream.toByteArray()
        Log.d("TTT", "bitmapToByteArray_E!")
    }

    private fun takePicture() {
        Log.d("TTT", "takePicture_S!")
        camera.takePicture(null, null, Camera.PictureCallback { data, _ ->
            Log.d("TTT", "takePicture_INNER__S!")
            val picture = BitmapFactory.decodeByteArray(data, 0, data.size)
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            val rotatedPicture = Bitmap.createBitmap(picture, 0, 0, picture.width, picture.height, matrix, true)

            try {
                val pictureFile = createImageFile()
                val fos = FileOutputStream(pictureFile)
                fos.write(bitmapToByteArray(rotatedPicture))
                fos.close()
                startActivity(Intent(this, ViewActivity::class.java))
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.d("TTT", "takePicture_INNER__E!")
        })
        Log.d("TTT", "takePicture_E!")
    }

    private fun setCameraDisplayOrientation() {
        Log.d("TTT", "setCameraDisplayOrientation_S!")
        val cameraCount = Camera.getNumberOfCameras()
        val cameraInfo = Camera.CameraInfo()
        var cameraId: Int = 0

        for (i in 0 until cameraCount) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i
                break
            }
        }

        val info = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }


        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            this.rotation = (info.orientation + degrees) % 360
            this.rotation = (360 - this.rotation) % 360
        } else {
            this.rotation = (info.orientation - degrees + 360) % 360
        }

        camera.setDisplayOrientation(this.rotation)
        Log.d("TTT", "setCameraDisplayOrientation_E!")
    }

}