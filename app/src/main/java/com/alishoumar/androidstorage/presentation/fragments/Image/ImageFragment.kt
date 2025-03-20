package com.alishoumar.androidstorage.presentation.fragments.Image

import android.app.Activity.RESULT_OK
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.alishoumar.androidstorage.R
import com.alishoumar.androidstorage.databinding.FragmentImageBinding
import com.alishoumar.androidstorage.domain.models.ExternalStoragePhoto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ImageFragment : Fragment() {

    private var _binding: FragmentImageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ImageViewModel by viewModels()
    private lateinit var intentSenderLauncher:  ActivityResultLauncher<IntentSenderRequest>
    private var deletedPhotoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentImageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photo = arguments?.getParcelable<ExternalStoragePhoto>("photo")

        val actionToolBar =  binding.tlImageFragment
        actionToolBar.navigationIcon = ContextCompat.getDrawable(
            requireContext()
            ,R.drawable.baseline_arrow_back_24
        )
        (activity as AppCompatActivity).setSupportActionBar(actionToolBar)
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        actionToolBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        intentSenderLauncher = registerForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
            callback = {
                if (it.resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        lifecycleScope.launch {
                            deletePhotoFromExternalStorage(deletedPhotoUri ?: return@launch)
                        }
                    }
                    findNavController().popBackStack()
                }
            }
        )

        binding.ibDeletePhoto.setOnClickListener {
            lifecycleScope.launch {
                deletePhotoFromExternalStorage(photo?.uri!!)
                deletedPhotoUri = photo.uri
            }
        }

        if (photo != null) {
            binding.ivOpenedImage.load(photo.uri) {
                crossfade(true)
            }
        }
    }

    private suspend fun deletePhotoFromExternalStorage(photoUri: Uri){
        withContext (Dispatchers.IO){
            viewModel.deletePhoto(photoUri)?.let {sender ->
                intentSenderLauncher.launch(
                    IntentSenderRequest.Builder(sender).build()
                )
            }
        }
    }
}