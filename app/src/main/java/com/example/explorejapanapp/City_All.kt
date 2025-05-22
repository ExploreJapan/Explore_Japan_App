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
import com.example.explorejapanapp.databinding.FragmentCityAllBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class City_All : Fragment() {

    private var _binding: FragmentCityAllBinding? = null
    private val binding get() = _binding!!
    private var isArticleOpen = false
    private var currentArticleContent: View? = null
    private var isArticleSaved = false
    private lateinit var favoriteButton: ImageButton
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val cityName = "All" // Используем "All", так как это общая страница

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCityAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Привязка карточек к обработчикам
        binding.articleLiving.setOnClickListener { showArticleContent("Проживання") }
        binding.articleTransport.setOnClickListener { showArticleContent("Аеропорти Японії") }
        binding.articleFood.setOnClickListener { showArticleContent("Етикет") }
        binding.articleHolidays.setOnClickListener { showArticleContent("Традиційні свята") }
        binding.articlePop.setOnClickListener { showArticleContent("Японська поп-культура") }
        binding.articleSouvenirs.setOnClickListener { showArticleContent("Сувеніри") }

        // Привязка кнопок сохранения к обработчикам
        binding.favoriteStarLiving.setOnClickListener { toggleFavoriteFromMain("Проживання", binding.favoriteStarLiving) }
        binding.favoriteStarAero.setOnClickListener { toggleFavoriteFromMain("Аеропорти Японії", binding.favoriteStarAero) }
        binding.favoriteStarEticet.setOnClickListener { toggleFavoriteFromMain("Етикет", binding.favoriteStarEticet) }
        binding.favoriteStarHolidays.setOnClickListener { toggleFavoriteFromMain("Традиційні свята", binding.favoriteStarHolidays) }
        binding.favoriteStarPop.setOnClickListener { toggleFavoriteFromMain("Японська поп-культура", binding.favoriteStarPop) }
        binding.favoriteStarSouvenirs.setOnClickListener { toggleFavoriteFromMain("Сувеніри", binding.favoriteStarSouvenirs) }

        // Проверка сохранения статей
        checkIfArticleSaved("Проживання", binding.favoriteStarLiving)
        checkIfArticleSaved("Аеропорти Японії", binding.favoriteStarAero)
        checkIfArticleSaved("Етикет", binding.favoriteStarEticet)
        checkIfArticleSaved("Традиційні свята", binding.favoriteStarHolidays)
        checkIfArticleSaved("Японська поп-культура", binding.favoriteStarPop)
        checkIfArticleSaved("Сувеніри", binding.favoriteStarSouvenirs)

        // Поддержка deep link
        val articleToOpen = arguments?.getString("articleToOpen")
        if (articleToOpen != null) {
            showArticleContent(articleToOpen)
        }
    }

    private fun showArticleContent(title: String) {
        if (!isArticleOpen) {
            Log.d("CityAll", "Starting showArticleContent for title: $title")

            if (binding.mainContent == null) {
                Log.e("CityAll", "binding.mainContent is null")
                return
            }
            if (binding.overlayContainer == null) {
                Log.e("CityAll", "binding.overlayContainer is null")
                return
            }

            binding.mainContent.visibility = View.GONE
            binding.overlayContainer.visibility = View.VISIBLE

            binding.overlayContainer.post {
                Log.d("CityAll", "overlayContainer width: ${binding.overlayContainer.width}, height: ${binding.overlayContainer.height}")
            }

            val overlayContentContainer = binding.overlayContainer.findViewById<LinearLayout>(R.id.overlay_content_container)
            if (overlayContentContainer == null) {
                Log.e("CityAll", "overlayContentContainer is null")
                return
            }

            overlayContentContainer.removeAllViews()

            currentArticleContent = when (title) {
                "Проживання" -> binding.contentLiving
                "Аеропорти Японії" -> binding.contentAero
                "Етикет" -> binding.contentEticet
                "Традиційні свята" -> binding.contentHolidays
                "Японська поп-культура" -> binding.contentPop
                "Сувеніри" -> binding.contentSouvenirs
                else -> null
            }

            if (currentArticleContent == null) {
                Log.e("CityAll", "currentArticleContent is null for title: $title")
                return
            }

            currentArticleContent?.let { content ->
                Log.d("CityAll", "Current article content ID: ${content.id}")
                if (content.parent != null) {
                    (content.parent as? ViewGroup)?.removeView(content)
                    Log.d("CityAll", "Removed view from parent: ${content.id}")
                }

                content.visibility = View.VISIBLE
                try {
                    overlayContentContainer.addView(content)
                    Log.d("CityAll", "Content added to overlay: ${content.id}")
                    Log.d("CityAll", "overlayContentContainer child count: ${overlayContentContainer.childCount}")
                } catch (e: Exception) {
                    Log.e("CityAll", "Error adding content to overlay: ${e.message}")
                }

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

            val backButton = binding.overlayContainer.findViewById<ImageButton>(R.id.backButton)
            if (backButton == null) {
                Log.e("CityAll", "backButton is null")
                return
            }

            backButton.setOnClickListener {
                Log.d("CityAll", "Back button clicked")
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
            Log.d("CityAll", "Article opened successfully")
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
                    Log.e("CityAll", "Failed to check if article is saved: ${e.message}")
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
            "Аеропорти Японії" -> binding.favoriteStarAero
            "Етикет" -> binding.favoriteStarEticet
            "Традиційні свята" -> binding.favoriteStarHolidays
            "Японська поп-культура" -> binding.favoriteStarPop
            "Сувеніри" -> binding.favoriteStarSouvenirs
            else -> null
        }
        starIcon?.setImageResource(if (isArticleSaved) R.drawable.ic_star_filled else R.drawable.ic_star_outline)

        val docId = "${cityName}_$title"
        val articleRef = db.collection("users").document(user.uid)
            .collection("favorites").document(docId)

        if (isArticleSaved) {
            val deepLink = "explorejapanapp://all?article=$title"
            val articleData = hashMapOf(
                "url" to deepLink,
                "title" to title,
                "city" to cityName
            )
            articleRef.set(articleData)
                .addOnSuccessListener {
                    Log.d("CityAll", "Article $docId saved to favorites with URL: $deepLink")
                    Toast.makeText(requireContext(), "Стаття додана до Обраного", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("CityAll", "Failed to save article: ${e.message}")
                    Toast.makeText(requireContext(), "Помилка збереження", Toast.LENGTH_SHORT).show()
                }
        } else {
            articleRef.delete()
                .addOnSuccessListener {
                    Log.d("CityAll", "Article $docId removed from favorites")
                    Toast.makeText(requireContext(), "Стаття видалена з Обраного", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("CityAll", "Failed to remove article: ${e.message}")
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
                        Log.e("CityAll", "Failed to remove article: ${e.message}")
                        Toast.makeText(requireContext(), "Помилка видалення", Toast.LENGTH_SHORT).show()
                    }
            } else {
                val deepLink = "explorejapanapp://all?article=$title"
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
                        Log.e("CityAll", "Failed to save article: ${e.message}")
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