package com.example.stockmateapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stockmateapp.data.repository.AuthRepository
import com.example.stockmateapp.ui.admin.UserManagementScreen
import com.example.stockmateapp.ui.auth.LoginScreen
import com.example.stockmateapp.ui.auth.RegisterScreen
import com.example.stockmateapp.ui.counterparties.CounterpartyListScreen
import com.example.stockmateapp.ui.navigation.Routes
import com.example.stockmateapp.ui.notifications.NotificationScreen
import com.example.stockmateapp.ui.operations.DocumentDetailScreen
import com.example.stockmateapp.ui.operations.DocumentListScreen
import com.example.stockmateapp.ui.products.BatchListScreen
import com.example.stockmateapp.ui.products.ProductDetailScreen
import com.example.stockmateapp.ui.products.ProductFormScreen
import com.example.stockmateapp.ui.products.ProductListScreen
import com.example.stockmateapp.ui.reports.DashboardScreen
import com.example.stockmateapp.ui.requests.RequestListScreen
import com.example.stockmateapp.ui.settings.SettingsScreen
import com.example.stockmateapp.ui.theme.StockMateAppTheme
import com.example.stockmateapp.ui.warehouses.WarehouseListScreen
import com.example.stockmateapp.ui.warehouses.WarehouseStockScreen
import com.example.stockmateapp.ui.warehouses.ZoneListScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startDestination = if (authRepository.isLoggedIn()) Routes.DASHBOARD else Routes.LOGIN
        val role = authRepository.getRole() ?: ""
        setContent {
            StockMateAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavHost(startDestination, role)
                }
            }
        }
    }
}

@Composable
fun AppNavHost(startDestination: String, role: String) {
    val navController = rememberNavController()
    var role by remember { mutableStateOf(role) }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { newRole ->
                    role = newRole
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                role = role,
                onProductsClick = { navController.navigate(Routes.PRODUCT_LIST) },
                onWarehousesClick = { navController.navigate(Routes.WAREHOUSE_LIST) },
                onDocumentsClick = { navController.navigate(Routes.DOCUMENT_LIST) },
                onRequestsClick = { navController.navigate(Routes.REQUESTS) },
                onNotificationsClick = { navController.navigate(Routes.NOTIFICATIONS) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onCounterpartiesClick = { navController.navigate(Routes.COUNTERPARTIES) },
                onUsersClick = { navController.navigate(Routes.USER_MANAGEMENT) }
            )
        }
        composable(Routes.USER_MANAGEMENT) {
            UserManagementScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.COUNTERPARTIES) {
            CounterpartyListScreen(
                onBack = { navController.popBackStack() },
                canEdit = role in listOf("Admin", "Manager")
            )
        }
        composable(Routes.REQUESTS) {
            RequestListScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                }
            )
        }
        composable(Routes.DOCUMENT_LIST) {
            DocumentListScreen(
                onDocumentClick = { navController.navigate(Routes.documentDetail(it)) },
                onAddDocument = { }
            )
        }
        composable(
            Routes.DOCUMENT_DETAIL,
            arguments = listOf(navArgument("documentId") { type = NavType.IntType })
        ) {
            DocumentDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.WAREHOUSE_LIST) {
            WarehouseListScreen(
                onWarehouseClick = { navController.navigate(Routes.warehouseStock(it)) },
                onZonesClick = { navController.navigate(Routes.warehouseZones(it)) }
            )
        }
        composable(
            Routes.WAREHOUSE_STOCK,
            arguments = listOf(navArgument("warehouseId") { type = NavType.IntType })
        ) {
            WarehouseStockScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Routes.WAREHOUSE_ZONES,
            arguments = listOf(navArgument("warehouseId") { type = NavType.IntType })
        ) {
            ZoneListScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                onProductClick = { navController.navigate(Routes.productDetail(it)) },
                onAddProduct = if (role in listOf("Admin", "Manager")) {
                    { navController.navigate(Routes.PRODUCT_ADD) }
                } else null
            )
        }
        composable(
            Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) {
            ProductDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = if (role in listOf("Admin", "Manager")) {
                    { navController.navigate(Routes.productEdit(it)) }
                } else null,
                onBatches = { navController.navigate(Routes.productBatches(it)) }
            )
        }
        composable(Routes.PRODUCT_ADD) {
            ProductFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            Routes.PRODUCT_EDIT,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) {
            ProductFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                isEdit = true
            )
        }
        composable(
            Routes.PRODUCT_BATCHES,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) {
            BatchListScreen(onBack = { navController.popBackStack() })
        }
    }
}
