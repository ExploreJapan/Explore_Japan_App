package com.example.explorejapanapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        // Знаходимо TextView "Вхід" і додаємо обробку кліку
        val enterTextView = findViewById<TextView>(R.id.enter)
        enterTextView.setOnClickListener {
            // Перенаправлення на AccountActivity (сторінка авторизації)
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
        }
    }
}