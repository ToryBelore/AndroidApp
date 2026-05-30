package com.example.stockmateapp.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val PRODUCT_LIST = "products"
    const val PRODUCT_DETAIL = "products/{productId}"
    const val PRODUCT_ADD = "products/add"
    const val PRODUCT_EDIT = "products/{productId}/edit"
    const val PRODUCT_BATCHES = "products/{productId}/batches"

    fun productDetail(id: Int) = "products/$id"
    fun productEdit(id: Int) = "products/$id/edit"
    fun productBatches(id: Int) = "products/$id/batches"

    const val WAREHOUSE_LIST = "warehouses"
    const val WAREHOUSE_STOCK = "warehouses/{warehouseId}/stock"
    const val WAREHOUSE_ZONES = "warehouses/{warehouseId}/zones"
    fun warehouseStock(id: Int) = "warehouses/$id/stock"
    fun warehouseZones(id: Int) = "warehouses/$id/zones"

    const val DOCUMENT_LIST = "documents"
    const val DOCUMENT_DETAIL = "documents/{documentId}"
    fun documentDetail(id: Int) = "documents/$id"

    const val REQUESTS = "requests"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
    const val COUNTERPARTIES = "counterparties"
    const val USER_MANAGEMENT = "admin/users"
}
