package com.alishoumar.androidstorage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alishoumar.androidstorage.adapter.InternalStoragePhotoAdapter
import com.alishoumar.androidstorage.data.InternalStoragePhoto
import com.alishoumar.androidstorage.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
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


        setUpInternalStorageRecyclerViews()
        LoadPhotosFromInternalStorageToRecyclerView()

        val takePhoto = registerForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview(),
            callback ={
                val isPrivate = binding.switchPrivate.isChecked
                if(isPrivate){
                    val isSavedSuccessfully=savePhotoToInternalStorage(
                        UUID.randomUUID().toString(),
                        it!!)
                    if(isSavedSuccessfully){
                        LoadPhotosFromInternalStorageToRecyclerView()
                        Toast.makeText(this, "saved successfully", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Failed to save", Toast.LENGTH_SHORT).show()
                    }
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
}