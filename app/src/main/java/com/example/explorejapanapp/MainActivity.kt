package com.example.explorejapanapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.explorejapanapp.databinding.MainPageBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainPageBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ініціалізація Firebase Authentication
        auth = FirebaseAuth.getInstance()

        if (savedInstanceState == null) {  // Загружаем фрагмент только при первом создании
            replaceFragment(Fragment_Home())
            binding.bottomNavigationView.selectedItemId = R.id.home
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.map -> {
                    replaceFragment(Fragment_Map())
                    true
                }
                R.id.profile -> {
                    replaceFragment(Fragment_Profile())
                    true
                }
                R.id.home -> {
                    replaceFragment(Fragment_Home())
                    true
                }
                R.id.tips -> {
                    replaceFragment(Fragment_Tips())
                    true
                }
                R.id.options -> {
                    replaceFragment(Fragment_Options())
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Виконуємо вихід із акаунта лише при повному закритті активності
        if (auth.currentUser != null) {
            auth.signOut()
        }
    }
}