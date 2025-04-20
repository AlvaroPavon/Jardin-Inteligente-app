package com.azrael.jardininteligente

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.azrael.jardininteligente.ui.JardinInteligenteApp
import com.azrael.jardininteligente.workers.ForumNewPostWorker
import com.azrael.jardininteligente.workers.WateringReminderWorker
import java.util.concurrent.TimeUnit
import com.azrael.jardininteligente.ui.theme.JardinInteligenteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JardinInteligenteTheme {
                JardinInteligenteApp() // ← ya no se pasa nada
            }
        }

        val wateringWorkRequest = PeriodicWorkRequestBuilder<WateringReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "wateringReminder",
            ExistingPeriodicWorkPolicy.REPLACE,
            wateringWorkRequest
        )

        val forumWorkRequest = PeriodicWorkRequestBuilder<ForumNewPostWorker>(1, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "forumNewPost",
            ExistingPeriodicWorkPolicy.REPLACE,
            forumWorkRequest
        )
    }
}

