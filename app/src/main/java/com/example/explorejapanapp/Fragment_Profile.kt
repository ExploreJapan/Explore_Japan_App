package com.example.explorejapanapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Fragment_Profile : Fragment() {

    internal lateinit var auth: FirebaseAuth
    internal lateinit var db: FirebaseFirestore
    internal lateinit var loginContainer: ConstraintLayout
    internal lateinit var registerContainer: ConstraintLayout
    internal lateinit var emailContainer: ConstraintLayout
    internal lateinit var passwordContainer: ConstraintLayout
    internal lateinit var topButtonsContainer: LinearLayout
    internal lateinit var favoritesButton: MaterialButton
    internal lateinit var profileButton: MaterialButton
    internal lateinit var logoutButton: MaterialButton
    internal lateinit var deleteAccountButton: MaterialButton
    internal lateinit var newEmail: EditText
    internal lateinit var updateEmailButton: MaterialButton
    internal lateinit var newPassword: EditText
    internal lateinit var confirmNewPassword: EditText
    internal lateinit var updatePasswordButton: MaterialButton
    internal lateinit var noFavoritesText: TextView
    internal lateinit var favoritesTitle: TextView
    internal lateinit var favoritesTable: TableLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loginContainer = view.findViewById(R.id.login_container)
        registerContainer = view.findViewById(R.id.register_container)
        emailContainer = view.findViewById(R.id.email_container)
        passwordContainer = view.findViewById(R.id.password_container)
        topButtonsContainer = view.findViewById(R.id.top_buttons_container)
        favoritesButton = view.findViewById(R.id.favorites_button)
        profileButton = view.findViewById(R.id.profile_button)
        logoutButton = view.findViewById(R.id.logout_button)
        deleteAccountButton = view.findViewById(R.id.delete_account_button)
        newEmail = view.findViewById(R.id.new_email)
        updateEmailButton = view.findViewById(R.id.update_email_button)
        newPassword = view.findViewById(R.id.new_password)
        confirmNewPassword = view.findViewById(R.id.confirm_new_password)
        updatePasswordButton = view.findViewById(R.id.update_password_button)
        noFavoritesText = view.findViewById(R.id.no_favorites_text)
        favoritesTitle = view.findViewById(R.id.favorites_title)
        favoritesTable = view.findViewById(R.id.favorites_table)

        if (auth.currentUser != null) {
            loginContainer.visibility = View.GONE
            registerContainer.visibility = View.GONE
            topButtonsContainer.visibility = View.VISIBLE
            showProfileSection()
            loadFavorites()
        } else {
            loginContainer.visibility = View.VISIBLE
            registerContainer.visibility = View.VISIBLE
            emailContainer.visibility = View.GONE
            passwordContainer.visibility = View.GONE
            topButtonsContainer.visibility = View.GONE
            logoutButton.visibility = View.GONE
            deleteAccountButton.visibility = View.GONE
            favoritesTitle.visibility = View.GONE
            favoritesTable.visibility = View.GONE
            noFavoritesText.visibility = View.GONE
        }

        // Налаштування кнопок входу та реєстрації
        setupLoginAndRegister(view)

        // Обработчики для новых кнопок
        favoritesButton.setOnClickListener {
            showFavoritesSection()
        }

        profileButton.setOnClickListener {
            showProfileSection()
        }

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
                            auth.signOut()
                            updateUIForLoggedOutUser()
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

        updatePasswordButton.setOnClickListener {
            val newPasswordText = newPassword.text.toString().trim()
            val confirmNewPasswordText = confirmNewPassword.text.toString().trim()

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
                                auth.signOut()
                                updateUIForLoggedOutUser()
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

        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(context, "Вихід успішний!", Toast.LENGTH_SHORT).show()
            updateUIForLoggedOutUser()
        }

        deleteAccountButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        return view
    }

    private fun loadFavorites() {
        val user = auth.currentUser
        if (user != null) {
            db.collection("users").document(user.uid)
                .collection("favorites")
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        favoritesTable.visibility = View.GONE
                        noFavoritesText.visibility = View.VISIBLE
                    } else {
                        val favorites = documents.map { doc ->
                            FavoriteItem(
                                title = doc.getString("title") ?: "",
                                city = doc.getString("city") ?: "",
                                deepLink = doc.getString("url") ?: "",
                                docId = doc.id
                            )
                        }

                        // Очищаємо таблицю, залишаючи лише заголовок
                        favoritesTable.removeViews(1, favoritesTable.childCount - 1)

                        // Додаємо рядки до таблиці
                        favorites.forEach { item ->
                            val displayText = if (item.city == "All") {
                                item.title
                            } else {
                                val cityUkrainian = convertCityToUkrainian(item.city)
                                "${item.title} ($cityUkrainian)"
                            }

                            val tableRow = TableRow(requireContext())
                            tableRow.layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            )
                            tableRow.setPadding(8, 8, 8, 8)

                            // Колонка з посиланням на статтю
                            val articleLink = TextView(requireContext())
                            articleLink.layoutParams = TableRow.LayoutParams(
                                0,
                                TableRow.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            articleLink.text = displayText
                            articleLink.textSize = 16f
                            articleLink.setTextColor(requireContext().getColor(android.R.color.black))
                            articleLink.gravity = android.view.Gravity.CENTER
                            articleLink.setOnClickListener {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.deepLink))
                                startActivity(intent)
                            }

                            // Колонка з кнопкою "Видалити"
                            val deleteButton = Button(requireContext())
                            deleteButton.layoutParams = TableRow.LayoutParams(
                                0,
                                TableRow.LayoutParams.WRAP_CONTENT,
                                1f
                            )
                            deleteButton.text = "Видалити"
                            deleteButton.setBackgroundColor(requireContext().getColor(android.R.color.holo_red_light))
                            deleteButton.setTextColor(requireContext().getColor(android.R.color.white))
                            deleteButton.setOnClickListener {
                                showDeleteConfirmationDialog(item)
                            }

                            tableRow.addView(articleLink)
                            tableRow.addView(deleteButton)
                            favoritesTable.addView(tableRow)
                        }

                        favoritesTable.visibility = View.VISIBLE
                        noFavoritesText.visibility = View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Помилка завантаження обраного: ${e.message}", Toast.LENGTH_SHORT).show()
                    favoritesTable.visibility = View.GONE
                    noFavoritesText.visibility = View.VISIBLE
                }
        }
    }

    // Діалог підтвердження видалення
    private fun showDeleteConfirmationDialog(item: FavoriteItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Видалити статтю?")
            .setMessage("Ви впевнені, що хочете видалити '${item.title}' із обраного?")
            .setPositiveButton("Так") { _, _ ->
                val user = auth.currentUser
                if (user != null) {
                    db.collection("users").document(user.uid)
                        .collection("favorites")
                        .document(item.docId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Стаття видалена з обраного!", Toast.LENGTH_SHORT).show()
                            loadFavorites() // Оновлюємо таблицю після видалення
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Помилка видалення: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Ні") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    // Функція для конвертації назви міста в українську (кирилиця)
    private fun convertCityToUkrainian(city: String): String {
        return when (city) {
            "Tokyo" -> "Токіо"
            "Fukuoka" -> "Фукуока"
            "Hiroshima" -> "Хіросіма"
            "Kyoto" -> "Кіото"
            "Nagoya" -> "Нагоя"
            "Naha" -> "Наха"
            "Niigata" -> "Нііґата"
            "Osaka" -> "Осака"
            "Sapporo" -> "Саппоро"
            "Sendai" -> "Сендай"
            "Tips" -> "Поради"
            else -> city
        }
    }

    private fun updateUIForLoggedOutUser() {
        loginContainer.visibility = View.VISIBLE
        registerContainer.visibility = View.VISIBLE
        emailContainer.visibility = View.GONE
        passwordContainer.visibility = View.GONE
        topButtonsContainer.visibility = View.GONE
        logoutButton.visibility = View.GONE
        deleteAccountButton.visibility = View.GONE
        favoritesTitle.visibility = View.GONE
        favoritesTable.visibility = View.GONE
        noFavoritesText.visibility = View.GONE
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(R.string.delete_account_confirmation)
            .setPositiveButton(R.string.confirm_delete) { _, _ ->
                val user = auth.currentUser
                if (user != null) {
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Акаунт успішно видалено!", Toast.LENGTH_SHORT).show()
                                updateUIForLoggedOutUser()
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
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun setupLoginAndRegister(view: View) {
        val loginEmail = view.findViewById<EditText>(R.id.login_email)
        val loginPassword = view.findViewById<EditText>(R.id.login_password)
        val loginButton = view.findViewById<MaterialButton>(R.id.login_button)
        val forgotPassword = view.findViewById<TextView>(R.id.forgot_password)

        val registerEmail = view.findViewById<EditText>(R.id.register_email)
        val registerPassword = view.findViewById<EditText>(R.id.register_password)
        val registerConfirmPassword = view.findViewById<EditText>(R.id.register_confirm_password)
        val registerButton = view.findViewById<MaterialButton>(R.id.register_button)

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

        loginButton.setOnClickListener {
            Log.d("FragmentProfile", "Login button clicked")
            val email = loginEmail.text.toString().trim()
            val password = loginPassword.text.toString().trim()

            if (email.isEmpty()) {
                loginEmail.error = "Введіть електронну пошту"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                loginPassword.error = "Введіть пароль"
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Вхід успішний!", Toast.LENGTH_SHORT).show()
                        loginEmail.text.clear()
                        loginPassword.text.clear()
                        loginContainer.visibility = View.GONE
                        registerContainer.visibility = View.GONE
                        topButtonsContainer.visibility = View.VISIBLE
                        showProfileSection()
                        loadFavorites()
                    } else {
                        Toast.makeText(
                            context,
                            "Помилка входу: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }

        registerButton.setOnClickListener {
            Log.d("FragmentProfile", "Register button clicked")
            val email = registerEmail.text.toString().trim()
            val password = registerPassword.text.toString().trim()
            val confirmPassword = registerConfirmPassword.text.toString().trim()

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

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Реєстрація успішна!", Toast.LENGTH_SHORT).show()
                        registerEmail.text.clear()
                        registerPassword.text.clear()
                        registerConfirmPassword.text.clear()
                        loginContainer.visibility = View.GONE
                        registerContainer.visibility = View.GONE
                        topButtonsContainer.visibility = View.VISIBLE
                        showProfileSection()
                        loadFavorites()
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
        val currentUser = auth.currentUser
        if (currentUser != null) {
            loginContainer.visibility = View.GONE
            registerContainer.visibility = View.GONE
            topButtonsContainer.visibility = View.VISIBLE
            showProfileSection()
            loadFavorites()
        } else {
            loginContainer.visibility = View.VISIBLE
            registerContainer.visibility = View.VISIBLE
            emailContainer.visibility = View.GONE
            passwordContainer.visibility = View.GONE
            topButtonsContainer.visibility = View.GONE
            logoutButton.visibility = View.GONE
            deleteAccountButton.visibility = View.GONE
            favoritesTitle.visibility = View.GONE
            favoritesTable.visibility = View.GONE
            noFavoritesText.visibility = View.GONE
        }
    }

    internal fun showProfileSection() {
        emailContainer.visibility = View.VISIBLE
        passwordContainer.visibility = View.VISIBLE
        logoutButton.visibility = View.VISIBLE
        deleteAccountButton.visibility = View.VISIBLE
        favoritesTitle.visibility = View.GONE
        favoritesTable.visibility = View.GONE
        noFavoritesText.visibility = View.GONE
    }

    internal fun showFavoritesSection() {
        emailContainer.visibility = View.GONE
        passwordContainer.visibility = View.GONE
        logoutButton.visibility = View.GONE
        deleteAccountButton.visibility = View.GONE
        favoritesTitle.visibility = View.VISIBLE
        favoritesTable.visibility = View.VISIBLE
        noFavoritesText.visibility = if (favoritesTable.childCount <= 1) View.VISIBLE else View.GONE
    }
}

data class FavoriteItem(val title: String, val city: String, val deepLink: String, val docId: String)