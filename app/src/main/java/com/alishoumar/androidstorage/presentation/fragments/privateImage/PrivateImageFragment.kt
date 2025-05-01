package com.alishoumar.androidstorage.presentation.fragments.privateImage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.load
import com.alishoumar.androidstorage.R
import com.alishoumar.androidstorage.data.utils.CryptoManager
import com.alishoumar.androidstorage.databinding.FragmentPrivateImageBinding
import com.alishoumar.androidstorage.presentation.adapter.fetcher.EncryptedImageFetcherFactory
import com.alishoumar.androidstorage.presentation.fragments.shared.ImageRefreshViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PrivateImageFragment : Fragment() {

    @Inject
    lateinit var cryptoManager: CryptoManager

    private var _binding: FragmentPrivateImageBinding? = null
    private val binding get() = _binding!!
    private val privateImageViewModel: PrivateImageViewModel by viewModels()
    private val imageRefreshViewModel: ImageRefreshViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPrivateImageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imagePath = arguments?.getString("filePath")
        val imageName = arguments?.getString("fileName")

        val actionToolBar =  binding.tlImageFragment
        actionToolBar.navigationIcon = ContextCompat.getDrawable(
            requireContext()
            , R.drawable.baseline_arrow_back_24
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


        binding.ibDeletePhoto.setOnClickListener {
            lifecycleScope.launch {
                privateImageViewModel.deletePhotoFromInternalStorage(imageName!!){ onSuccess ->
                    if(onSuccess){
                        imageRefreshViewModel.notifyInternalStorageChanged()
                        findNavController().popBackStack()
                    }
                }
            }
        }


        val imageLoader = ImageLoader.Builder(requireContext())
            .components {
                add(
                    EncryptedImageFetcherFactory(
                        cryptoManager,
                        requireContext())
                )
            }
            .build()

        if (imagePath != null) {
            binding.ivOpenedImage.load(imagePath, imageLoader) {
                crossfade(true)
            }
        }
    }
}