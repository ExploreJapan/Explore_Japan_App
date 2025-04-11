package com.example.explorejapanapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        // Ініціалізація sections_icon
        val sectionsIcon = findViewById<ImageView>(R.id.sections_icon)

        // Обробка кліку на sections_icon
        sectionsIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}