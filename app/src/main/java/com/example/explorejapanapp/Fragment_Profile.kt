package com.example.explorejapanapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class Fragment_Profile : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginContainer: ConstraintLayout
    private lateinit var registerContainer: ConstraintLayout
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment__profile, container, false)

        // Ініціалізація Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Знаходимо контейнери для входу, реєстрації та кнопку "Вийти"
        loginContainer = view.findViewById(R.id.login_container)
        registerContainer = view.findViewById(R.id.register_container)
        logoutButton = view.findViewById(R.id.logout_button)

        // Перевіряємо стан авторизації та показуємо/ховаємо елементи
        if (auth.currentUser != null) {
            // Якщо користувач авторизований, ховаємо контейнери та показуємо кнопку "Вийти"
            loginContainer.visibility = View.GONE
            registerContainer.visibility = View.GONE
            logoutButton.visibility = View.VISIBLE
        } else {
            // Якщо користувач не авторизований, показуємо контейнери та ховаємо кнопку "Вийти"
            loginContainer.visibility = View.VISIBLE
            registerContainer.visibility = View.VISIBLE
            logoutButton.visibility = View.GONE
            setupLoginAndRegister(view)
        }

        // Обробник кнопки "Вийти"
        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(context, "Вихід успішний!", Toast.LENGTH_SHORT).show()
            // Показуємо контейнери для входу/реєстрації та ховаємо кнопку "Вийти"
            loginContainer.visibility = View.VISIBLE
            registerContainer.visibility = View.VISIBLE
            logoutButton.visibility = View.GONE
        }

        return view
    }

    private fun setupLoginAndRegister(view: View) {
        // Знаходимо елементи з макета
        val loginEmail = view.findViewById<EditText>(R.id.login_email)
        val loginPassword = view.findViewById<EditText>(R.id.login_password)
        val loginButton = view.findViewById<Button>(R.id.login_button)

        val registerEmail = view.findViewById<EditText>(R.id.register_email)
        val registerPassword = view.findViewById<EditText>(R.id.register_password)
        val registerConfirmPassword = view.findViewById<EditText>(R.id.register_confirm_password)
        val registerButton = view.findViewById<Button>(R.id.register_button)

        // Обробник кнопки "Увійти"
        loginButton.setOnClickListener {
            val email = loginEmail.text.toString().trim()
            val password = loginPassword.text.toString().trim()

            // Перевірка, чи заповнені поля
            if (email.isEmpty()) {
                loginEmail.error = "Введіть електронну пошту"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                loginPassword.error = "Введіть пароль"
                return@setOnClickListener
            }

            // Вхід через Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Вхід успішний!", Toast.LENGTH_SHORT).show()
                        // Очищаємо поля
                        loginEmail.text.clear()
                        loginPassword.text.clear()
                        // Ховаємо контейнери та показуємо кнопку "Вийти"
                        loginContainer.visibility = View.GONE
                        registerContainer.visibility = View.GONE
                        logoutButton.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            context,
                            "Помилка входу: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        // Обробник кнопки "Зареєструватися"
        registerButton.setOnClickListener {
            val email = registerEmail.text.toString().trim()
            val password = registerPassword.text.toString().trim()
            val confirmPassword = registerConfirmPassword.text.toString().trim()

            // Перевірка, чи заповнені поля
            if (email.isEmpty()) {
                registerEmail.error = "Введіть електронну пошту"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                registerPassword.error = "Введіть пароль"
                return@setOnClickListener
            }
            if (confirmPassword.isEmpty()) {
                registerConfirmPassword.error = "Підтвердіть пароль"
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                registerConfirmPassword.error = "Паролі не співпадають"
                return@setOnClickListener
            }

            // Реєстрація через Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Реєстрація успішна!", Toast.LENGTH_SHORT).show()
                        // Очищаємо поля
                        registerEmail.text.clear()
                        registerPassword.text.clear()
                        registerConfirmPassword.text.clear()
                        // Ховаємо контейнери та показуємо кнопку "Вийти"
                        loginContainer.visibility = View.GONE
                        registerContainer.visibility = View.GONE
                        logoutButton.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            context,
                            "Помилка реєстрації: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        // Перевіряємо, чи користувач уже увійшов
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Toast.makeText(context, "Користувач уже увійшов: ${currentUser.email}", Toast.LENGTH_SHORT).show()
            // Ховаємо контейнери та показуємо кнопку "Вийти"
            loginContainer.visibility = View.GONE
            registerContainer.visibility = View.GONE
            logoutButton.visibility = View.VISIBLE
        } else {
            // Показуємо контейнери та ховаємо кнопку "Вийти"
            loginContainer.visibility = View.VISIBLE
            registerContainer.visibility = View.VISIBLE
            logoutButton.visibility = View.GONE
        }
    }
}