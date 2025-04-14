package com.alishoumar.androidstorage

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.alishoumar.androidstorage.databinding.ActivityMainBinding
import com.alishoumar.androidstorage.presentation.utils.DeveloperModeCheckUtils
import com.alishoumar.androidstorage.presentation.utils.RootCheckUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        if(!BuildConfig.DEBUG) {
            System.loadLibrary("root_checker")
            if (isDeviceRooted() || isDeveloperModeEnabled()) {
                showRootOrDevModeDetectedToast()
                finishAffinity()
                return
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavController()
    }

    private fun isDeviceRooted(): Boolean {
        return isDeviceRootedNative() || RootCheckUtils.isDeviceRooted(applicationContext)
    }

    private fun isDeveloperModeEnabled(): Boolean {
        return DeveloperModeCheckUtils.isDevelopmentSettingsEnabled(applicationContext)
    }

    private fun showRootOrDevModeDetectedToast() {
        Toast.makeText(this, "Root or Developer Mode detected! Closing app.", Toast.LENGTH_LONG).show()
    }

    private fun setupNavController() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigationView.visibility = if (destination.id == R.id.imageFragment) View.GONE else View.VISIBLE
        }
    }

    private external fun isDeviceRootedNative(): Boolean
}
