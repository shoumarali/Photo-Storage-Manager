package com.alishoumar.androidstorage

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.contentValuesOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alishoumar.androidstorage.adapter.InternalStoragePhotoAdapter
import com.alishoumar.androidstorage.adapter.SharedStoragePhotoAdapter
import com.alishoumar.androidstorage.data.ExternalStoragePhoto
import com.alishoumar.androidstorage.data.InternalStoragePhoto
import com.alishoumar.androidstorage.databinding.ActivityMainBinding
import com.alishoumar.androidstorage.util.sdk24AndAbove
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

/*internal storage is your private storage in your app no other app can access internal storage of other
app unless the phone is rooted
*/

/*
start activity for result is used to start activity and waite for result for example like permission
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var internalStoragePhotoAdapter: InternalStoragePhotoAdapter
    private lateinit var externalStoragePhotoAdapter: SharedStoragePhotoAdapter

    private var isReadPermissionGranted:Boolean = false
    private var isWritePermissionGranted:Boolean = false
    private lateinit var permissionLauncher : ActivityResultLauncher<Array<String>>

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

        internalStoragePhotoAdapter = InternalStoragePhotoAdapter {
            val isDeleted = deletePhotoFromInternalStorage(it.name)
            if(isDeleted){
                LoadPhotosFromInternalStorageToRecyclerView()
                Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
        }


        externalStoragePhotoAdapter = SharedStoragePhotoAdapter {

        }

        permissionLauncher = registerForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            callback = {
                isReadPermissionGranted = it[Manifest.permission.READ_EXTERNAL_STORAGE]?:false
                isWritePermissionGranted = it[Manifest.permission.WRITE_EXTERNAL_STORAGE]?:false
            }
        )
        updateOrRequestPermissions()
        setUpInternalStorageRecyclerViews()
        LoadPhotosFromInternalStorageToRecyclerView()

        val takePhoto = registerForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview(),
            callback ={
                val isPrivate = binding.switchPrivate.isChecked
                val isSavedSuccessfully= when {
                    isPrivate -> savePhotoToInternalStorage(UUID.randomUUID().toString(), it!!)
                    isWritePermissionGranted -> savePhotoToExternalStorage(UUID.randomUUID().toString(), bmp = it!!)
                    else -> false
                }

                if(isPrivate){
                    LoadPhotosFromInternalStorageToRecyclerView()
                }
                if(isSavedSuccessfully){
                    Toast.makeText(this, "saved successfully", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.btnTakePhoto.setOnClickListener {
            takePhoto.launch()
        }
    }

    /*
    we got 2 types of streams input stream and output stream
    output stream when we have bitmap and we want to put them out to a file
    input when we have a file and we want to put them in a bmp

    use is kotlin extension function specific to file stream it close our stream after we wrote to it
    or an exception is thrown
     */

    private fun savePhotoToInternalStorage(fileName: String, bmp: Bitmap): Boolean{
        return try {
            openFileOutput("$fileName.jpg", MODE_PRIVATE).use {stream ->
                if(!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)){
                    throw IOException("Couldn't save bitmap")
                }
            }
            true
        }catch (e: IOException){
            e.printStackTrace()
            false
        }
    }

    private suspend fun loadPhotosFromInternalStorage():List<InternalStoragePhoto>{
        return  withContext(Dispatchers.IO){
            val files = filesDir.listFiles()
            files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
                val bytes = it.readBytes()
                val bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
                InternalStoragePhoto(it.name,bmp)
            } ?: listOf()
        }
    }

    private fun deletePhotoFromInternalStorage(fileName: String): Boolean{
        return try {
            deleteFile(fileName)
        }catch (e: Exception){
            e.printStackTrace()
            false
        }
    }

    private fun LoadPhotosFromInternalStorageToRecyclerView(){
        lifecycleScope.launch {
            val photos = loadPhotosFromInternalStorage()
            internalStoragePhotoAdapter.submitList(photos)
        }
    }

    private fun setUpInternalStorageRecyclerViews()=binding.rvPrivatePhotos.apply{
        adapter = internalStoragePhotoAdapter
        layoutManager = StaggeredGridLayoutManager(3,RecyclerView.VERTICAL)

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

    private fun savePhotoToExternalStorage(displayName:String , bmp: Bitmap):Boolean{
        val imageCollection = sdk24AndAbove {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }?:MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME,"$displayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE,"image/jpg")
            put(MediaStore.Images.Media.WIDTH, bmp.width)
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
        }

        /*the insert method will only save the metadata of the image in big database and it will return
        the uri of the image where we can actually save it
         */

        return try {
            contentResolver.insert(imageCollection, contentValues)?.also {uri ->
                contentResolver.openOutputStream(uri)?.use {
                    if(!bmp.compress(Bitmap.CompressFormat.JPEG, 95, it)){
                        throw IOException("Couldn't save the bitmap")
                    }
                }
            }?: throw IOException("Couldn't create media store entry")
            true
        }catch (e:IOException){
            e.printStackTrace()
            false
        }
    }
}