package com.alishoumar.androidstorage

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.alishoumar.androidstorage.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)


//        ViewCompat.setOnApplyWindowInsetsListener(binding.root){v , insets->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }



//        val takePhoto = registerForActivityResult(
//            contract = ActivityResultContracts.TakePicturePreview(),
//            callback = {
//                lifecycleScope.launch {
//                    if(binding.switchPrivate.isChecked){
//                        internalStorageViewModel.savePhotoToInternalStorage(
//                            UUID.randomUUID().toString(),
//                            it!!)
//                    }else{
//                        externalStorageViewModel.savePhotoToExternalStorage(
//                            UUID.randomUUID().toString(), it!!
//                        )
//                    }
//                }
//            }
//        )
//        binding.btnTakePhoto.setOnClickListener {
//            takePhoto.launch()
//        }
    }
}