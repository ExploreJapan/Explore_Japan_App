package com.example.explorejapanapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.explorejapanapp.databinding.FragmentCityNahaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class City_Naha : Fragment() {

    private var _binding: FragmentCityNahaBinding? = null
    private val binding get() = _binding!!
    private var isArticleOpen = false
    private var currentArticleContent: View? = null
    private var isArticleSaved = false
    private lateinit var favoriteButton: ImageButton
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val cityName = "Naha"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCityNahaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Привязка карточек к обработчикам
        binding.articleLiving.setOnClickListener { showArticleContent("Проживання") }
        binding.articleTransport.setOnClickListener { showArticleContent("Транспорт") }
        binding.articleFood.setOnClickListener { showArticleContent("Їжа") }
        binding.articlePlace.setOnClickListener { showArticleContent("Пам`ятки культури") }

        // Привязка кнопок збереження до обробників
        binding.favoriteStarLiving.setOnClickListener { toggleFavoriteFromMain("Проживання", binding.favoriteStarLiving) }
        binding.favoriteStarTransport.setOnClickListener { toggleFavoriteFromMain("Транспорт", binding.favoriteStarTransport) }
        binding.favoriteStarFood.setOnClickListener { toggleFavoriteFromMain("Їжа", binding.favoriteStarFood) }
        binding.favoriteStarPlace.setOnClickListener { toggleFavoriteFromMain("Пам`ятки культури", binding.favoriteStarPlace) }

        // Перевірка збереження статей
        checkIfArticleSaved("Проживання", binding.favoriteStarLiving)
        checkIfArticleSaved("Транспорт", binding.favoriteStarTransport)
        checkIfArticleSaved("Їжа", binding.favoriteStarFood)
        checkIfArticleSaved("Пам`ятки культури", binding.favoriteStarPlace)

        // Підтримка deep link
        val articleToOpen = arguments?.getString("articleToOpen")
        if (articleToOpen != null) {
            showArticleContent(articleToOpen)
        }
    }

    private fun showArticleContent(title: String) {
        if (!isArticleOpen) {
            Log.d("CityNaha", "Starting showArticleContent for title: $title")

            // Проверяем, что binding.mainContent и binding.overlayContainer не null
            if (binding.mainContent == null) {
                Log.e("CityNaha", "binding.mainContent is null")
                return
            }
            if (binding.overlayContainer == null) {
                Log.e("CityNaha", "binding.overlayContainer is null")
                return
            }

            // Скрываем основной контент и показываем оверлей
            binding.mainContent.visibility = View.GONE
            binding.overlayContainer.visibility = View.VISIBLE

            // Проверяем размеры оверлея
            binding.overlayContainer.post {
                Log.d("CityNaha", "overlayContainer width: ${binding.overlayContainer.width}, height: ${binding.overlayContainer.height}")
            }

            // Находим контейнер в оверлее
            val overlayContentContainer = binding.overlayContainer.findViewById<LinearLayout>(R.id.overlay_content_container)
            if (overlayContentContainer == null) {
                Log.e("CityNaha", "overlayContentContainer is null")
                return
            }

            // Удаляем предыдущее содержимое из оверлея
            overlayContentContainer.removeAllViews()

            // Определяем, какой контент показывать
            currentArticleContent = when (title) {
                "Проживання" -> binding.contentLiving
                "Транспорт" -> binding.contentTransport
                "Їжа" -> binding.contentFood
                "Пам`ятки культури" -> binding.contentPlace
                else -> null
            }

            if (currentArticleContent == null) {
                Log.e("CityNaha", "currentArticleContent is null for title: $title")
                return
            }

            // Добавляем содержимое в оверлей
            currentArticleContent?.let { content ->
                Log.d("CityNaha", "Current article content ID: ${content.id}")
                // Проверяем, есть ли у представления родитель, и удаляем его
                if (content.parent != null) {
                    (content.parent as? ViewGroup)?.removeView(content)
                    Log.d("CityNaha", "Removed view from parent: ${content.id}")
                }

                content.visibility = View.VISIBLE
                try {
                    overlayContentContainer.addView(content)
                    Log.d("CityNaha", "Content added to overlay: ${content.id}")
                    Log.d("CityNaha", "overlayContentContainer child count: ${overlayContentContainer.childCount}")
                } catch (e: Exception) {
                    Log.e("CityNaha", "Error adding content to overlay: ${e.message}")
                }

                // Перевіряємо, чи стаття збережена, і додаємо кнопку збереження
                checkIfArticleSaved(title)

                favoriteButton = ImageButton(requireContext()).apply {
                    setImageResource(if (isArticleSaved) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(16, 16, 16, 16)
                    }
                    setOnClickListener { toggleFavorite(title) }
                }
                overlayContentContainer.addView(favoriteButton)
            }

            // Настраиваем кнопку возврата
            val backButton = binding.overlayContainer.findViewById<ImageButton>(R.id.backButton)
            if (backButton == null) {
                Log.e("CityNaha", "backButton is null")
                return
            }

            // Кнопка возврата: закрывает оверлей и возвращает к списку статей
            backButton.setOnClickListener {
                Log.d("CityNaha", "Back button clicked")
                binding.overlayContainer.visibility = View.GONE
                binding.mainContent.visibility = View.VISIBLE
                currentArticleContent?.let { content ->
                    if (content.parent != null) {
                        (content.parent as? ViewGroup)?.removeView(content)
                    }
                    content.visibility = View.GONE
                }
                isArticleOpen = false
            }

            isArticleOpen = true
            Log.d("CityNaha", "Article opened successfully")
        }
    }

    private fun checkIfArticleSaved(title: String, starIcon: ImageView? = null) {
        val user = auth.currentUser
        if (user != null) {
            val docId = "${cityName}_$title"
            db.collection("users").document(user.uid)
                .collection("favorites").document(docId)
                .get()
                .addOnSuccessListener { document ->
                    isArticleSaved = document.exists()
                    starIcon?.setImageResource(if (isArticleSaved) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
                    if (starIcon == null) {
                        favoriteButton.setImageResource(if (isArticleSaved) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("CityNaha", "Failed to check if article is saved: ${e.message}")
                }
        }
    }

    private fun toggleFavorite(title: String) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Для збереження увійдіть у свій акаунт", Toast.LENGTH_SHORT).show()
            return
        }

        isArticleSaved = !isArticleSaved
        favoriteButton.setImageResource(if (isArticleSaved) R.drawable.ic_star_filled else R.drawable.ic_star_outline)

        val starIcon = when (title) {
            "Проживання" -> binding.favoriteStarLiving
            "Транспорт" -> binding.favoriteStarTransport
            "Їжа" -> binding.favoriteStarFood
            "Пам`ятки культури" -> binding.favoriteStarPlace
            else -> null
        }
        starIcon?.setImageResource(if (isArticleSaved) R.drawable.ic_star_filled else R.drawable.ic_star_outline)

        val docId = "${cityName}_$title"
        val articleRef = db.collection("users").document(user.uid)
            .collection("favorites").document(docId)

        if (isArticleSaved) {
            val deepLink = "explorejapanapp://naha?article=$title"
            val articleData = hashMapOf(
                "url" to deepLink,
                "title" to title,
                "city" to cityName
            )
            articleRef.set(articleData)
                .addOnSuccessListener {
                    Log.d("CityNaha", "Article $docId saved to favorites with URL: $deepLink")
                    Toast.makeText(requireContext(), "Стаття додана до Обраного", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("CityNaha", "Failed to save article: ${e.message}")
                    Toast.makeText(requireContext(), "Помилка збереження", Toast.LENGTH_SHORT).show()
                }
        } else {
            articleRef.delete()
                .addOnSuccessListener {
                    Log.d("CityNaha", "Article $docId removed from favorites")
                    Toast.makeText(requireContext(), "Стаття видалена з Обраного", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("CityNaha", "Failed to remove article: ${e.message}")
                    Toast.makeText(requireContext(), "Помилка видалення", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun toggleFavoriteFromMain(title: String, starIcon: ImageView) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Для збереження увійдіть у свій акаунт", Toast.LENGTH_SHORT).show()
            return
        }

        val docId = "${cityName}_$title"
        val articleRef = db.collection("users").document(user.uid)
            .collection("favorites").document(docId)

        articleRef.get().addOnSuccessListener { document ->
            val wasSaved = document.exists()
            if (wasSaved) {
                articleRef.delete()
                    .addOnSuccessListener {
                        starIcon.setImageResource(R.drawable.ic_star_outline)
                        Toast.makeText(requireContext(), "Стаття видалена з Обраного", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("CityNaha", "Failed to remove article: ${e.message}")
                        Toast.makeText(requireContext(), "Помилка видалення", Toast.LENGTH_SHORT).show()
                    }
            } else {
                val deepLink = "explorejapanapp://naha?article=$title"
                val articleData = hashMapOf(
                    "url" to deepLink,
                    "title" to title,
                    "city" to cityName
                )
                articleRef.set(articleData)
                    .addOnSuccessListener {
                        starIcon.setImageResource(R.drawable.ic_star_filled)
                        Toast.makeText(requireContext(), "Стаття додана до Обраного", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("CityNaha", "Failed to save article: ${e.message}")
                        Toast.makeText(requireContext(), "Помилка збереження", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}