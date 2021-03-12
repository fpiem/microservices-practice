package it.polito.ap.warehouseservice.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Warehouse(
    @Id
    val name: String,
    val inventory: MutableMap<String, Int>
)

