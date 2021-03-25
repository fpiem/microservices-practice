package it.polito.ap.warehouseservice.model

import it.polito.ap.warehouseservice.model.utils.WarehouseTransactionStatus

class WarehouseTransaction(
    var orderId: String?,
    var products: MutableMap<String, Int>,
    var status: WarehouseTransactionStatus
)