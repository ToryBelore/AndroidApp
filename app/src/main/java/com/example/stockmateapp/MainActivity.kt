package com.example.stockmateapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.stockmateapp.data.repository.AuthRepository
import com.example.stockmateapp.ui.auth.LoginScreen
import com.example.stockmateapp.ui.navigation.Routes
import com.example.stockmateapp.ui.products.ProductDetailScreen
import com.example.stockmateapp.ui.products.ProductFormScreen
import com.example.stockmateapp.ui.products.ProductListScreen
import com.example.stockmateapp.ui.operations.DocumentDetailScreen
import com.example.stockmateapp.ui.operations.DocumentListScreen
import com.example.stockmateapp.ui.warehouses.WarehouseListScreen
import com.example.stockmateapp.ui.warehouses.WarehouseStockScreen
import com.example.stockmateapp.ui.theme.StockMateAppTheme
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
        setContent {
            StockMateAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(startDestination)
                }
            }
        }
    }
}

@Composable
fun AppNavHost(startDestination: String) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardPlaceholder(
                onProductsClick = { navController.navigate(Routes.PRODUCT_LIST) },
                onWarehousesClick = { navController.navigate(Routes.WAREHOUSE_LIST) },
                onDocumentsClick = { navController.navigate(Routes.DOCUMENT_LIST) }
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
                onWarehouseClick = { navController.navigate(Routes.warehouseStock(it)) }
            )
        }
        composable(
            Routes.WAREHOUSE_STOCK,
            arguments = listOf(navArgument("warehouseId") { type = NavType.IntType })
        ) {
            WarehouseStockScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PRODUCT_LIST) {
            ProductListScreen(
                onProductClick = { navController.navigate(Routes.productDetail(it)) },
                onAddProduct = { navController.navigate(Routes.PRODUCT_ADD) }
            )
        }
        composable(
            Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) {
            ProductDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.productEdit(it)) }
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
    }
}

@Composable
fun DashboardPlaceholder(
    onProductsClick: () -> Unit = {},
    onWarehousesClick: () -> Unit = {},
    onDocumentsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("StockMate", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onProductsClick, modifier = Modifier.fillMaxWidth()) { Text("Товары") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onWarehousesClick, modifier = Modifier.fillMaxWidth()) { Text("Склады") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onDocumentsClick, modifier = Modifier.fillMaxWidth()) { Text("Операции") }
    }
}
