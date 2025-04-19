package com.example.explorejapanapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.explorejapanapp.databinding.MainPageBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainPageBinding
    private lateinit var auth: FirebaseAuth
    private var currentFragmentIndex = 2 // По умолчанию "Главная" (индекс 2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ініціалізація Firebase Authentication
        auth = FirebaseAuth.getInstance()

        if (savedInstanceState == null) {  // Загружаем фрагмент только при первом создании
            replaceFragment(Fragment_Home(), 2)
            binding.bottomNavigationView.selectedItemId = R.id.home
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val newFragmentIndex = when (item.itemId) {
                R.id.map -> 0
                R.id.profile -> 1
                R.id.home -> 2
                R.id.tips -> 3
                R.id.options -> 4
                else -> 2 // По умолчанию "Главная"
            }

            when (item.itemId) {
                R.id.map -> {
                    replaceFragment(Fragment_Map(), newFragmentIndex)
                    true
                }
                R.id.profile -> {
                    replaceFragment(Fragment_Profile(), newFragmentIndex)
                    true
                }
                R.id.home -> {
                    replaceFragment(Fragment_Home(), newFragmentIndex)
                    true
                }
                R.id.tips -> {
                    replaceFragment(Fragment_Tips(), newFragmentIndex)
                    true
                }
                R.id.options -> {
                    replaceFragment(Fragment_Options(), newFragmentIndex)
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, newIndex: Int) {
        val isMovingRight = newIndex > currentFragmentIndex

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                if (isMovingRight) R.anim.slide_in_right else R.anim.slide_in_left,
                if (isMovingRight) R.anim.slide_out_left else R.anim.slide_out_right,
                if (isMovingRight) R.anim.slide_in_right else R.anim.slide_in_left,
                if (isMovingRight) R.anim.slide_out_left else R.anim.slide_out_right
            )
            .replace(R.id.frame_layout, fragment)
            .commit()

        // Обновляем текущий индекс
        currentFragmentIndex = newIndex
    }

    override fun onDestroy() {
        super.onDestroy()
        // Виконуємо вихід із акаунта лише при повному закритті активності
        if (auth.currentUser != null) {
            auth.signOut()
        }
    }
}