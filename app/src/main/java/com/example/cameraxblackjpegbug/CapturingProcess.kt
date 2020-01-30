package com.example.cameraxblackjpegbug

import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

class CapturingProcess(context: Context, lifecycleOwner: LifecycleOwner) {

    private var context: Context = context

    private var outputDirectory: File
    private var mainExecutor: Executor

    private lateinit var cameraControl: CameraControl
    private lateinit var cameraInfo: CameraInfo
    private var capture: ImageCapture? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK

    private var lifecycleOwner: LifecycleOwner = lifecycleOwner

    fun requestPicture() {
        capture?.let { imageCapture ->

            val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

            val metadata = ImageCapture.Metadata().apply {

                isReversedHorizontal = lensFacing == CameraSelector.LENS_FACING_FRONT
            }

            imageCapture.takePicture(photoFile, metadata, mainExecutor, imageSavedListener)
        }
    }

    private fun bindCameraUseCases() {

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this.context)
        cameraProviderFuture.addListener(Runnable {

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            capture = ImageCapture.Builder()
                .setTargetName("Capture")
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            cameraProvider.unbindAll()

            try {
                val camera =
                    cameraProvider.bindToLifecycle(this.lifecycleOwner, cameraSelector, capture)
                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo
            } catch (e: Exception) {
                throw IllegalStateException(e)
            }

        }, this.mainExecutor)
    }

    private val imageSavedListener = object : ImageCapture.OnImageSavedCallback {

        override fun onError(imageCaptureError: Int, message: String, cause: Throwable?) {
            Log.e(TAG, "Photo capture failed: $message")
        }

        override fun onImageSaved(photoFile: File) {

            Log.d(TAG, "Photo capture succeeded: ${photoFile.absolutePath}")

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                context.sendBroadcast(
                    Intent("android.hardware.action.NEW_PICTURE", Uri.fromFile(photoFile))
                )
            }

            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(photoFile.extension)
            MediaScannerConnection.scanFile(
                context, arrayOf(photoFile.absolutePath), arrayOf(mimeType), null
            )
        }
    }

    companion object {
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val TAG = "CapturingProcess"


        private fun createFile(
            baseFolder: File,
            @Suppress("SameParameterValue") format: String,
            @Suppress("SameParameterValue") extension: String
        ) = File(
            baseFolder,
            SimpleDateFormat(
                format,
                Locale.getDefault()
            ).format(System.currentTimeMillis()) + extension
        )

        fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, context.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists()) {
                mediaDir
            } else {
                appContext.filesDir
            }
        }
    }

    init {
        this.mainExecutor = ContextCompat.getMainExecutor(context)
        outputDirectory = getOutputDirectory(context)
        bindCameraUseCases()
    }
}
