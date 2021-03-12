package it.polito.ap.orderservice.service

import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.UserDTO
import it.polito.ap.common.utils.StatusType
import it.polito.ap.orderservice.model.Delivery
import it.polito.ap.orderservice.model.Order
import it.polito.ap.orderservice.model.utils.CartElement
import it.polito.ap.orderservice.repository.OrderRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class OrderService(val orderRepository: OrderRepository) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

    fun createNewOrder(cart: List<CartProductDTO>, user: UserDTO): Order {
        // TODO check disponibilità su warehouse
        // TODO check sul wallet
        LOGGER.info("Placing new order for ${user.email}")
        val order = Order(
            mutableListOf<CartElement>(), user.email, mutableListOf<Delivery>(), StatusType.PAID
        )
        orderRepository.save(order)
        LOGGER.info("Placed new order ${order.orderId}")
        return order
    }

    fun getOrderById(orderId: ObjectId): Optional<Order> {
        LOGGER.info("Getting order with id: $orderId")
        return orderRepository.findById(orderId.toString())
    }

    fun modifyOrder(orderId: ObjectId, newStatus: StatusType): Order {
        val order = getOrderById(orderId).get()
        order.status = newStatus
        orderRepository.save(order)
        return order
    }

}