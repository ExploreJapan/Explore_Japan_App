package com.example.explorejapanapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.util.Log
import com.example.explorejapanapp.databinding.FragmentCityTokyoBinding

class City_Tokyo : Fragment() {
    private var _binding: FragmentCityTokyoBinding? = null
    private val binding get() = _binding!!
    private var isArticleOpen = false
    private var currentArticleContent: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCityTokyoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val livingCard = binding.articleLiving
        val transportCard = binding.articleTransport
        val foodCard = binding.articleFood
        val sightsCard = binding.articleSights

        livingCard.setOnClickListener { showArticleContent("Проживання") }
        transportCard.setOnClickListener { showArticleContent("Транспорт") }
        foodCard.setOnClickListener { showArticleContent("Їжа") }
        sightsCard.setOnClickListener { showArticleContent("Визначні пам’ятки") }
    }

    private fun showArticleContent(title: String) {
        if (!isArticleOpen) {
            Log.d("CityTokyo", "Starting showArticleContent for title: $title")

            // Проверяем, что binding.mainContent и binding.overlayContainer не null
            if (binding.mainContent == null) {
                Log.e("CityTokyo", "binding.mainContent is null")
                return
            }
            if (binding.overlayContainer == null) {
                Log.e("CityTokyo", "binding.overlayContainer is null")
                return
            }

            Log.d("CityTokyo", "Setting mainContent visibility to GONE")
            binding.mainContent.visibility = View.GONE
            Log.d("CityTokyo", "Setting overlayContainer visibility to VISIBLE")
            binding.overlayContainer.visibility = View.VISIBLE

            // Проверяем размеры оверлея
            binding.overlayContainer.post {
                Log.d("CityTokyo", "overlayContainer width: ${binding.overlayContainer.width}, height: ${binding.overlayContainer.height}")
            }

            // Находим контейнер в оверлее
            val overlayContentContainer = binding.overlayContainer.findViewById<LinearLayout>(R.id.overlay_content_container)
            if (overlayContentContainer == null) {
                Log.e("CityTokyo", "overlayContentContainer is null")
                return
            }

            // Удаляем предыдущее содержимое из оверлея
            Log.d("CityTokyo", "Removing all views from overlayContentContainer")
            overlayContentContainer.removeAllViews()

            // Определяем, какой контент показывать
            currentArticleContent = when (title) {
                "Проживання" -> binding.contentLiving
                "Транспорт" -> binding.contentTransport
                "Їжа" -> binding.contentFood
                "Визначні пам’ятки" -> binding.contentSights
                else -> null
            }
            if (currentArticleContent == null) {
                Log.e("CityTokyo", "currentArticleContent is null for title: $title")
                return
            }

            // Добавляем содержимое в оверлей
            currentArticleContent?.let {
                Log.d("CityTokyo", "Current article content ID: ${it.id}")
                // Проверяем, есть ли у представления родитель, и удаляем его
                if (it.parent != null) {
                    (it.parent as? ViewGroup)?.removeView(it)
                    Log.d("CityTokyo", "Removed view from parent: ${it.id}")
                }

                it.visibility = View.VISIBLE
                try {
                    overlayContentContainer.addView(it)
                    Log.d("CityTokyo", "Content added to overlay: ${it.id}")
                    Log.d("CityTokyo", "overlayContentContainer child count: ${overlayContentContainer.childCount}")
                } catch (e: Exception) {
                    Log.e("CityTokyo", "Error adding content to overlay: ${e.message}")
                }
            }

            // Настраиваем кнопку закрытия
            val closeButton = binding.overlayContainer.findViewById<Button>(R.id.closeButton)
            if (closeButton == null) {
                Log.e("CityTokyo", "closeButton is null")
                return
            }

            closeButton.setOnClickListener {
                Log.d("CityTokyo", "Close button clicked")
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
            Log.d("CityTokyo", "Article opened successfully")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}