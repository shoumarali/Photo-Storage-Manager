package com.alishoumar.androidstorage.presentation.fragments.Camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.alishoumar.androidstorage.databinding.FragmentCameraBinding
import com.alishoumar.androidstorage.presentation.utils.FileUtils.createFileName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CameraViewModel by viewModels()
    private lateinit var cameraController: LifecycleCameraController

    private var canTakePhoto = true
    private var savePhotoInInternalStorage = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            if (hasCameraPermission()) {
                startCamera()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }, 300)

        binding.ibPrivatePhoto.setOnClickListener {
            savePhotoInInternalStorage = !savePhotoInInternalStorage
            Toast.makeText(
                requireContext(),
                "Private mode: $savePhotoInInternalStorage",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.ibTakePhoto.isEnabled = canTakePhoto
        binding.ibTakePhoto.setOnClickListener {
            if (canTakePhoto) {
                takePhoto()
            }
        }

        binding.ibSwitchMode.setOnClickListener {
            switchCamera()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        cameraController = LifecycleCameraController(requireContext()).apply {
            setEnabledUseCases(
                LifecycleCameraController.IMAGE_CAPTURE or LifecycleCameraController.VIDEO_CAPTURE
            )
        }

        binding.cameraPreview.controller = cameraController
        cameraController.bindToLifecycle(viewLifecycleOwner)
    }

    private fun takePhoto() {
        canTakePhoto = false
        binding.ibTakePhoto.isEnabled = false

        cameraController.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val frontCameraOrBack =
                            cameraController.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

                        if (savePhotoInInternalStorage) {
                            viewModel.savePhotoToInternalStorage(
                                createFileName(
                                    isSavingEncrypted = true,
                                    width = image.width,
                                    height = image.height
                                ),
                                image,
                                frontCameraOrBack
                            )
                        } else {
                            viewModel.savePhotoToExternalStorage(
                                createFileName(),
                                image,
                                frontCameraOrBack
                            )
                        }

                        Toast.makeText(requireContext(), "Photo saved", Toast.LENGTH_SHORT).show()
                    } finally {
                        image.close()
                    }
                    canTakePhoto = true;
                    Handler(Looper.getMainLooper()).postDelayed({
                        canTakePhoto = true
                        binding.ibTakePhoto.isEnabled = true
                    }, 1500)
                }

                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Toast.makeText(requireContext(), "Capture failed", Toast.LENGTH_SHORT).show()
                    canTakePhoto = true
                    binding.ibTakePhoto.isEnabled = true
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
