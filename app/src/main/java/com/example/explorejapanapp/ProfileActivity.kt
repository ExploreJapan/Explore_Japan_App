package com.example.explorejapanapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
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

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page)

        try {
            val db = DatabaseProvider.getDatabase(this)
            val profileDao = db.profileDao()

            val authMode = findViewById<RadioGroup>(R.id.auth_mode)
            val loginMode = findViewById<RadioButton>(R.id.login_mode)
            val registerMode = findViewById<RadioButton>(R.id.register_mode)
            val username = findViewById<EditText>(R.id.username)
            val email = findViewById<EditText>(R.id.email)
            val password = findViewById<EditText>(R.id.password)
            val confirmPassword = findViewById<EditText>(R.id.confirm_password)
            val authButton = findViewById<Button>(R.id.auth_button)
            val authMessage = findViewById<TextView>(R.id.auth_message)

            // Зміна видимості полів email і confirm_password залежно від режиму
            authMode.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.login_mode -> {
                        email.visibility = View.GONE
                        confirmPassword.visibility = View.GONE
                        authButton.text = "Увійти"
                        // Очищаємо всі поля при зміні режиму
                        username.text.clear()
                        email.text.clear()
                        password.text.clear()
                        confirmPassword.text.clear()
                    }
                    R.id.register_mode -> {
                        email.visibility = View.VISIBLE
                        confirmPassword.visibility = View.VISIBLE
                        authButton.text = "Зареєструватися"
                        // Очищаємо всі поля при зміні режиму
                        username.text.clear()
                        email.text.clear()
                        password.text.clear()
                        confirmPassword.text.clear()
                    }
                }
                authMessage.visibility = View.GONE
            }

            // Обробка кліку на кнопку авторизації
            authButton.setOnClickListener {
                val usernameText = username.text.toString()
                val emailText = email.text.toString()
                val passwordText = password.text.toString()
                val confirmPasswordText = confirmPassword.text.toString()

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        if (loginMode.isChecked) {
                            // Режим входу
                            if (usernameText.isBlank() || passwordText.isBlank()) {
                                showMessage(authMessage, "Заповніть усі поля.", android.R.color.holo_red_dark)
                            } else {
                                val user = withContext(Dispatchers.IO) {
                                    profileDao.login(usernameText, passwordText)
                                }
                                if (user != null) {
                                    showMessage(authMessage, "Вхід успішний!", android.R.color.holo_green_dark)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                                        finish()
                                    }, 2000)
                                } else {
                                    showMessage(authMessage, "Невірний логін або пароль.", android.R.color.holo_red_dark)
                                }
                            }
                        } else if (registerMode.isChecked) {
                            // Режим реєстрації
                            if (usernameText.isBlank() || emailText.isBlank() || passwordText.isBlank() || confirmPasswordText.isBlank()) {
                                showMessage(authMessage, "Заповніть усі поля.", android.R.color.holo_red_dark)
                            } else if (!isValidEmail(emailText)) {
                                showMessage(authMessage, "Невірний формат пошти.", android.R.color.holo_red_dark)
                            } else if (passwordText != confirmPasswordText) {
                                showMessage(authMessage, "Паролі не збігаються.", android.R.color.holo_red_dark)
                            } else {
                                val existingUser = withContext(Dispatchers.IO) {
                                    profileDao.findByUsernameOrEmail(usernameText, emailText)
                                }
                                if (existingUser != null) {
                                    showMessage(authMessage, "Користувач із таким логіном або поштою вже існує.", android.R.color.holo_red_dark)
                                } else {
                                    withContext(Dispatchers.IO) {
                                        profileDao.insert(
                                            Profile(
                                                username = usernameText,
                                                email = emailText,
                                                password = passwordText
                                            )
                                        )
                                    }
                                    showMessage(authMessage, "Реєстрація успішна!", android.R.color.holo_green_dark)
                                    username.text.clear()
                                    email.text.clear()
                                    password.text.clear()
                                    confirmPassword.text.clear()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showMessage(authMessage, "Помилка: ${e.message}", android.R.color.holo_red_dark)
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showMessage(textView: TextView, message: String, colorResId: Int) {
        textView.text = message
        textView.visibility = View.VISIBLE
        Handler(Looper.getMainLooper()).postDelayed({
            textView.visibility = View.GONE
        }, 2000)
    }
}