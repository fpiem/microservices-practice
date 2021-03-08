package it.polito.ap.catalogservice.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.lang.IllegalArgumentException

@Document
class Product(
    @Id
    var name: String = "",
    var description: String = "",
    var picture: String = "",
    var category: String = "",
    var price: Double = 0.0
){
    init {
        if (price <= 0.0) {
            throw IllegalArgumentException()
        }
    }
}