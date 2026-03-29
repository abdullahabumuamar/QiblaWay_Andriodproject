package com.example.qiblaway2


import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.qiblaway2.databinding.ActivityMainBinding
import com.example.qiblaway2.utils.LanguageHelper
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.loadLocale(this) // تحميل اللغة المحفوظة

        //****************************
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.fragmentContainerView)
        navView.setupWithNavController(navController)

        ///////*****************************
        // When navigating via BottomNavigation, clear the back stack
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.popBackStack(R.id.navigation_home, false)
                    navController.navigate(R.id.navigation_home)
                }
                R.id.navigation_Quran -> {
                    navController.popBackStack(R.id.navigation_Quran, false)
                    navController.navigate(R.id.navigation_Quran)
                }
                R.id.navigation_Dhikr -> {
                    navController.popBackStack(R.id.navigation_Dhikr, false)
                    navController.navigate(R.id.navigation_Dhikr)
                }
                R.id.navigation_Qiblah -> {
                    navController.popBackStack(R.id.navigation_Qiblah, false)
                    navController.navigate(R.id.navigation_Qiblah)
                }
            }
            true
        }

        // Handle Back Button: Always return to HomeFragment before exiting
        onBackPressedDispatcher.addCallback(this) {
            if (navController.currentDestination?.id != R.id.navigation_home) {
                navController.popBackStack(R.id.navigation_home, false)
            } else {
                finish() // Exit the app
            }
        }




    }

}