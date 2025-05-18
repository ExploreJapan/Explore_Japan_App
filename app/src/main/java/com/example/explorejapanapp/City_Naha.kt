package com.example.explorejapanapp

import android.os.Bundle import android.util.Log import android.view.LayoutInflater import android.view.View import android.view.ViewGroup import android.widget.ImageButton import android.widget.LinearLayout import androidx.fragment.app.Fragment
import com.example.explorejapanapp.databinding.FragmentCityNahaBinding

class City_Naha : Fragment() {

    private var _binding: FragmentCityNahaBinding? = null
    private val binding get() = _binding!!
    private var isArticleOpen = false
    private var currentArticleContent: View? = null

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}