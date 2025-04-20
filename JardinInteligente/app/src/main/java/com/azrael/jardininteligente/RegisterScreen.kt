package com.azrael.jardininteligente

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.azrael.jardininteligente.api.ApiClient
import com.azrael.jardininteligente.api.models.ApiResponse
import com.azrael.jardininteligente.api.models.RegisterRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (correo.trim().isEmpty() || password.trim().isEmpty()){
                    Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                loading = true
                val registerRequest = RegisterRequest(correo.trim(), password.trim())
                ApiClient.apiService.registerUser(registerRequest).enqueue(object : Callback<ApiResponse> {
                    override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                        loading = false
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Registro exitoso. Inicie sesión.", Toast.LENGTH_SHORT).show()
                            navController.navigate("login") { popUpTo("register") { inclusive = true } }
                        } else {
                            Log.e("RegisterScreen", "Error en el registro: ${response.errorBody()?.string()}")
                            errorMessage = "Error en el registro: ${response.code()}"
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
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Registrarse")
            }
        }
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}
