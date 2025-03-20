package com.alishoumar.androidstorage.presentation.fragments.Image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import com.alishoumar.androidstorage.databinding.FragmentImageBinding
import com.alishoumar.androidstorage.domain.models.ExternalStoragePhoto
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageFragment : Fragment() {

    private var _binding: FragmentImageBinding? = null
    private val binding get() = _binding!!

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
        if (photo != null) {
            binding.ivOpenedImage.load(photo.uri) {
                crossfade(true)
            }
        }
    }
}