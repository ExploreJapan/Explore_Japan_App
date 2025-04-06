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
fun MapScreen(mapDao: MapDao) {
    val scope = rememberCoroutineScope()
    var mapContent by remember { mutableStateOf<List<MapLocation>>(emptyList()) } // Змінено на List

    // Завантаження даних із бази при першому рендері
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            // Додаємо тестовий вміст (виконати один раз)
            mapDao.deleteAll() // Очистити для тестування
            mapDao.insert(
                MapLocation(
                    name = "Токіо",
                    latitude = 35.6762,
                    longitude = 139.6503,
                    description = "Столиця Японії."
                )
            )

            // Отримуємо вміст
            mapContent = mapDao.getAllLocations() // Змінено на список
        }
    }

    // Відображення вмісту
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (mapContent.isNotEmpty()) {
            mapContent.forEach { content ->
                Text(
                    text = content.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp
                )
                Text(
                    text = content.description ?: "Опис відсутній",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp
                )
            }
        } else {
            Text("Завантаження...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}