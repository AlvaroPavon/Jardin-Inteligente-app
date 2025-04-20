package com.azrael.jardininteligente.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.azrael.jardininteligente.ForumScreen
import com.azrael.jardininteligente.PlantIdentificationScreen
import com.azrael.jardininteligente.api.ApiClient
import com.azrael.jardininteligente.api.models.Plant
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Sealed class para definir los items del menú del drawer.
sealed class DrawerMenuItem(val route: String, val title: String, val icon: ImageVector) {
    object Inicio : DrawerMenuItem("inicio", "Inicio", Icons.Filled.Home)
    object Identificar : DrawerMenuItem("identificar", "Identificar", Icons.Filled.CameraAlt)
    object Foro : DrawerMenuItem("foro", "Foro", Icons.Filled.Forum)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val navController = rememberNavController() // ✅ Aquí se crea correctamente
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController = navController) {
                coroutineScope.launch { drawerState.close() }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Jardín Inteligente") },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Default.Menu,
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
                    startDestination = "inicio",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("inicio") { MainScreenContent(navController) }
                    composable("identificar") { PlantIdentificationScreen(navController) }
                    composable("foro") { ForumScreen(navController) }
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
    Column(modifier = Modifier.fillMaxSize()) {
        menuItems.forEach { item ->
            DrawerItem(item = item) {
                navController.navigate(item.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
                onItemSelected()
            }
        }
    }
}

@Composable
fun DrawerItem(item: DrawerMenuItem, onClick: () -> Unit) {
    NavigationDrawerItem(
        icon = { Icon(item.icon, contentDescription = item.title) },
        label = { Text(item.title) },
        selected = false,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
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
            loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            plantList.isEmpty() -> {
                Text(
                    text = "No hay plantas registradas",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
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
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
        Text(text = plant.descripcion, style = MaterialTheme.typography.bodyMedium)
    }
}
