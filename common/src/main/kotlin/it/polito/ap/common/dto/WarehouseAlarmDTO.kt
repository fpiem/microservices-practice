package it.polito.ap.common.dto

data class WarehouseAlarmDTO (
    var productId: String,
    var alarmThreshold: Int = -1
)