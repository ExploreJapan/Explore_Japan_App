package com.example.explorejapanapp.account

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
fun AccountScreen(accountDao: AccountDao) {
    val scope = rememberCoroutineScope()
    var accountContent by remember { mutableStateOf<Account?>(null) }

    // Завантаження даних із бази при першому рендері
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            // Додаємо тестовий вміст (виконати один раз)
            accountDao.deleteAll() // Очистити для тестування
            accountDao.insert(
                Account(
                    username = "Користувач",
                    email = "user@example.com",
                    language = "ua",
                    theme = "light"
                )
            )

            // Отримуємо вміст
            accountContent = accountDao.getAccount()
        }
    }

    // Відображення вмісту
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        accountContent?.let { content ->
            Text(
                text = content.username,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp
            )
            Text(
                text = content.email ?: "Email не вказано",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp
            )
        } ?: Text("Завантаження...", style = MaterialTheme.typography.bodyLarge)
    }
}