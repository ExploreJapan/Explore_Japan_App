package com.example.explorejapanapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SectionScreen(sectionDao: SectionDao) {
    val scope = rememberCoroutineScope()
    var sectionContent by remember { mutableStateOf<Section?>(null) }

    // Завантаження даних із бази при першому рендері
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            // Додаємо тестовий вміст (виконати один раз)
            sectionDao.deleteAll() // Очистити для тестування
            sectionDao.insert(
                Section(
                    title = "Культура Японії",
                    content = "Традиції, мистецтво та історія."
                )
            )

            // Отримуємо вміст
            sectionContent = sectionDao.getAllSections().firstOrNull()
        }
    }

    // Відображення вмісту
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        sectionContent?.let { content ->
            Text(
                text = content.title,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp
            )
            Text(
                text = content.content ?: "Вміст відсутній",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp
            )
        } ?: Text("Завантаження...", style = MaterialTheme.typography.bodyLarge)
    }
}