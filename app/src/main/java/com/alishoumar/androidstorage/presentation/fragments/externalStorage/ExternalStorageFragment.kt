package com.alishoumar.androidstorage.presentation.fragments.externalStorage

import android.database.ContentObserver
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alishoumar.androidstorage.R
import com.alishoumar.androidstorage.databinding.FragmentExternalStorageBinding
import com.alishoumar.androidstorage.presentation.adapter.SharedStoragePhotoAdapter
import com.alishoumar.androidstorage.presentation.adapter.SpaceItemDecoration
import com.alishoumar.androidstorage.presentation.utils.permission.PermissionsUtil
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class ExternalStorageFragment : Fragment() {

    private var _binding:FragmentExternalStorageBinding? =null
    private val binding get() = _binding!!

    private val externalStorageViewModel: ExternalStorageViewModel by viewModels()
    private lateinit var externalStoragePhotoAdapter: SharedStoragePhotoAdapter
    private lateinit var contentObserver : ContentObserver
    private lateinit var itemDecoration : SpaceItemDecoration
    private lateinit var registerPermissions: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExternalStorageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemDecoration = SpaceItemDecoration(4)

        externalStoragePhotoAdapter = SharedStoragePhotoAdapter (
            viewLifecycleOwner,
        ){
            val bundle = Bundle().apply {
                putParcelable("photo",it)
            }

            findNavController().navigate(
                R.id.action_externalStorageFragment_to_imageFragment,
                bundle
            )
        }

        registerPermissions = registerForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            callback = { permissions ->
                val allGranted = permissions.all { it.value }
                if( allGranted ){
                    setUpRecyclerView()
                    setUpObservables()
                    initContentObserver()
                }else{
                    Toast.makeText(
                        requireContext(),
                        "Allow storage access to view external images",
                        Toast.LENGTH_SHORT).show()
                }
            })

        val unGrantedPermissions = PermissionsUtil.getUnGrantedPermissions(requireContext())

        setUpRecyclerView()
        if (unGrantedPermissions.isEmpty()) {
            setUpObservables()
            initContentObserver()
        } else {
            registerPermissions.launch(unGrantedPermissions.toTypedArray())
        }
    }

    private fun setUpRecyclerView(){
        binding.rvPublicPhotos.apply{
            setItemViewCacheSize(40)
            setHasFixedSize(true)
            recycledViewPool.setMaxRecycledViews(0,40)
            adapter = externalStoragePhotoAdapter
            layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
            addItemDecoration(itemDecoration)
            setHasFixedSize(true)
        }
    }

    private fun setUpObservables(){


        externalStorageViewModel.externalStoragePhotos.observe(viewLifecycleOwner) { newList ->
            if (externalStoragePhotoAdapter.currentList != newList) {
                externalStoragePhotoAdapter.submitList(newList)
            }
        }
    }


    private fun initContentObserver(){
        contentObserver = object : ContentObserver(null){
            override fun onChange(selfChange: Boolean) {
                externalStorageViewModel.loadPhotosFromExternalStorage()
            }
        }
        context?.contentResolver?.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.contentResolver?.unregisterContentObserver(contentObserver)
    }
}