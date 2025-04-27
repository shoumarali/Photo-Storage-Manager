package com.alishoumar.androidstorage.presentation.fragments.internalStorage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alishoumar.androidstorage.R
import com.alishoumar.androidstorage.data.utils.CryptoManager
import com.alishoumar.androidstorage.databinding.FragmentInternalStorageBinding
import com.alishoumar.androidstorage.presentation.adapter.InternalStoragePhotoAdapter
import com.alishoumar.androidstorage.presentation.adapter.SpaceItemDecoration
import com.alishoumar.androidstorage.presentation.fragments.shared.AuthSharedViewModel
import com.alishoumar.androidstorage.presentation.fragments.shared.ImageRefreshViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class InternalStorageFragment : Fragment() {


    private var _binding: FragmentInternalStorageBinding? = null
    private val binding get() = _binding!!

    private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter
    private lateinit var itemDecoration : SpaceItemDecoration
    private val internalStorageViewModel: InternalStorageViewModel by viewModels()
    private val imageRefreshViewModel: ImageRefreshViewModel by activityViewModels()
    private val authSharedViewModel: AuthSharedViewModel by activityViewModels()

    @Inject
    lateinit var cryptoManager: CryptoManager;


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInternalStorageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authSharedViewModel.isAuthenticated.observe(viewLifecycleOwner) {
            if (!it) {
                findNavController().navigate(R.id.action_internalStorageFragment_to_biometricFragment)
            }
            internalStoragePhotoAdapter = InternalStoragePhotoAdapter(
                cryptoManager
            ) {
                internalStorageViewModel.deletePhotoFromInternalStorage(it.name)
            }
            itemDecoration = SpaceItemDecoration(4)
            setUpRecyclerView()
            setUpObservables()
        }
    }

    private fun setUpRecyclerView(){
        binding.rvPrivatePhotos.apply{
            adapter = internalStoragePhotoAdapter
            layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
            addItemDecoration(itemDecoration)
        }
    }

    private fun setUpObservables(){

        internalStorageViewModel.internalPhotos.observe(viewLifecycleOwner) {
            internalStoragePhotoAdapter.submitList(it)
        }

        imageRefreshViewModel.internalStorageChanged.observe(viewLifecycleOwner) {
            internalStorageViewModel.loadPhotosFromInternalStorage()
        }
    }
}