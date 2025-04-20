package com.azrael.jardininteligente

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.azrael.jardininteligente.api.ApiClient
import com.azrael.jardininteligente.api.models.ApiResponse
import com.azrael.jardininteligente.api.models.NewForumTopicRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun NewTopicScreen(navController: NavController) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val context = LocalContext.current
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Contenido") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (title.isBlank() || content.isBlank()) {
                    Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                loading = true
                val prefs = context.getSharedPreferences("JardinInteligentePrefs", Context.MODE_PRIVATE)
                val token = prefs.getString("token", null)
                if (token.isNullOrEmpty()) {
                    Toast.makeText(context, "No hay token, inicie sesión", Toast.LENGTH_SHORT).show()
                    loading = false
                    return@Button
                }
                val authHeader = "Bearer $token"
                val request = NewForumTopicRequest(titulo = title, contenido = content)
                ApiClient.apiService.createForumTopic(authHeader, request).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        loading = false
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Tema creado exitosamente", Toast.LENGTH_SHORT).show()
                            navController.navigate("forum") {
                                popUpTo("new_topic") { inclusive = true }
                            }
                        } else {
                            errorMessage = "Error ${response.code()} – ${response.message()}"
                        }
                    }
                    override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                        loading = false
                        errorMessage = "Error: ${t.message}"
                    }
                })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text("Crear Tema")
            }
        }
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}
