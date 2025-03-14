package com.alishoumar.androidstorage.presentation.fragments.Biometric

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
                        authSharedViewModel.setAuthentication(true)

                        findNavController().navigate(
                            R.id.action_biometricFragment_to_internalStorageFragment,
                            null,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.biometricFragment, true)
                                .build())
                    }
                    is BiometricResult.AuthenticationError -> {
                        showToast("Not recognized")
                        println(result.error)
                    }
                    is BiometricResult.FeatureUnavailable -> showToast("Please enable biometrics in your device")
                    is BiometricResult.HardwareUnavailable -> showToast("Hardware unavailable")
                    is BiometricResult.AuthenticationNotSet -> showToast("Set Authentication")
                    else -> Toast.makeText(requireContext(),"something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    
    private fun showToast(
        str: String
    ){
        Toast.makeText(requireContext(),str, Toast.LENGTH_SHORT).show()
    }
}