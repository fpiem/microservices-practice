package it.polito.ap.orderservice.model

import it.polito.ap.orderservice.model.Order
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document


@Document
class Delivery (
    var shippingAddress: String,
    var deliveryProducts: MutableMap<String, Int>,
    var order: Order,
    var warehouse: String, // equals to warehouse name
    @Id
    val deliveryId: ObjectId = ObjectId()
){

}