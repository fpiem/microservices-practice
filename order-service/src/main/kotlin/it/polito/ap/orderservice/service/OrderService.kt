package it.polito.ap.orderservice.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import it.polito.ap.common.dto.*
import it.polito.ap.common.utils.RoleType
import it.polito.ap.common.utils.StatusType
import it.polito.ap.common.utils.TransactionMotivation
import it.polito.ap.orderservice.model.Delivery
import it.polito.ap.orderservice.model.Order
import it.polito.ap.orderservice.model.utils.CartElement
import it.polito.ap.orderservice.repository.OrderRepository
import it.polito.ap.orderservice.service.mapper.OrderMapper
import kotlinx.coroutines.delay
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.*
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import java.util.*
import kotlin.collections.ArrayList

// useful to manage return type such as List in exchange function
inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}

@Service
class OrderService(
    val orderRepository: OrderRepository,
    val orderMapper: OrderMapper,
    val mongoTemplate: MongoTemplate,
    val kafkaTemplate: KafkaTemplate<String, String>,
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OrderService::class.java)
    }

    @Value("\${application.wallet-address}")
    private lateinit var walletServiceAddress: String
    @Value("\${application.warehouse-address}")
    private lateinit var warehouseServiceAddress: String
    @Value("\${application.consistency-check-timeout-ms}")
    private lateinit var consistencyCheckTimeoutMillis: Number

    val jacksonObjectMapper = jacksonObjectMapper()

    @KafkaListener(groupId = "order_service", topics = ["place_order"])
    // TODO: figure out if it is possible to use suspend + delay here instead of Thread.sleep()
    fun ensureOrderConsistency(message: String) {
        val orderId = jacksonObjectMapper.readValue<String>(message)
        LOGGER.debug("Ensuring consistency of order $orderId")
        Thread.sleep(consistencyCheckTimeoutMillis.toLong())
        val order = getOrderByOrderId(orderId)
        if (order == null) {
            LOGGER.debug("Order $orderId missing from the database, rolling back")
            orderRollback(orderId)
        } else {
            LOGGER.debug("Found order $orderId in the database, order successful")
        }
    }

    fun orderRollback(orderId: String) {
        kafkaTemplate.send("rollback", jacksonObjectMapper.writeValueAsString(orderId))
    }

    fun getOrderByOrderId(orderId: String): Order? {
        LOGGER.debug("Attempting to retrieve order $orderId from the database")
        val order = orderRepository.getOrderByOrderId(orderId)
        order?.let {
            LOGGER.debug("Found order $orderId in the database")
        } ?: kotlin.run {
            LOGGER.debug("Could not find order $orderId in the database")
        }
        return order
    }

    // TODO: should this function be "suspend"?
    fun createNewOrder(orderPlacingDTO: OrderPlacingDTO): OrderDTO? {
        LOGGER.debug("Received a request to place an order for user ${orderPlacingDTO.user.userId}")

        val cart = orderPlacingDTO.cart.map { orderMapper.toModel(it) }
        val cartPrice = cart.sumByDouble { it.price * it.quantity }
        val order = Order()

        kafkaTemplate.send("place_order", jacksonObjectMapper.writeValueAsString(order.orderId.toString()))

        val user = orderPlacingDTO.user

        // send info to wallet-service
        val transactionDTO = TransactionDTO(order.orderId.toString(), -cartPrice, TransactionMotivation.ORDER_PAYMENT)

        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestEntityWallet = HttpEntity<TransactionDTO>(transactionDTO, headers)

        // TODO: kafka - inizio processo per order with orderId

        try {
            val responseEntityWallet: HttpEntity<String> = restTemplate.exchange(
                "$walletServiceAddress/${user.userId}/transactions",
                HttpMethod.PUT,
                requestEntityWallet,
                String::class.java
            )
            LOGGER.debug("Communication with wallet service successful: ${responseEntityWallet.body}")
        } catch (e: HttpServerErrorException) {
            LOGGER.debug("Cannot modify wallet: ${e.message}")
            return null
        } catch (e: HttpClientErrorException) {
            LOGGER.debug("Cannot modify wallet: ${e.message}")
            return null
        }

        // send info to warehouse-service
        val cartProductDTOList = ArrayList<CartProductDTO>()
        cart.forEach {
            cartProductDTOList.add(
                CartProductDTO(
                    ProductDTO(it.productId, it.price),
                    it.quantity
                )
            )
        }

        val deliveryList: ArrayList<Delivery> = ArrayList()
        val delivery = Delivery()

        val requestEntityWarehouse = HttpEntity<List<CartProductDTO>>(cartProductDTOList, headers)
        try {
            val responseEntityWarehouse: ResponseEntity<List<DeliveryDTO>> = restTemplate.exchange(
                "$warehouseServiceAddress/${order.orderId}/deliveries",
                HttpMethod.POST,
                requestEntityWarehouse,
                typeRef<List<DeliveryDTO>>() // we need it to return a List
            )
            LOGGER.debug("Communication with warehouse service successful: ${responseEntityWarehouse.body}")

            responseEntityWarehouse.body?.forEach { deliveryDTO ->
                deliveryDTO.deliveryProducts.forEach {
                    delivery.deliveryProducts[it.productDTO.productId] = it.quantity
                }
                deliveryList.add(delivery)
                // fill delivery information
                delivery.shippingAddress = orderPlacingDTO.shippingAddress
                delivery.warehouseId = deliveryDTO.warehouseId
            }
        } catch (e: HttpServerErrorException) {
            LOGGER.debug("Cannot create Delivery List: ${e.message}")
            return null
        } catch (e: HttpClientErrorException) {
            LOGGER.debug("Cannot create Delivery List: ${e.message}")
            return null
        }

        // here all checks are successful
        // fill order information
        order.cart = cart as MutableList<CartElement>
        order.buyer = user.userId
        order.deliveryList = deliveryList

        orderRepository.save(order)
        LOGGER.debug("Placed new order ${order.orderId}")
        // TODO far andare il mapper per Order to OrderDTO
        return OrderDTO(order.orderId.toString(), order.status)
    }

    fun getOrderById(orderId: ObjectId): Optional<Order> {
        LOGGER.debug("Getting order with id: $orderId")
        return orderRepository.findById(orderId.toString())
    }

    // TODO: send kafka message
    // TODO fare un check se è tutto giusto
    fun modifyOrder(orderId: ObjectId, newStatus: StatusType, user: UserDTO): ResponseEntity<String> {
        LOGGER.debug("Receiving request to modify status for order $orderId")
        // admin logic
        if (RoleType.ROLE_ADMIN == user.role) {
            val query = Query().addCriteria(
                Criteria.where("orderId").`is`(orderId)
            )
            val update = Update().set("status", newStatus)
            val updateStatus = mongoTemplate.findAndModify(
                query, update, FindAndModifyOptions().returnNew(true), Order::class.java
            )

            updateStatus?.let {
                LOGGER.debug("Order modified by admin! Order status: ${updateStatus.status}")
                return ResponseEntity.ok("Order modified by admin! Order status: ${updateStatus.status}")
            } ?: kotlin.run {
                LOGGER.debug("Cannot change order status")
                return ResponseEntity("Cannot change order status", HttpStatus.BAD_REQUEST)
            }
        }
        // customer logic
        val query = Query().addCriteria(
            Criteria.where("orderId").`is`(orderId)
                .and("buyer").`is`(user.userId)
                .and("status").`is`(StatusType.ISSUED)
        )
        val update = Update().set("status", StatusType.CANCELLED)
        val updateStatus = mongoTemplate.findAndModify(
            query, update, FindAndModifyOptions().returnNew(true), Order::class.java
        )

        updateStatus?.let {
            LOGGER.debug("Order modified! Order status: ${updateStatus.status}")
            return ResponseEntity.ok("Order modified! Order status: ${updateStatus.status}")
        } ?: kotlin.run {
            LOGGER.debug("Unauthorized request to modify order status")
            return ResponseEntity("Unauthorized request", HttpStatus.UNAUTHORIZED)
        }
    }

}