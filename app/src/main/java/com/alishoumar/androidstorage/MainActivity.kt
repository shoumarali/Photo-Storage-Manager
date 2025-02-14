package com.alishoumar.androidstorage

import android.Manifest
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alishoumar.androidstorage.presentation.adapter.InternalStoragePhotoAdapter
import com.alishoumar.androidstorage.presentation.adapter.SharedStoragePhotoAdapter
import com.alishoumar.androidstorage.presentation.adapter.SpaceItemDecoration
import com.alishoumar.androidstorage.databinding.ActivityMainBinding
import com.alishoumar.androidstorage.presentation.ExternalStorageViewModel
import com.alishoumar.androidstorage.presentation.InternalStorageViewModel
import com.alishoumar.androidstorage.presentation.util.sdk29AndAbove
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter
    private lateinit var externalStoragePhotoAdapter: SharedStoragePhotoAdapter

    private var isReadPermissionGranted:Boolean = false
    private var isWritePermissionGranted:Boolean = false

    private lateinit var permissionLauncher : ActivityResultLauncher<Array<String>>
    private lateinit var contentObserver : ContentObserver
    private lateinit var itemDecoration : SpaceItemDecoration

    private val internalStorageViewModel: InternalStorageViewModel by viewModels()
    private val externalStorageViewModel: ExternalStorageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root){v , insets->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        itemDecoration = SpaceItemDecoration(16)

        internalStoragePhotoAdapter = InternalStoragePhotoAdapter {
            internalStorageViewModel.deletePhotoFromInternalStorage(it.name)
        }


        externalStoragePhotoAdapter = SharedStoragePhotoAdapter {

        }

        permissionLauncher = registerForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            callback = {
                isReadPermissionGranted = it[Manifest.permission.READ_EXTERNAL_STORAGE]?:false
                isWritePermissionGranted = it[Manifest.permission.WRITE_EXTERNAL_STORAGE]?:false

                if(isReadPermissionGranted){
                    loadPhotosFromExternalStorage()
                }else{
                    Toast.makeText(this, "Cannot read files from external storage", Toast.LENGTH_SHORT).show()
                }
            }
        )
        updateOrRequestPermissions()
        setUpRecyclerViews()
        setUpObservables()
        initContentObserver()
        loadPhotosFromExternalStorage()



        val takePhoto = registerForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview(),
            callback = {
                lifecycleScope.launch {
                    val isPrivate = binding.switchPrivate.isChecked
                    when {
                        isPrivate -> internalStorageViewModel.savePhotoToInternalStorage(
                            UUID.randomUUID().toString(),
                            it!!)

                        isWritePermissionGranted -> savePhotoToExternalStorage(
                            UUID.randomUUID().toString(), bmp = it!!
                        )
                    }
                }
            }
        )
        binding.btnTakePhoto.setOnClickListener {
            takePhoto.launch()
        }
    }


    private fun setUpRecyclerViews(){
        binding.rvPrivatePhotos.apply{
            adapter = internalStoragePhotoAdapter
            layoutManager = StaggeredGridLayoutManager(3,RecyclerView.VERTICAL)
            addItemDecoration(itemDecoration)
        }
        binding.rvPublicPhotos.apply{
            adapter = externalStoragePhotoAdapter
            layoutManager = StaggeredGridLayoutManager(3,RecyclerView.VERTICAL)
            addItemDecoration(itemDecoration)

        }
    }
    private fun setUpObservables(){
        internalStorageViewModel.internalPhotos.observe(this) {
            internalStoragePhotoAdapter.submitList(it)
        }
        externalStorageViewModel.externalStoragePhotos.observe(this){
            externalStoragePhotoAdapter.submitList(it)
        }
    }

    private fun savePhotoToExternalStorage(displayName:String , bmp: Bitmap){
            val imageCollection = sdk29AndAbove {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

             externalStorageViewModel.savePhotoToExternalStorage(
                imageCollection,
                displayName,
                bmp
            )
    }


    private fun loadPhotosFromExternalStorage(){

            val collection = sdk29AndAbove {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
         externalStorageViewModel.loadPhotosFromExternalStorage(collection)
        }


    private fun initContentObserver(){
        contentObserver = object : ContentObserver(null){
            override fun onChange(selfChange: Boolean) {
                if(isReadPermissionGranted){
                    loadPhotosFromExternalStorage()
                }
            }
        }
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)
    }

    private fun updateOrRequestPermissions(){
        val hasReadPermission = ContextCompat
            .checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat
            .checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        isReadPermissionGranted = hasReadPermission
        isWritePermissionGranted = hasWritePermission || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if(!isReadPermissionGranted){
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(!isWritePermissionGranted){
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(permissionsToRequest.isNotEmpty()){
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}