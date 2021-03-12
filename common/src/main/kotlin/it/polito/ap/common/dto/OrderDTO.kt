package it.polito.ap.common.dto

import it.polito.ap.common.utils.StatusType

data class OrderDTO(
    var orderId: String,
    var status: StatusType
)
/*
import it.polito.ap.ecommerce.utils.CartProduct
import it.polito.ap.ecommerce.utils.StatusType
import org.bson.types.ObjectId

class OrderDTO(
    val id: ObjectId,
    val cart: MutableList<CartProduct>,
    val user: User,
    val deliveryList: List<Delivery>,
    var status: StatusType
)*/