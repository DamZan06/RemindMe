package com.damiano.remindme

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.damiano.remindme.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Applica la lingua salvata
        applySavedLanguage()

        // Inizializza Firebase
        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ottieni la BottomNavigationView e il NavController
        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_settings,
                R.id.navigation_add,
                R.id.navigation_event
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Verifica se l'utente è autenticato
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Se l'utente non è autenticato, apri il SettingsFragment
            navController.navigate(R.id.navigation_settings)
        } else {
            // Se l'utente è autenticato, apri il HomeFragment (di default)
            navController.navigate(R.id.navigation_home)
        }
    }

    private fun applySavedLanguage() {
        // Recupera le SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("Settings", 0)
        val savedLanguage = sharedPreferences.getString("App_Language", "en") ?: "en"

        // Set the default locale to the saved language
        val locale = Locale(savedLanguage)
        Locale.setDefault(locale)

        // Update the configuration to use the saved language
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

}
