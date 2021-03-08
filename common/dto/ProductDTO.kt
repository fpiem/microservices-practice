package it.polito.ap.ecommerce

import java.lang.IllegalArgumentException

class ProductDTO(
    var name: String = "",
    var description: String = "",
    var picture: String = "",
    var category: String = "", // TODO aggiungere CategoryType
    var price: Double = 0.0
){
    init {
        if (price <= 0.0) {
            throw IllegalArgumentException()
        }
    }
}