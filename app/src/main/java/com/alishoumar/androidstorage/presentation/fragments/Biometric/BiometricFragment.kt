package com.alishoumar.androidstorage.presentation.fragments.Biometric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.alishoumar.androidstorage.R
import com.alishoumar.androidstorage.databinding.FragmentBiometricBinding
import com.alishoumar.androidstorage.presentation.fragments.shared.AuthSharedViewModel
import com.alishoumar.androidstorage.presentation.utils.biometric.BiometricResult
import com.alishoumar.androidstorage.presentation.utils.biometric.BiometricUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BiometricFragment : Fragment() {

    private var _binding: FragmentBiometricBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BiometricViewModel by viewModels()
    private val authSharedViewModel: AuthSharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBiometricBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val promptManager = BiometricUtils(requireContext())

        binding.ibBiometric.setOnClickListener {
            promptManager.showBiometricPrompt("Login", "Login to continue", this) {
                viewModel.sendBiometricResult(it)
            }
        }
        lifecycleScope.launch {
            viewModel.biometricFlow.collect { result ->
                when (result) {
                    is BiometricResult.AuthenticationSuccess -> {

                        binding.tvBiometric.text = "Authentication successful! Redirecting..."
                        binding.tvBiometric.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.success_green
                            )
                        )
                        authSharedViewModel.setAuthentication(true)

                        findNavController().navigate(
                            R.id.action_biometricFragment_to_internalStorageFragment,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.biometricFragment, true)
                                .build()
                        )
                    }

                    is BiometricResult.AuthenticationError -> {
                        println(result.error)
                    }

                    is BiometricResult.FeatureUnavailable -> {
                        binding.tvBiometric.text =
                            "Your device does not support biometric authentication."
                        binding.tvBiometric.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.error_red
                            )
                        )
                    }

                    is BiometricResult.HardwareUnavailable -> {
                        binding.tvBiometric.text = "Biometric hardware is currently unavailable."
                        binding.tvBiometric.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.error_red
                            )
                        )
                    }

                    is BiometricResult.AuthenticationNotSet -> {
                        binding.tvBiometric.text =
                            "No biometrics enrolled. Please set up biometrics in your device settings."
                        binding.tvBiometric.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.warning_yellow
                            )
                        )
                    }

                    is BiometricResult.AuthenticationFailed -> {
                        binding.tvBiometric.text = "Authentication failed. Please try again."
                        binding.tvBiometric.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.error_red
                            )
                        )
                    }
                }
            }
        }
    }
}