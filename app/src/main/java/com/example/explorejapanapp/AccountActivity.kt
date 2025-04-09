package com.example.explorejapanapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_page)

        val db = DatabaseProvider.getDatabase(this)
        val accountDao = db.AccountDao()

        val authMode = findViewById<RadioGroup>(R.id.auth_mode)
        val loginMode = findViewById<RadioButton>(R.id.login_mode)
        val registerMode = findViewById<RadioButton>(R.id.register_mode)
        val username = findViewById<EditText>(R.id.username)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val authButton = findViewById<Button>(R.id.auth_button)
        val backButton = findViewById<Button>(R.id.back_button)

        // Зміна видимості поля email та тексту кнопки залежно від режиму
        authMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.login_mode -> {
                    email.visibility = View.GONE
                    authButton.text = "Увійти"
                }
                R.id.register_mode -> {
                    email.visibility = View.VISIBLE
                    authButton.text = "Зареєструватися"
                }
            }
        }

        // Обробка кліку на кнопку авторизації
        authButton.setOnClickListener {
            val usernameText = username.text.toString()
            val emailText = email.text.toString()
            val passwordText = password.text.toString()

            CoroutineScope(Dispatchers.Main).launch {
                if (loginMode.isChecked) {
                    // Режим входу
                    val user = withContext(Dispatchers.IO) {
                        accountDao.login(usernameText, passwordText)
                    }
                    if (user != null) {
                        // Успішний вхід, перенаправлення на головну сторінку
                        startActivity(Intent(this@AccountActivity, MainActivity::class.java))
                        finish()
                    } else {
                        // Невірний логін або пароль, але повідомлення прибрано
                    }
                } else {
                    // Режим реєстрації
                    if (usernameText.isBlank() || emailText.isBlank() || passwordText.isBlank()) {
                        // Заповнення всіх полів обов’язкове, але повідомлення прибрано
                    } else {
                        val existingUser = withContext(Dispatchers.IO) {
                            accountDao.findByUsernameOrEmail(usernameText, emailText)
                        }
                        if (existingUser != null) {
                            // Користувач уже існує, але повідомлення прибрано
                        } else {
                            withContext(Dispatchers.IO) {
                                accountDao.insert(
                                    Account(
                                        username = usernameText,
                                        email = emailText,
                                        password = passwordText
                                    )
                                )
                            }
                            username.text.clear()
                            email.text.clear()
                            password.text.clear()
                        }
                    }
                }
            }
        }

        // Обробка кліку на "Повернутися назад" (нова кнопка)
        backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Обробка кліку на "Назад" (альтернативний спосіб)
        backLink.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}