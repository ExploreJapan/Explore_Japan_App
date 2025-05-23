package com.example.explorejapanapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Fragment_Profile : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var loginContainer: ConstraintLayout
    private lateinit var registerContainer: ConstraintLayout
    private lateinit var logoutButton: MaterialButton
    private lateinit var deleteAccountButton: MaterialButton
    private lateinit var newEmail: EditText
    private lateinit var updateEmailButton: MaterialButton
    private lateinit var newPassword: EditText
    private lateinit var confirmNewPassword: EditText
    private lateinit var updatePasswordButton: MaterialButton
    private lateinit var favoritesSpinner: Spinner
    private lateinit var noFavoritesText: TextView
    private lateinit var favoritesTitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loginContainer = view.findViewById(R.id.login_container)
        registerContainer = view.findViewById(R.id.register_container)
        logoutButton = view.findViewById(R.id.logout_button)
        deleteAccountButton = view.findViewById(R.id.delete_account_button)
        newEmail = view.findViewById(R.id.new_email)
        updateEmailButton = view.findViewById(R.id.update_email_button)
        newPassword = view.findViewById(R.id.new_password)
        confirmNewPassword = view.findViewById(R.id.confirm_new_password)
        updatePasswordButton = view.findViewById(R.id.update_password_button)
        favoritesSpinner = view.findViewById(R.id.favoritesSpinner)
        noFavoritesText = view.findViewById(R.id.no_favorites_text)
        favoritesTitle = view.findViewById(R.id.favorites_title)

        if (auth.currentUser != null) {
            loginContainer.visibility = View.GONE
            registerContainer.visibility = View.GONE
            newEmail.visibility = View.VISIBLE
            updateEmailButton.visibility = View.VISIBLE
            newPassword.visibility = View.VISIBLE
            confirmNewPassword.visibility = View.VISIBLE
            updatePasswordButton.visibility = View.VISIBLE
            logoutButton.visibility = View.VISIBLE
            deleteAccountButton.visibility = View.VISIBLE
            favoritesTitle.visibility = View.VISIBLE
            loadFavorites()
        } else {
            loginContainer.visibility = View.VISIBLE
            registerContainer.visibility = View.VISIBLE
            newEmail.visibility = View.GONE
            updateEmailButton.visibility = View.GONE
            newPassword.visibility = View.GONE
            confirmNewPassword.visibility = View.GONE
            updatePasswordButton.visibility = View.GONE
            logoutButton.visibility = View.GONE
            deleteAccountButton.visibility = View.GONE
            favoritesTitle.visibility = View.GONE
            favoritesSpinner.visibility = View.GONE
            noFavoritesText.visibility = View.GONE
        }

        // Налаштування кнопок входу та реєстрації
        setupLoginAndRegister(view)

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
                        favoritesSpinner.visibility = View.GONE
                        noFavoritesText.visibility = View.VISIBLE
                    } else {
                        val favorites = documents.map { doc ->
                            FavoriteItem(
                                title = doc.getString("title") ?: "",
                                city = doc.getString("city") ?: "",
                                deepLink = doc.getString("url") ?: ""
                            )
                        }

                        // Формуємо список для Spinner
                        val displayItems = mutableListOf<String>()
                        displayItems.add("Оберіть статтю") // Додаємо перший елемент для підказки
                        val favoriteItemsMap = favorites.associateBy { item ->
                            if (item.city == "All") {
                                item.title // Для "All" показуємо лише назву без скобок
                            } else {
                                val cityUkrainian = convertCityToUkrainian(item.city)
                                "${item.title} ($cityUkrainian)" // Для інших міст додаємо місто в дужках
                            }
                        }

                        displayItems.addAll(favoriteItemsMap.keys)

                        // Налаштовуємо адаптер для Spinner
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            displayItems
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        favoritesSpinner.adapter = adapter
                        favoritesSpinner.visibility = View.VISIBLE
                        noFavoritesText.visibility = View.GONE

                        // Додаємо слухач для Spinner
                        favoritesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                                if (position == 0) return // Пропускаємо перший елемент ("Оберіть статтю")
                                val selectedItem = displayItems[position]
                                val favoriteItem = favoriteItemsMap[selectedItem]
                                favoriteItem?.deepLink?.let { deepLink ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))
                                    startActivity(intent)
                                }
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {}
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Помилка завантаження обраного: ${e.message}", Toast.LENGTH_SHORT).show()
                    favoritesSpinner.visibility = View.GONE
                    noFavoritesText.visibility = View.VISIBLE
                }
        }
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
            else -> city // Якщо місто невідоме, повертаємо оригінальну назву
        }
    }

    private fun updateUIForLoggedOutUser() {
        loginContainer.visibility = View.VISIBLE
        registerContainer.visibility = View.VISIBLE
        newEmail.visibility = View.GONE
        updateEmailButton.visibility = View.GONE
        newPassword.visibility = View.GONE
        confirmNewPassword.visibility = View.GONE
        updatePasswordButton.visibility = View.GONE
        logoutButton.visibility = View.GONE
        deleteAccountButton.visibility = View.GONE
        favoritesTitle.visibility = View.GONE
        favoritesSpinner.visibility = View.GONE
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
                        newEmail.visibility = View.VISIBLE
                        updateEmailButton.visibility = View.VISIBLE
                        newPassword.visibility = View.VISIBLE
                        confirmNewPassword.visibility = View.VISIBLE
                        updatePasswordButton.visibility = View.VISIBLE
                        logoutButton.visibility = View.VISIBLE
                        deleteAccountButton.visibility = View.VISIBLE
                        favoritesTitle.visibility = View.VISIBLE
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
                        newEmail.visibility = View.VISIBLE
                        updateEmailButton.visibility = View.VISIBLE
                        newPassword.visibility = View.VISIBLE
                        confirmNewPassword.visibility = View.VISIBLE
                        updatePasswordButton.visibility = View.VISIBLE
                        logoutButton.visibility = View.VISIBLE
                        deleteAccountButton.visibility = View.VISIBLE
                        favoritesTitle.visibility = View.VISIBLE
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
            newEmail.visibility = View.VISIBLE
            updateEmailButton.visibility = View.VISIBLE
            newPassword.visibility = View.VISIBLE
            confirmNewPassword.visibility = View.VISIBLE
            updatePasswordButton.visibility = View.VISIBLE
            logoutButton.visibility = View.VISIBLE
            deleteAccountButton.visibility = View.VISIBLE
            favoritesTitle.visibility = View.VISIBLE
            loadFavorites()
        } else {
            loginContainer.visibility = View.VISIBLE
            registerContainer.visibility = View.VISIBLE
            newEmail.visibility = View.GONE
            updateEmailButton.visibility = View.GONE
            newPassword.visibility = View.GONE
            confirmNewPassword.visibility = View.GONE
            updatePasswordButton.visibility = View.GONE
            logoutButton.visibility = View.GONE
            deleteAccountButton.visibility = View.GONE
            favoritesTitle.visibility = View.GONE
            favoritesSpinner.visibility = View.GONE
            noFavoritesText.visibility = View.GONE
        }
    }
}

data class FavoriteItem(val title: String, val city: String, val deepLink: String)