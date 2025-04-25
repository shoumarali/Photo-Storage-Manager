package com.alishoumar.androidstorage.presentation.fragments.Camera

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

class CameraManager (
    private val context:Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
){

    private lateinit var cameraController: LifecycleCameraController
    var canTakePhoto = true

    fun startCamera() {
        cameraController = LifecycleCameraController(context).apply {
            setEnabledUseCases(
                LifecycleCameraController.IMAGE_CAPTURE or LifecycleCameraController.VIDEO_CAPTURE
            )
        }
        previewView.controller = cameraController
        cameraController.bindToLifecycle(lifecycleOwner)
    }

    fun switchCamera() {
        cameraController.cameraSelector = if (
            cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA
        ) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    fun takePhoto(
        takePhotoButton: ImageButton,
        flashOverlay : View,
       savePhoto: (imageProxy: ImageProxy, frontCameraOrBack: Boolean) -> Unit
    ) {
        canTakePhoto = false
        takePhotoButton.isEnabled = false


        flashOverlay.visibility = View.VISIBLE
        flashOverlay.alpha = 1f

        flashOverlay.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                flashOverlay.visibility = View.INVISIBLE
                captureImage(takePhotoButton,savePhoto)
            }
            .start()
    }

    private fun captureImage(
        takePhotoButton: ImageButton,
        savePhoto: (imageProxy: ImageProxy, frontCameraOrBack: Boolean) -> Unit
    ) {
        cameraController.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val frontCameraOrBack = cameraController.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA
                        savePhoto(image , frontCameraOrBack)
                    } catch (e: Exception) {
                        image.close()
                    }
                    canTakePhoto = true;
                    Handler(Looper.getMainLooper()).postDelayed({
                        canTakePhoto = true
                        takePhotoButton.isEnabled = true
                    }, 700)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Toast.makeText(context, "Capture failed", Toast.LENGTH_SHORT).show()
                    canTakePhoto = true
                    takePhotoButton.isEnabled = true
                }
            }
        )

    }
}