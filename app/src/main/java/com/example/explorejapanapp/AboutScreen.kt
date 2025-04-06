package com.example.explorejapanapp.about

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
fun AboutScreen(aboutDao: AboutDao) {
    val scope = rememberCoroutineScope()
    var aboutContent by remember { mutableStateOf<About?>(null) }

    // Завантаження даних із бази при першому рендері
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            // Додаємо тестовий вміст (виконати один раз)
            aboutDao.deleteAll() // Очистити для тестування
            aboutDao.insert(
                About(
                    title = "Про ExploreJapan",
                    description = "Додаток для дослідження Японії."
                )
            )

            // Отримуємо вміст
            aboutContent = aboutDao.getAbout()
        }
    }

    // Відображення вмісту
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        aboutContent?.let { content ->
            Text(
                text = content.title,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp
            )
            Text(
                text = content.description,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp
            )
        } ?: Text("Завантаження...", style = MaterialTheme.typography.bodyLarge)
    }
}