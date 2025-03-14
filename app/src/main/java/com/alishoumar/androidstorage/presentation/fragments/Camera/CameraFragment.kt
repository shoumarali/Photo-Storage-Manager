package com.alishoumar.androidstorage.presentation.fragments.Camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.alishoumar.androidstorage.databinding.FragmentCameraBinding
import com.alishoumar.androidstorage.presentation.utils.FileUtils.createFileName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraFragment : Fragment() {


    private var _binding:FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CameraViewModel by viewModels()
    private lateinit var cameraController: LifecycleCameraController

    private var canTakePhoto = true
    private var savePhotoInInternalStorage = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


         val requestPermissionLauncher = registerForActivityResult(
             ActivityResultContracts.RequestPermission()
         ) { isGranted ->
                if (isGranted) {
                    startCamera()
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }

        cameraController = LifecycleCameraController(
            requireContext()
        ).apply {
            setEnabledUseCases(
                LifecycleCameraController.IMAGE_CAPTURE or LifecycleCameraController.VIDEO_CAPTURE
            )
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.ibPrivatePhoto.setOnClickListener {

            savePhotoInInternalStorage= !savePhotoInInternalStorage
            Toast.makeText(
                requireContext(),
                "$savePhotoInInternalStorage",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.ibTakePhoto.isEnabled = canTakePhoto
        binding.ibTakePhoto.setOnClickListener{
            takePhoto()
            Toast.makeText(requireContext(),
                "Private mode is : $savePhotoInInternalStorage",
                Toast.LENGTH_SHORT).show()
        }

        binding.ibSwitchMode.setOnClickListener {
            switchCamera()
        }
    }
    private fun startCamera(){
        binding.cameraPreview.controller = cameraController
        cameraController.bindToLifecycle(this)
    }

    private fun takePhoto(){
        cameraController.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback(){
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    canTakePhoto = false

                    val frontCameraOrBack = cameraController.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

                    if(savePhotoInInternalStorage) {
                        viewModel.savePhotoToInternalStorage(
                            createFileName(),
                            image,
                            frontCameraOrBack
                        )
                    }else{
                        viewModel.savePhotoToExternalStorage(
                            createFileName(),
                            image,
                            frontCameraOrBack
                        )
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        canTakePhoto = true
                    },2000)
                    Toast.makeText(requireContext(),"photo saved", Toast.LENGTH_SHORT).show()
                }
                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    private fun switchCamera() {
        cameraController.cameraSelector = if (
            cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA
            ) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraController.unbind()
    }
}