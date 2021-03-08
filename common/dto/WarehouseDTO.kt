package it.polito.ap.ecommerce

class WarehouseDTO(
    val name: String,
    val inventory: MutableMap<String, Int>
)

