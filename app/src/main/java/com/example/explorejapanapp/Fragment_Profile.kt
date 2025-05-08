package com.example.explorejapanapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class Fragment_Profile : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var loginContainer: ConstraintLayout
    private lateinit var registerContainer: ConstraintLayout
    private lateinit var logoutButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var newEmail: EditText
    private lateinit var updateEmailButton: Button
    private lateinit var newPassword: EditText
    private lateinit var confirmNewPassword: EditText
    private lateinit var updatePasswordButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Ініціалізація Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Знаходимо контейнери для входу, реєстрації та кнопки
        loginContainer = view.findViewById(R.id.login_container)
        registerContainer = view.findViewById(R.id.register_container)
        logoutButton = view.findViewById(R.id.logout_button)
        deleteAccountButton = view.findViewById(R.id.delete_account_button)
        newEmail = view.findViewById(R.id.new_email)
        updateEmailButton = view.findViewById(R.id.update_email_button)
        newPassword = view.findViewById(R.id.new_password)
        confirmNewPassword = view.findViewById(R.id.confirm_new_password)
        updatePasswordButton = view.findViewById(R.id.update_password_button)

        // Перевіряємо стан авторизації та показуємо/ховаємо елементи
        if (auth.currentUser != null) {
            // Якщо користувач авторизований, ховаємо контейнери та показуємо поля для оновлення
            loginContainer.visibility = View.GONE
            registerContainer.visibility = View.GONE
            newEmail.visibility = View.VISIBLE
            updateEmailButton.visibility = View.VISIBLE
            newPassword.visibility = View.VISIBLE
            confirmNewPassword.visibility = View.VISIBLE
            updatePasswordButton.visibility = View.VISIBLE
            logoutButton.visibility = View.VISIBLE
            deleteAccountButton.visibility = View.VISIBLE
        } else {
            // Якщо користувач не авторизований, показуємо контейнери та ховаємо поля для оновлення
            loginContainer.visibility = View.VISIBLE
            registerContainer.visibility = View.VISIBLE
            newEmail.visibility = View.GONE
            updateEmailButton.visibility = View.GONE
            newPassword.visibility = View.GONE
            confirmNewPassword.visibility = View.GONE
            updatePasswordButton.visibility = View.GONE
            logoutButton.visibility = View.GONE
            deleteAccountButton.visibility = View.GONE
            setupLoginAndRegister(view)
        }

        // Обробник кнопки "Оновити пошту"
        updateEmailButton.setOnClickListener {
            val newEmailText = newEmail.text.toString().trim()
            if (newEmailText.isEmpty()) {
                newEmail.error = "Введіть нову пошту"
                return@setOnClickListener
            }

            val user = auth.currentUser
            if (user != null) {
                user.verifyBeforeUpdateEmail(newEmailText)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                context,
                                "На нову пошту надіслано лист для підтвердження. " +
                                        getString(R.string.please_relogin),
                                Toast.LENGTH_LONG
                            ).show()
                            newEmail.text.clear()
                            // Виконуємо вихід
                            auth.signOut()
                            // Показуємо контейнери для входу/реєстрації та ховаємо поля для оновлення
                            loginContainer.visibility = View.VISIBLE
                            registerContainer.visibility = View.VISIBLE
                            newEmail.visibility = View.GONE
                            updateEmailButton.visibility = View.GONE
                            newPassword.visibility = View.GONE
                            confirmNewPassword.visibility = View.GONE
                            updatePasswordButton.visibility = View.GONE
                            logoutButton.visibility = View.GONE
                            deleteAccountButton.visibility = View.GONE
                            // Завершуємо додаток
                            activity?.finish()
                        } else {
                            Toast.makeText(
                                context,
                                "Помилка оновлення пошти: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Користувач не авторизований!", Toast.LENGTH_SHORT).show()
            }
        }

        // Обробник кнопки "Оновити пароль"
        updatePasswordButton.setOnClickListener {
            val newPasswordText = newPassword.text.toString().trim()
            val confirmNewPasswordText = confirmNewPassword.text.toString().trim()

            // Перевірка, чи паролі співпадають
            if (newPasswordText.isNotEmpty() || confirmNewPasswordText.isNotEmpty()) {
                if (newPasswordText != confirmNewPasswordText) {
                    confirmNewPassword.error = "Паролі не співпадають"
                    return@setOnClickListener
                }
                if (newPasswordText.length < 6) {
                    newPassword.error = "Пароль має бути довшим за 6 символів"
                    return@setOnClickListener
                }

                val user = auth.currentUser
                if (user != null) {
                    user.updatePassword(newPasswordText)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Пароль успішно оновлено! " + getString(R.string.please_relogin),
                                    Toast.LENGTH_LONG
                                ).show()
                                newPassword.text.clear()
                                confirmNewPassword.text.clear()
                                // Виконуємо вихід
                                auth.signOut()
                                // Показуємо контейнери для входу/реєстрації та ховаємо поля для оновлення
                                loginContainer.visibility = View.VISIBLE
                                registerContainer.visibility = View.VISIBLE
                                newEmail.visibility = View.GONE
                                updateEmailButton.visibility = View.GONE
                                newPassword.visibility = View.GONE
                                confirmNewPassword.visibility = View.GONE
                                updatePasswordButton.visibility = View.GONE
                                logoutButton.visibility = View.GONE
                                deleteAccountButton.visibility = View.GONE
                                // Завершуємо додаток
                                activity?.finish()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Помилка оновлення пароля: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Користувач не авторизований!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Введіть новий пароль!", Toast.LENGTH_SHORT).show()
            }
        }

        // Обробник кнопки "Вийти"
        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(context, "Вихід успішний!", Toast.LENGTH_SHORT).show()
            // Показуємо контейнери для входу/реєстрації та ховаємо поля для оновлення
            loginContainer.visibility = View.VISIBLE
            registerContainer.visibility = View.VISIBLE
            newEmail.visibility = View.GONE
            updateEmailButton.visibility = View.GONE
            newPassword.visibility = View.GONE
            confirmNewPassword.visibility = View.GONE
            updatePasswordButton.visibility = View.GONE
            logoutButton.visibility = View.GONE
            deleteAccountButton.visibility = View.GONE
        }

        // Обробник кнопки "Видалити акаунт"
        deleteAccountButton.setOnClickListener {
            // Показуємо діалог підтвердження
            showDeleteConfirmationDialog()
        }

        return view
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.delete_account_confirmation)
            .setPositiveButton(R.string.confirm_delete) { _, _ ->
                // Користувач підтвердив видалення
                val user = auth.currentUser
                if (user != null) {
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Акаунт успішно видалено!", Toast.LENGTH_SHORT).show()
                                // Показуємо контейнери для входу/реєстрації та ховаємо поля для оновлення
                                loginContainer.visibility = View.VISIBLE
                                registerContainer.visibility = View.VISIBLE
                                newEmail.visibility = View.GONE
                                updateEmailButton.visibility = View.GONE
                                newPassword.visibility = View.GONE
                                confirmNewPassword.visibility = View.GONE
                                updatePasswordButton.visibility = View.GONE
                                logoutButton.visibility = View.GONE
                                deleteAccountButton.visibility = View.GONE
                            } else {
                                Toast.makeText(
                                    context,
                                    "Помилка видалення акаунта: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Користувач не авторизований!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                // Користувач скасував видалення
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun setupLoginAndRegister(view: View) {
        // Знаходимо елементи з макета
        val loginEmail = view.findViewById<EditText>(R.id.login_email)
        val loginPassword = view.findViewById<EditText>(R.id.login_password)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        val forgotPassword = view.findViewById<TextView>(R.id.forgot_password)

        val registerEmail = view.findViewById<EditText>(R.id.register_email)
        val registerPassword = view.findViewById<EditText>(R.id.register_password)
        val registerConfirmPassword = view.findViewById<EditText>(R.id.register_confirm_password)
        val registerButton = view.findViewById<Button>(R.id.register_button)

        // Обробник кнопки "Забув пароль?"
        forgotPassword.setOnClickListener {
            val email = loginEmail.text.toString().trim()
            if (email.isEmpty()) {
                loginEmail.error = "Введіть електронну пошту"
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            context,
                            getString(R.string.password_reset_sent),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Помилка: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

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
                        // Ховаємо контейнери та показуємо поля для оновлення
                        loginContainer.visibility = View.GONE
                        registerContainer.visibility = View.GONE
                        newEmail.visibility = View.VISIBLE
                        updateEmailButton.visibility = View.VISIBLE
                        newPassword.visibility = View.VISIBLE
                        confirmNewPassword.visibility = View.VISIBLE
                        updatePasswordButton.visibility = View.VISIBLE
                        logoutButton.visibility = View.VISIBLE
                        deleteAccountButton.visibility = View.VISIBLE
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
                        // Ховаємо контейнери та показуємо поля для оновлення
                        loginContainer.visibility = View.GONE
                        registerContainer.visibility = View.GONE
                        newEmail.visibility = View.VISIBLE
                        updateEmailButton.visibility = View.VISIBLE
                        newPassword.visibility = View.VISIBLE
                        confirmNewPassword.visibility = View.VISIBLE
                        updatePasswordButton.visibility = View.VISIBLE
                        logoutButton.visibility = View.VISIBLE
                        deleteAccountButton.visibility = View.VISIBLE
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
            // Ховаємо контейнери та показуємо поля для оновлення
            loginContainer.visibility = View.GONE
            registerContainer.visibility = View.GONE
            newEmail.visibility = View.VISIBLE
            updateEmailButton.visibility = View.VISIBLE
            newPassword.visibility = View.VISIBLE
            confirmNewPassword.visibility = View.VISIBLE
            updatePasswordButton.visibility = View.VISIBLE
            logoutButton.visibility = View.VISIBLE
            deleteAccountButton.visibility = View.VISIBLE
        } else {
            // Показуємо контейнери та ховаємо поля для оновлення
            loginContainer.visibility = View.VISIBLE
            registerContainer.visibility = View.VISIBLE
            newEmail.visibility = View.GONE
            updateEmailButton.visibility = View.GONE
            newPassword.visibility = View.GONE
            confirmNewPassword.visibility = View.GONE
            updatePasswordButton.visibility = View.GONE
            logoutButton.visibility = View.GONE
            deleteAccountButton.visibility = View.GONE
        }
    }
}