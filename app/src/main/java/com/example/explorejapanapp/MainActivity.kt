package com.example.explorejapanapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.explorejapanapp.databinding.MainPageBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainPageBinding
    private lateinit var auth: FirebaseAuth
    private var currentFragmentIndex = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (savedInstanceState == null) {
            replaceFragment(Fragment_Home(), 2)
            binding.bottomNavigationView.selectedItemId = R.id.home
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val newFragmentIndex = when (item.itemId) {
                R.id.map -> 0
                R.id.profile -> 1
                R.id.home -> 2
                R.id.tips -> 3
                R.id.info -> 4
                else -> 2
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
                R.id.info -> {
                    replaceFragment(Fragment_Info(), newFragmentIndex)
                    true
                }
                else -> false
            }
        }

        // Обробка Deep Link при запуску
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent)
    { // Залишаємо без "override" через обмеження Kotlin
        super.onNewIntent(intent)
        this.intent = intent // Оновлюємо поточний інтент
        handleDeepLink(intent) // Передаємо intent як є, адже handleDeepLink уже обробляє null
    }

    internal fun handleDeepLink(intent: Intent?) {
        if (intent == null) return // Повертаємося, якщо intent null

        val uri: Uri? = intent.data
        if (uri != null) {
            // Обробка email-посилання для аутентифікації
            val emailLink = uri.toString()
            if (auth.isSignInWithEmailLink(emailLink)) {
                val sharedPref = getPreferences(Context.MODE_PRIVATE)
                val email = sharedPref.getString("pending_email", null)
                if (email != null) {
                    auth.signInWithEmailLink(email, emailLink)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Реєстрація підтверджена!", Toast.LENGTH_SHORT).show()
                                with(sharedPref.edit()) {
                                    remove("pending_email")
                                    apply()
                                }
                                // Переходимо до профілю після підтвердження
                                replaceFragment(Fragment_Profile(), 1)
                                binding.bottomNavigationView.selectedItemId = R.id.profile
                            } else {
                                Toast.makeText(
                                    this,
                                    "Помилка підтвердження: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Помилка: email не знайдено", Toast.LENGTH_LONG).show()
                }
                return // Завершуємо обробку, якщо це email-посилання
            }

            // Обробка Deep Link для статей
            if (uri.scheme == "explorejapanapp") {
                val host = uri.host
                val articleTitle = uri.getQueryParameter("article")
                if (articleTitle != null) {
                    val fragment: Fragment = when (host) {
                        "tokyo" -> City_Tokyo()
                        "fukuoka" -> City_Fukuoka()
                        "hiroshima" -> City_Hiroshima()
                        "kyoto" -> City_Kyoto()
                        "nagoya" -> City_Nagoya()
                        "naha" -> City_Naha()
                        "niigata" -> City_Niigata()
                        "osaka" -> City_Osaka()
                        "sapporo" -> City_Sapporo()
                        "sendai" -> City_Sendai()
                        else -> return
                    }
                    val bundle = Bundle().apply {
                        putString("articleToOpen", articleTitle)
                    }
                    fragment.arguments = bundle

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }

    internal fun replaceFragment(fragment: Fragment, newIndex: Int) {
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

        currentFragmentIndex = newIndex
    }

    override fun onDestroy() {
        super.onDestroy()
        if (auth.currentUser != null) {
            auth.signOut()
        }
    }
}