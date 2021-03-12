package it.polito.ap.orderservice.model

import it.polito.ap.orderservice.model.utils.CartElement
import it.polito.ap.common.utils.StatusType
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Order(
    val cart: MutableList<CartElement>,
    val buyer: String, // equals to email
    val deliveryList: List<Delivery>,
    var status: StatusType,
    @Id
    val orderId: ObjectId = ObjectId()
)