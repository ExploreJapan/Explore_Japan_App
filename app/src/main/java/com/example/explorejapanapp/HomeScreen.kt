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
fun HomeScreen(homeDao: HomeDao) {
    val scope = rememberCoroutineScope()
    var homeContent by remember { mutableStateOf<HomeContent?>(null) }

    // Завантаження даних із бази при першому рендері
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            // Додаємо тестовий вміст (виконати один раз)
            homeDao.deleteAll() // Очистити для тестування
            homeDao.insert(
                HomeContent(
                    title = "Ласкаво просимо до Японії",
                    description = "Дізнайтесь про культуру, традиції та красу Японії."
                )
            )

            // Отримуємо вміст
            homeContent = homeDao.getHomeContent()
        }
    }

    // Відображення вмісту
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        homeContent?.let { content ->
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