package it.polito.ap.orderservice.model.utils

// Flattened mapping of CartProductDTO
data class CartElement (
    val product: String,
    val quantity: Int,
    val price: Double
)
