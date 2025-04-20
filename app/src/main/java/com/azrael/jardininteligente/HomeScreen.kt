package com.azrael.jardininteligente

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azrael.jardininteligente.api.ApiClient
import com.azrael.jardininteligente.api.ApiService.UserResponse
import com.azrael.jardininteligente.api.models.Plant
import com.azrael.jardininteligente.api.models.UserResponse
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

sealed class DrawerMenuItem(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : DrawerMenuItem("inicio", "Inicio", Icons.Filled.Home)
    object Identificar : DrawerMenuItem("identificar", "Identificar", Icons.Filled.CameraAlt)
    object Foro : DrawerMenuItem("foro", "Foro", Icons.Filled.Forum)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color.Transparent,
        drawerContent = {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.75f),
                color = Color(0xFFF0F0F0)
            ) {
                DrawerContent(navController = navController) {
                    coroutineScope.launch { drawerState.close() }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Jardín Inteligente") },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menú",
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                )
            },
            content = { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = DrawerMenuItem.Inicio.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(DrawerMenuItem.Inicio.route) { MainScreenContent(navController) }
                    composable(DrawerMenuItem.Identificar.route) { PlantIdentificationScreen(navController) }
                    composable(DrawerMenuItem.Foro.route) { ForumScreen(navController) }
                }
            }
        )
    }
}

@Composable
fun DrawerContent(navController: NavHostController, onItemSelected: () -> Unit) {
    val menuItems = listOf(
        DrawerMenuItem.Inicio,
        DrawerMenuItem.Identificar,
        DrawerMenuItem.Foro
    )

    val context = LocalContext.current
    var correo by remember { mutableStateOf("") }

    // Cargar el nombre del usuario desde la API
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("JardinInteligentePrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("token", null)
        if (!token.isNullOrEmpty()) {
            val authHeader = "Bearer $token"
            ApiClient.apiService.getUserInfo(authHeader).enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.isSuccessful) {
                        correo = response.body()?.correo ?: ""
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    // Puedes manejar el error si quieres
                }
            })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = if (correo.isNotEmpty()) "¡Bienvenido, $correo!" else "¡Bienvenido!",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        menuItems.forEachIndexed { index, item ->
            DrawerItem(item = item) {
                navController.navigate(item.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
                onItemSelected()
            }

            if (index != menuItems.lastIndex) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DrawerItem(item: DrawerMenuItem, onClick: () -> Unit) {
    NavigationDrawerItem(
        icon = { Icon(item.icon, contentDescription = item.title) },
        label = { Text(text = item.title, style = MaterialTheme.typography.titleMedium) },
        selected = false,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}

@Composable
fun MainScreenContent(navController: NavHostController) {
    val context = LocalContext.current
    var plantList by remember { mutableStateOf(listOf<Plant>()) }
    var loading by remember { mutableStateOf(true) }

    val prefs = context.getSharedPreferences("JardinInteligentePrefs", Context.MODE_PRIVATE)
    val token = prefs.getString("token", null)

    LaunchedEffect(Unit) {
        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "No se encontró token, inicie sesión nuevamente", Toast.LENGTH_SHORT).show()
            navController.navigate("login") { popUpTo("inicio") { inclusive = true } }
        } else {
            val authHeader = "Bearer $token"
            ApiClient.apiService.getPlants(authHeader).enqueue(object : Callback<List<Plant>> {
                override fun onResponse(call: Call<List<Plant>>, response: Response<List<Plant>>) {
                    loading = false
                    if (response.isSuccessful) {
                        plantList = response.body() ?: emptyList()
                    } else {
                        Toast.makeText(context, "Error al cargar plantas", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Plant>>, t: Throwable) {
                    loading = false
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            plantList.isEmpty() -> Text(
                text = "No hay plantas registradas",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(plantList) { plant ->
                        PlantItem(plant = plant)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun PlantItem(plant: Plant) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = plant.nombre, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = plant.descripcion, style = MaterialTheme.typography.bodyMedium)
    }
}
