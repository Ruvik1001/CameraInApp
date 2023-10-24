package com.example.MyPhoto

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import com.example.MyPhoto.databinding.ActivityMyCameraBinding
import java.io.ByteArrayOutputStream
import kotlin.properties.Delegates

class MyCameraActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var binding: ActivityMyCameraBinding
    private lateinit var camera: Camera
    private lateinit var surfaceHolder: SurfaceHolder
    private lateinit var surfaceView: SurfaceView
    private var rotation by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkCameraPermission()) {
            camera = Camera.open()
            surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
            surfaceHolder = surfaceView.holder
            surfaceHolder.addCallback(this)
            setCameraDisplayOrientation()
        }

        binding.button.setOnClickListener {
            takePicture()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        try {
            camera.setPreviewDisplay(holder)
            camera.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        camera.stopPreview()

        val parameters = camera.parameters
        val supportedSizes = parameters.supportedPreviewSizes
        val optimalSize = getOptimalPreviewSize(supportedSizes, width, height)
        parameters.setPreviewSize(optimalSize.width, optimalSize.height)
        camera.parameters = parameters

        camera.startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera.stopPreview()
        camera.release()
    }

    private fun getOptimalPreviewSize(sizes: List<Camera.Size>, w: Int, h: Int): Camera.Size {
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

        return optimalSize ?: sizes[0]
    }

    private fun checkCameraPermission(): Boolean {
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

    private fun takePicture() {
        camera.takePicture(null, null, Camera.PictureCallback { data, _ ->
            val picture = BitmapFactory.decodeByteArray(data, 0, data.size)
            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())
            val rotatedPicture = Bitmap.createBitmap(picture, 0, 0, picture.width, picture.height, matrix, true)

            try {
                val pictureFile = createImageFile()
                val fos = FileOutputStream(pictureFile)
                fos.write(bitmapToByteArray(rotatedPicture))
                fos.close()
                startActivity(Intent(this, ListPhotosActivity::class.java))
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun createImageFile(): File? {
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

        return imageFile
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }

    private fun setCameraDisplayOrientation() {
        val count = Camera.getNumberOfCameras()
        val info = Camera.CameraInfo()
        var cameraId: Int = 0
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0

        for (i in 0 until count) {
            Camera.getCameraInfo(i, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i
                break
            }
        }

        Camera.getCameraInfo(cameraId, info)
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
    }
}
