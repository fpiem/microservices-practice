package it.polito.ap.orderservice.service

import it.polito.ap.common.dto.*
import it.polito.ap.common.utils.RoleType
import it.polito.ap.common.utils.StatusType
import it.polito.ap.common.utils.TransactionMotivation
import it.polito.ap.orderservice.model.Delivery
import it.polito.ap.orderservice.model.Order
import it.polito.ap.orderservice.model.utils.CartElement
import it.polito.ap.orderservice.repository.DeliveryRepository
import it.polito.ap.orderservice.repository.OrderRepository
import it.polito.ap.orderservice.service.mapper.OrderMapper
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
    val deliveryRepository: DeliveryRepository,
    val orderMapper: OrderMapper,
    val mongoTemplate: MongoTemplate
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OrderService::class.java)
    }

    @Value("\${application.wallet_address}") lateinit private var walletServiceAddress: String
    @Value("\${application.warehouse_address}") lateinit private var warehouseServiceAddress: String

    // TODO: currently sync orchestration, check if ok
    fun createNewOrder(orderPlacingDTO: OrderPlacingDTO): OrderDTO? {
        LOGGER.debug("Received a request to place an order for user ${orderPlacingDTO.user.email}")
        val cart = orderPlacingDTO.cart.map { orderMapper.toModel(it) }
        val cartPrice = cart.sumByDouble { it.price * it.quantity }
        val order = Order()

        // get user info
        val user = orderPlacingDTO.user

        // send info to wallet-service
        val transactionDTO = TransactionDTO(user.email, cartPrice, TransactionMotivation.ORDER_PAYMENT)

        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestEntityWallet = HttpEntity<TransactionDTO>(transactionDTO, headers)

        try {
            val responseEntityWallet: HttpEntity<String> = restTemplate.exchange(
                "$walletServiceAddress/${order.orderId}/transactions",
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
            ) // orderMapper.toCartProductDTO(it)
        }

        val deliveryList: ArrayList<Delivery> = ArrayList()
        val delivery = Delivery()

        val requestEntityWarehouse = HttpEntity<List<CartProductDTO>>(cartProductDTOList, headers)
        try {
            val responseEntityWarehouse: ResponseEntity<List<DeliveryDTO>> = restTemplate.exchange(
                "$warehouseServiceAddress/deliveries?orderId=${order.orderId}",
                HttpMethod.POST,
                requestEntityWarehouse,
                typeRef<List<DeliveryDTO>>() // we need it to return a List
            )
            LOGGER.debug("Communication with warehouse service successful: ${responseEntityWarehouse.body}")

            responseEntityWarehouse.body?.forEach { cartProductDTO ->
                cartProductDTO.deliveryProducts.forEach {
                    delivery.deliveryProducts[it.productDTO.productId] = it.quantity
                }
                deliveryList.add(delivery)
                // fill delivery information
                delivery.shippingAddress = orderPlacingDTO.shippingAddress
                delivery.order = order
                delivery.warehouse = cartProductDTO.warehouseId
                deliveryRepository.save(delivery)
            }
        } catch (e: HttpServerErrorException) {
            LOGGER.debug("Cannot create Delivery List: ${e.message}")
            return null
        } catch (e: HttpClientErrorException) {
            LOGGER.debug("Cannot create Delivery List: ${e.message}")
            return null
        }

        // here all checks are successful
        //fill order information
        order.cart = cart as MutableList<CartElement>
        order.buyer = user.email
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
                .and("buyer").`is`(user.email)
                .and("status").`is`(StatusType.PAID)
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

    private fun changeStatus(order: Order, newStatus: StatusType): Order {
        order.status = newStatus
        orderRepository.save(order)
        return order
    }

}