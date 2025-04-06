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
fun TipScreen(tipDao: TipDao) {
    val scope = rememberCoroutineScope()
    var tipContent by remember { mutableStateOf<Tip?>(null) }

    // Завантаження даних із бази при першому рендері
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            // Додаємо тестовий вміст (виконати один раз)
            tipDao.deleteAll() // Очистити для тестування
            tipDao.insert(
                Tip(
                    title = "Подорожуйте економно",
                    tipText = "Користуйтесь місцевими транспортними картками."
                )
            )

            // Отримуємо вміст
            tipContent = tipDao.getAllTips().firstOrNull()
        }
    }

    // Відображення вмісту
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        tipContent?.let { content ->
            Text(
                text = content.title,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp
            )
            Text(
                text = content.tipText,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp
            )
        } ?: Text("Завантаження...", style = MaterialTheme.typography.bodyLarge)
    }
}