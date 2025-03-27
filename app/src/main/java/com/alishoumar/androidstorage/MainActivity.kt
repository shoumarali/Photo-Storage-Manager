package com.alishoumar.androidstorage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.alishoumar.androidstorage.databinding.ActivityMainBinding
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        System.loadLibrary("root_checker")

        isDeviceRootedNative()

        if(isDeviceRootedNative()){
            Toast.makeText(this, "Root detected! Closing app.", Toast.LENGTH_LONG).show()
            finishAffinity()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _,destination,_ ->
            when(destination.id) {
                R.id.imageFragment -> {
                    binding.bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun isDeviceRooted(): Boolean {
        return RootBeer(this).isRooted
    }

    private external fun isDeviceRootedNative(): Boolean

}