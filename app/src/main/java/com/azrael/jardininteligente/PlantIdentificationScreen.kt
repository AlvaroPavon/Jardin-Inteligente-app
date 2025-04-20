package com.azrael.jardininteligente

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.navigation.NavController
import com.azrael.jardininteligente.api.ApiClient
import com.azrael.jardininteligente.api.models.PlantIdentifierRequest
import com.azrael.jardininteligente.api.models.PlantIdentifierResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

@Composable
fun PlantIdentificationScreen(navController: NavController) {
    val context = LocalContext.current
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var diagnosis by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    // Launcher para capturar imagen desde la cámara
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedImage = bitmap
        }
    }

    // Launcher para pedir permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            )
            if (permissionStatus == PERMISSION_GRANTED) {
                takePictureLauncher.launch(null)
            } else {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }) {
            Text("Tomar Foto")
        }

        Spacer(modifier = Modifier.height(16.dp))

        capturedImage?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto capturada",
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (capturedImage == null) {
                Toast.makeText(context, "Toma una foto primero", Toast.LENGTH_SHORT).show()
                return@Button
            }

            loading = true

            // Convertir Bitmap a Base64
            val outputStream = ByteArrayOutputStream()
            capturedImage!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val imageBytes = outputStream.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP) // usar NO_WRAP

            // Obtener token
            val prefs = context.getSharedPreferences("JardinInteligentePrefs", Context.MODE_PRIVATE)
            val token = prefs.getString("token", null)

            if (token.isNullOrEmpty()) {
                Toast.makeText(context, "No hay token, inicie sesión", Toast.LENGTH_SHORT).show()
                loading = false
                return@Button
            }

            val authHeader = "Bearer $token"

            // Llamar a la API
            ApiClient.apiService.identifyPlant(
                authHeader,
                PlantIdentifierRequest(image = base64Image)
            ).enqueue(object : Callback<PlantIdentifierResponse> {
                override fun onResponse(
                    call: Call<PlantIdentifierResponse>,
                    response: Response<PlantIdentifierResponse>
                ) {
                    loading = false
                    if (response.isSuccessful) {
                        diagnosis = response.body()?.suggestions?.firstOrNull()?.plant_name
                            ?: "Sin diagnóstico"
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Respuesta vacía"
                        diagnosis = "Error ${response.code()}: $errorMsg"
                    }
                }

                override fun onFailure(call: Call<PlantIdentifierResponse>, t: Throwable) {
                    loading = false
                    diagnosis = "Error: ${t.message}"
                }
            })
        }) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
            } else {
                Text("Identificar Planta")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (diagnosis.isNotEmpty()) {
            Text(text = "Diagnóstico: $diagnosis")
        }
    }
}
