package com.alishoumar.androidstorage.presentation

import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alishoumar.androidstorage.R
import com.alishoumar.androidstorage.databinding.FragmentExternalStorageBinding
import com.alishoumar.androidstorage.presentation.adapter.SharedStoragePhotoAdapter
import com.alishoumar.androidstorage.presentation.adapter.SpaceItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
@AndroidEntryPoint
class ExternalStorageFragment : Fragment() {

    private var _binding:FragmentExternalStorageBinding? =null
    private val binding get() = _binding!!

    private val externalStorageViewModel: ExternalStorageViewModel by viewModels()
    private lateinit var externalStoragePhotoAdapter: SharedStoragePhotoAdapter
    private lateinit var contentObserver : ContentObserver
    private lateinit var itemDecoration : SpaceItemDecoration
    private var deletedPhotoUri: Uri? = null
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
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
        itemDecoration = SpaceItemDecoration(6)
        externalStoragePhotoAdapter = SharedStoragePhotoAdapter {
            lifecycleScope.launch {
                deletePhotoFromExternalStorage(it.uri)
                deletedPhotoUri = it.uri
            }
        }

        intentSenderLauncher =registerForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult(),
            callback = {
                if (it.resultCode == RESULT_OK) {
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        lifecycleScope.launch {
                            deletePhotoFromExternalStorage(deletedPhotoUri ?: return@launch)
                        }
                    }
                }
            }
        )
        registerPermissions = registerForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            callback = {}
        )

        setUpRecyclerView()
        setUpObservables()
        initContentObserver()
    }

    private fun setUpRecyclerView(){
        binding.rvPublicPhotos.apply{
            adapter = externalStoragePhotoAdapter
            layoutManager = StaggeredGridLayoutManager(3, RecyclerView.VERTICAL)
            addItemDecoration(itemDecoration)

        }
    }

    private fun setUpObservables(){
        externalStorageViewModel.unGrantedPermissions.observe(viewLifecycleOwner){
            if(it.isNotEmpty())
                registerPermissions.launch(it.toTypedArray())
        }

        externalStorageViewModel.externalStoragePhotos.observe(viewLifecycleOwner){
            externalStoragePhotoAdapter.submitList(it)
        }
    }

    private suspend fun deletePhotoFromExternalStorage(photoUri: Uri){
        withContext (Dispatchers.IO){
            externalStorageViewModel.deletePhotoFromExternalStorage(photoUri)?.let {sender ->
                intentSenderLauncher.launch(
                    IntentSenderRequest.Builder(sender).build()
                )
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