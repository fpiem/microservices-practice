package it.polito.ap.orderservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import it.polito.ap.common.dto.OrderDTO
import it.polito.ap.common.dto.OrderPlacingDTO
import it.polito.ap.common.dto.UserDTO
import it.polito.ap.common.utils.StatusType
import it.polito.ap.orderservice.service.OrderService
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/orders")
class OrderController(val orderService: OrderService, val kafkaTemplate: KafkaTemplate<String, String>
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(OrderController::class.java)
    }

    @PostMapping("")
    fun placeOrder(@RequestBody orderPlacingDTO: OrderPlacingDTO): ResponseEntity<OrderDTO?> {
        LOGGER.info("Received a request to place an order for user ${orderPlacingDTO.user.email}")
        val order = orderService.createNewOrder(orderPlacingDTO)
        order?.let {
            LOGGER.info("Order ${order.orderId} placed successfully!")
            return ResponseEntity.ok(order)
        } ?: kotlin.run {
            LOGGER.info("Cannot place order")
            return ResponseEntity.badRequest().body(null)
        }
    }

    @GetMapping("/{orderId}/status")
    fun orderStatus(@PathVariable orderId: ObjectId, @RequestBody user: UserDTO): ResponseEntity<String> {
        LOGGER.info("Received request for the status of $orderId by ${user.email} with role: ${user.role}!")
        val order = orderService.getOrderById(orderId)
        if (order.isPresent) {
            LOGGER.info("Found order for id $orderId")
            return ResponseEntity.ok("Order status: ${order.get().status}")
        }
        LOGGER.info("No order found with id $orderId")
        return ResponseEntity.badRequest().body("No order found with the requested Id")
    }

    @PatchMapping("/{orderId}")
    fun changeStatus(
        @PathVariable orderId: ObjectId,
        @RequestParam newStatus: StatusType,
        @RequestBody user: UserDTO
    ): ResponseEntity<String> {
        LOGGER.info("Received request to change the status of $orderId to $newStatus from ${user.email} with role: ${user.role}!")
        return orderService.modifyOrder(orderId, newStatus, user)
    }


    // TODO: remove test code
    val mapper = jacksonObjectMapper()

    @GetMapping("/test")
    fun test() {
        kafkaTemplate.send("hello_topic", "ciao")
    }

    @KafkaListener(groupId = "test", topics = ["hello_topic"])
    fun listenerTest(message: String) {
        Thread.sleep(1000)
        println("listener1 - $message")
    }

    @KafkaListener(groupId = "test3", topics = ["hello_topic"])
    fun listenerTest2(message: String) {
        println("listener2 - $message")
    }

}
