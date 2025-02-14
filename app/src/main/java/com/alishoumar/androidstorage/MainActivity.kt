package com.alishoumar.androidstorage

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
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
import com.alishoumar.androidstorage.domain.models.ExternalStoragePhoto
import com.alishoumar.androidstorage.databinding.ActivityMainBinding
import com.alishoumar.androidstorage.util.sdk24AndAbove
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
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
    private lateinit var intentSenderLauncher : ActivityResultLauncher<IntentSenderRequest>

    private val fooViewModel: StorageViewModel by viewModels()

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
            fooViewModel.deletePhotoFromInternalStorage(it.name)
        }


        externalStoragePhotoAdapter = SharedStoragePhotoAdapter {

        }

        permissionLauncher = registerForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            callback = {
                isReadPermissionGranted = it[Manifest.permission.READ_EXTERNAL_STORAGE]?:false
                isWritePermissionGranted = it[Manifest.permission.WRITE_EXTERNAL_STORAGE]?:false

                if(isReadPermissionGranted){
                    loadPhotosFromExternalStorageToRecyclerView()
                }else{
                    Toast.makeText(this, "Cannot read files from external storage", Toast.LENGTH_SHORT).show()
                }
            }
        )
        updateOrRequestPermissions()
        setUpRecyclerViews()
        setUpObservables()
        initContentObserver()
        loadPhotosFromExternalStorageToRecyclerView()

        val takePhoto = registerForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview(),
            callback = {
                lifecycleScope.launch {
                    val isPrivate = binding.switchPrivate.isChecked
                    when {
                        isPrivate -> fooViewModel.savePhotoToInternalStorage(
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
        fooViewModel.internalPhotos.observe(this) {
            internalStoragePhotoAdapter.submitList(it)
        }
    }

    private fun loadPhotosFromExternalStorageToRecyclerView(){
        lifecycleScope.launch {
         val photos = loadPhotosFromExternalStorage()
         externalStoragePhotoAdapter.submitList(photos)
        }
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

    private suspend fun savePhotoToExternalStorage(displayName:String , bmp: Bitmap):Boolean{
        return withContext(Dispatchers.IO) {
            val imageCollection = sdk24AndAbove {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
                put(MediaStore.Images.Media.WIDTH, bmp.width)
                put(MediaStore.Images.Media.HEIGHT, bmp.height)
            }
            try {
                contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                    contentResolver.openOutputStream(uri)?.use {
                        if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, it)) {
                            throw IOException("Couldn't save the bitmap")
                        }
                    }
                } ?: throw IOException("Couldn't create media store entry")
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun initContentObserver(){
        contentObserver = object : ContentObserver(null){
            override fun onChange(selfChange: Boolean) {
                if(isReadPermissionGranted){
                    loadPhotosFromExternalStorageToRecyclerView()
                }
            }
        }
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
    }

    private suspend fun loadPhotosFromExternalStorage():List<ExternalStoragePhoto>{
        return withContext(Dispatchers.IO){
            val collection = sdk24AndAbove {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT
            )

            val photos = mutableListOf<ExternalStoragePhoto>()

            contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
            ).use {cursor ->

                if(cursor != null) {

                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                    val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                    val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val displayName = cursor.getString(displayNameColumn)
                        val width = cursor.getInt(widthColumn)
                        val height = cursor.getInt(heightColumn)
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        photos.add(ExternalStoragePhoto(id,displayName,width,height,contentUri))
                    }
                }
                photos.toList()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(contentObserver)
    }
}