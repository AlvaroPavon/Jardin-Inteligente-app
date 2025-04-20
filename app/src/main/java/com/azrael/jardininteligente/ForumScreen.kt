package com.azrael.jardininteligente

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.azrael.jardininteligente.api.ApiClient
import com.azrael.jardininteligente.api.models.ForumTopic
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(navController: NavController) {
    val context = LocalContext.current
    var topics by remember { mutableStateOf(listOf<ForumTopic>()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    // Recuperar token de SharedPreferences.
    val prefs = context.getSharedPreferences("JardinInteligentePrefs", Context.MODE_PRIVATE)
    val token = prefs.getString("token", null)

    LaunchedEffect(Unit) {
        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "No hay token, inicie sesión nuevamente", Toast.LENGTH_SHORT).show()
            navController.navigate("login") {
                popUpTo("forum") { inclusive = true }
            }
        } else {
            val authHeader = "Bearer $token"
            ApiClient.apiService.getForumTopics(authHeader).enqueue(object : Callback<List<ForumTopic>> {
                override fun onResponse(call: Call<List<ForumTopic>>, response: Response<List<ForumTopic>>) {
                    loading = false
                    if (response.isSuccessful) {
                        topics = response.body() ?: emptyList()
                    } else {
                        errorMessage = "Error ${response.code()} – ${response.message()}"
                        Log.e("ForumScreen", errorMessage)
                    }
                }
                override fun onFailure(call: Call<List<ForumTopic>>, t: Throwable) {
                    loading = false
                    errorMessage = "Error: ${t.message}"
                    Log.e("ForumScreen", errorMessage)
                }
            })
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Foro de Plantas") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("new_topic") }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Tema")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            when {
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMessage.isNotEmpty() -> {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(topics) { topic ->
                            TopicItem(topic = topic)
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopicItem(topic: ForumTopic) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = topic.titulo, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = topic.contenido, style = MaterialTheme.typography.bodyMedium)
        Text(text = "Por: ${topic.autor} – ${topic.fecha_creacion}", style = MaterialTheme.typography.bodySmall)
    }
}
