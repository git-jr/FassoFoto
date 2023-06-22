package com.paradoxo.fassofoto.camerax

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.paradoxo.fassofoto.databinding.ActivityCameraXactivityBinding
import com.paradoxo.fassofoto.extensions.showToast
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraXActivity : androidx.activity.ComponentActivity() {

    private lateinit var viewBinding: ActivityCameraXactivityBinding

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private val pickImagesLauncher = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri>? ->
        uris?.let {
            Toast.makeText(this, "Files selected: ${uris.size}", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            showToast("Não será possivel usar o app sem essa permissão")
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCameraXactivityBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionsLauncher.launch(REQUIRED_PERMISSIONS)
        }

        setTakePhotoButton()
        setShownButtonImages()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setShownButtonImages() {
        viewBinding.videoCaptureButton.setOnClickListener {
            showRecentPhotos()
        }
    }

    private fun setTakePhotoButton() {
        viewBinding.imageCaptureButton.setOnClickListener {
            takePhoto()
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FassoFoto")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
    }

    private fun showRecentPhotos() {
        pickImagesLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "FassoFoto"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
}