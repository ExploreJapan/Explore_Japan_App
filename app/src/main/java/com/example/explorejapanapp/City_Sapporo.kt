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
import com.example.explorejapanapp.databinding.FragmentCitySapporoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class City_Sapporo : Fragment() {

    private var _binding: FragmentCitySapporoBinding? = null
    private val binding get() = _binding!!
    private var isArticleOpen = false
    private var currentArticleContent: View? = null
    private var isArticleSaved = false
    private lateinit var favoriteButton: ImageButton
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val cityName = "Sapporo"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCitySapporoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Налаштування обробників подій для статей
        binding.articleLiving.setOnClickListener { showArticleContent("Проживання") }
        binding.articleTransport.setOnClickListener { showArticleContent("Транспорт") }
        binding.articleFood.setOnClickListener { showArticleContent("Їжа") }
        binding.articlePlace.setOnClickListener { showArticleContent("Пам’ятки культури") }

        // Налаштування обробників для зірок
        try {
            binding.favoriteStarLiving.setOnClickListener { toggleFavoriteFromMain("Проживання", binding.favoriteStarLiving) }
            binding.favoriteStarTransport.setOnClickListener { toggleFavoriteFromMain("Транспорт", binding.favoriteStarTransport) }
            binding.favoriteStarFood.setOnClickListener { toggleFavoriteFromMain("Їжа", binding.favoriteStarFood) }
            binding.favoriteStarPlace.setOnClickListener { toggleFavoriteFromMain("Пам’ятки культури", binding.favoriteStarPlace) }

            // Перевірка стану збереження статей
            checkIfArticleSaved("Проживання", binding.favoriteStarLiving)
            checkIfArticleSaved("Транспорт", binding.favoriteStarTransport)
            checkIfArticleSaved("Їжа", binding.favoriteStarFood)
            checkIfArticleSaved("Пам’ятки культури", binding.favoriteStarPlace)
        } catch (e: Exception) {
            Log.e("CitySapporo", "Error setting up favorite stars: ${e.message}")
        }

        // Відкриття статті з аргументів
        val articleToOpen = arguments?.getString("articleToOpen")
        if (articleToOpen != null) {
            showArticleContent(articleToOpen)
        }
    }

    private fun showArticleContent(title: String) {
        if (!isArticleOpen) {
            Log.d("CitySapporo", "Starting showArticleContent for title: $title")

            if (binding.mainContent == null) {
                Log.e("CitySapporo", "binding.mainContent is null")
                return
            }
            if (binding.overlayContainer == null) {
                Log.e("CitySapporo", "binding.overlayContainer is null")
                return
            }

            binding.mainContent.visibility = View.GONE
            binding.overlayContainer.visibility = View.VISIBLE

            binding.overlayContainer.post {
                Log.d("CitySapporo", "overlayContainer width: ${binding.overlayContainer.width}, height: ${binding.overlayContainer.height}")
            }

            val overlayContentContainer = binding.overlayContainer.findViewById<LinearLayout>(R.id.overlay_content_container)
            if (overlayContentContainer == null) {
                Log.e("CitySapporo", "overlayContentContainer is null")
                return
            }

            overlayContentContainer.removeAllViews()

            currentArticleContent = when (title) {
                "Проживання" -> binding.contentLiving
                "Транспорт" -> binding.contentTransport
                "Їжа" -> binding.contentFood
                "Пам’ятки культури" -> binding.contentPlace
                else -> null
            }

            if (currentArticleContent == null) {
                Log.e("CitySapporo", "currentArticleContent is null for title: $title")
                return
            }

            currentArticleContent?.let { content ->
                if (content.parent != null) {
                    (content.parent as? ViewGroup)?.removeView(content)
                    Log.d("CitySapporo", "Removed view from parent: ${content.id}")
                }

                content.visibility = View.VISIBLE
                try {
                    overlayContentContainer.addView(content)
                    Log.d("CitySapporo", "Content added to overlay: ${content.id}")
                    Log.d("CitySapporo", "overlayContentContainer child count: ${overlayContentContainer.childCount}")
                } catch (e: Exception) {
                    Log.e("CitySapporo", "Error adding content to overlay: ${e.message}")
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
                Log.e("CitySapporo", "backButton is null")
                return
            }

            backButton.setOnClickListener {
                Log.d("CitySapporo", "Back button clicked")
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
            Log.d("CitySapporo", "Article opened successfully")
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
                    Log.d("CitySapporo", "Article $docId saved: $isArticleSaved")
                }
                .addOnFailureListener { e ->
                    Log.e("CitySapporo", "Failed to check if article is saved: ${e.message}")
                }
        } else {
            Log.d("CitySapporo", "User is null, skipping checkIfArticleSaved")
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
            "Пам’ятки культури" -> binding.favoriteStarPlace
            else -> null
        }
        starIcon?.setImageResource(if (isArticleSaved) R.drawable.ic_star_filled else R.drawable.ic_star_outline)

        val docId = "${cityName}_$title"
        val articleRef = db.collection("users").document(user.uid)
            .collection("favorites").document(docId)

        if (isArticleSaved) {
            val deepLink = "explorejapanapp://sapporo?article=$title"
            val articleData = hashMapOf(
                "url" to deepLink,
                "title" to title,
                "city" to cityName
            )
            articleRef.set(articleData)
                .addOnSuccessListener {
                    Log.d("CitySapporo", "Article $docId saved to favorites with URL: $deepLink")
                    Toast.makeText(requireContext(), "Стаття додана до Обраного", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("CitySapporo", "Failed to save article: ${e.message}")
                    Toast.makeText(requireContext(), "Помилка збереження", Toast.LENGTH_SHORT).show()
                }
        } else {
            articleRef.delete()
                .addOnSuccessListener {
                    Log.d("CitySapporo", "Article $docId removed from favorites")
                    Toast.makeText(requireContext(), "Стаття видалена з Обраного", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("CitySapporo", "Failed to remove article: ${e.message}")
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
                        Log.d("CitySapporo", "Article $docId removed from favorites")
                    }
                    .addOnFailureListener { e ->
                        Log.e("CitySapporo", "Failed to remove article: ${e.message}")
                        Toast.makeText(requireContext(), "Помилка видалення", Toast.LENGTH_SHORT).show()
                    }
            } else {
                val deepLink = "explorejapanapp://sapporo?article=$title"
                val articleData = hashMapOf(
                    "url" to deepLink,
                    "title" to title,
                    "city" to cityName
                )
                articleRef.set(articleData)
                    .addOnSuccessListener {
                        starIcon.setImageResource(R.drawable.ic_star_filled)
                        Toast.makeText(requireContext(), "Стаття додана до Обраного", Toast.LENGTH_SHORT).show()
                        Log.d("CitySapporo", "Article $docId saved to favorites")
                    }
                    .addOnFailureListener { e ->
                        Log.e("CitySapporo", "Failed to save article: ${e.message}")
                        Toast.makeText(requireContext(), "Помилка збереження", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("CitySapporo", "Failed to check article: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}