package com.example.stockmateapp.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PRODUCT_LIST = "products"
    const val PRODUCT_DETAIL = "products/{productId}"
    const val PRODUCT_ADD = "products/add"
    const val PRODUCT_EDIT = "products/{productId}/edit"

    fun productDetail(id: Int) = "products/$id"
    fun productEdit(id: Int) = "products/$id/edit"

    const val WAREHOUSE_LIST = "warehouses"
    const val WAREHOUSE_STOCK = "warehouses/{warehouseId}/stock"
    fun warehouseStock(id: Int) = "warehouses/$id/stock"
}
