package it.polito.ap.warehouseservice.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class WarehouseUnwind(
    @Id
    val warehouseId: ObjectId,
    val name: String,
    var inventory: WarehouseProduct,
    var transactionList: MutableList<WarehouseTransaction>
)