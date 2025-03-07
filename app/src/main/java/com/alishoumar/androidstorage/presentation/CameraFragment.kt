package com.alishoumar.androidstorage.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import com.alishoumar.androidstorage.databinding.FragmentCameraBinding

class CameraFragment : Fragment() {


    private var _binding:FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CameraViewModel by viewModels()
    private lateinit var cameraController: LifecycleCameraController

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


        binding.ibTakePhoto.setOnClickListener{
            takePhoto()
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
}