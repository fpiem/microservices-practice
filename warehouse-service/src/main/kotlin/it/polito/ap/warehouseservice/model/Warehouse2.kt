package it.polito.ap.warehouseservice.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Warehouse2(
    val name: String?,
    var inventory: MutableList<WarehouseProduct>?,
    var transactionList: MutableList<WarehouseTransaction>?,
    @Id
    val _id: ObjectId?
)