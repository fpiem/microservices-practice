package it.polito.ap.common.dto

data class WarehouseProductDTO (
    val productId: String,
    val quantity: Int = -1,
    val alarmThreshold: Int = -1
)