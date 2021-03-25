package it.polito.ap.orderservice.model

import it.polito.ap.orderservice.model.utils.CartElement
import it.polito.ap.common.utils.StatusType
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
class Order(
    var cart: MutableList<CartElement>? = null,
    var buyer: String?= "", // equals to email
    var deliveryList: List<Delivery>? = null,
    var status: StatusType = StatusType.PAID,
    @Id
    val orderId: ObjectId = ObjectId()
)