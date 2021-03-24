package it.polito.ap.orderservice.service

import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.OrderPlacingDTO
import it.polito.ap.common.dto.TransactionDTO
import it.polito.ap.common.dto.UserDTO
import it.polito.ap.common.utils.StatusType
import it.polito.ap.common.utils.TransactionMotivation
import it.polito.ap.orderservice.model.Delivery
import it.polito.ap.orderservice.model.Order
import it.polito.ap.orderservice.model.utils.CartElement
import it.polito.ap.orderservice.repository.OrderRepository
import it.polito.ap.orderservice.service.mapper.OrderMapper
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.util.*

@Service
class OrderService(val orderRepository: OrderRepository, val mapper: OrderMapper) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OrderService::class.java)
    }

    private val restTemplate = RestTemplate()

    // TODO: don't use hardcoded paths / endpoints
    private val walletServiceAddress = "http://localhost:8083/wallets"

    // TODO: currently sync orchestration, check if ok
    fun createNewOrder(orderPlacingDTO: OrderPlacingDTO): Order? {
        LOGGER.debug("Received a request to place an order for user ${orderPlacingDTO.user.email}")
        val cart = orderPlacingDTO.cart.map { mapper.toModel(it) }
        val cartPrice = cart.sumByDouble { it.price * it.quantity }

        // TODO: generate issuerId

        val transactionDTO = TransactionDTO("michelematteini", cartPrice, TransactionMotivation.ORDER_PAYMENT)
        val outcome: ResponseEntity<String> = restTemplate.exchange(
            "$walletServiceAddress/${orderPlacingDTO.user.email}/transactions",
            HttpMethod.PUT,
            HttpEntity<TransactionDTO>(transactionDTO),
            String::class.java
        )
        println(outcome)


        // TODO set back to debug loglevel

        return null
    }

//    fun createNewOrder(cart: List<CartProductDTO>, user: UserDTO): Order {
//        // TODO check disponibilità su warehouse
//        // TODO check sul wallet
//        LOGGER.info("Placing new order for ${user.email}")
//        val order = Order(
//            mutableListOf<CartElement>(), user.email, mutableListOf<Delivery>(), StatusType.PAID
//        )
//        orderRepository.save(order)
//        LOGGER.info("Placed new order ${order.orderId}")
//        return order
//    }

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