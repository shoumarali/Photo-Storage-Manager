package com.alishoumar.androidstorage.presentation.fragments.Camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
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
import com.alishoumar.androidstorage.R
import com.alishoumar.androidstorage.databinding.FragmentCameraBinding
import com.alishoumar.androidstorage.presentation.utils.FileUtils.createFileName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CameraViewModel by viewModels()

    private var isPrivateMode = false

    private lateinit var cameraManager: CameraManager;

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraManager.startCamera()
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
        cameraManager = CameraManager(
            context = requireContext(),
            lifecycleOwner = viewLifecycleOwner,
            previewView = binding.cameraPreview
        )

        Handler(Looper.getMainLooper()).postDelayed({
            if (hasCameraPermission()) {
               cameraManager.startCamera()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }, 300)

        binding.ibPrivatePhoto.setOnClickListener {
            isPrivateMode = !isPrivateMode
            updatePrivateModeUi()
        }

        binding.ibTakePhoto.isEnabled = cameraManager.canTakePhoto
        binding.ibTakePhoto.setOnClickListener {
            if (cameraManager.canTakePhoto) {
                takePhoto()
            }
        }
        var isRotated = false;
        binding.ibSwitchMode.setOnClickListener {
            cameraManager.switchCamera()

            val animRes = if (isRotated) R.anim.rotate_180_backward else R.anim.rotate_180_forward
            val animation = AnimationUtils.loadAnimation(context, animRes)
            binding.ibSwitchMode.startAnimation(animation)

            isRotated = !isRotated
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun takePhoto() {
        AnimationUtils.loadAnimation(context, R.anim.pulse_scale).also {
            binding.ibTakePhoto.startAnimation(it)
        }
        cameraManager.takePhoto(
            binding.ibTakePhoto,
            binding.flashOverlay){ image, frontCameraOrBack ->

            if (isPrivateMode) {
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
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updatePrivateModeUi() {
        binding.ibPrivatePhoto.isSelected = isPrivateMode
        AnimationUtils.loadAnimation(context, R.anim.pulse_scale).also {
            binding.ibPrivatePhoto.startAnimation(it)
        }
    }
}
