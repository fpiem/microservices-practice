package it.polito.ap.orderservice.controller

import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.UserDTO
import it.polito.ap.common.utils.RoleType
import it.polito.ap.common.utils.StatusType
import it.polito.ap.orderservice.service.OrderService
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/orders")
class OrderController(val orderService: OrderService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

    @PostMapping("")
    fun placeOrder(
        @RequestBody cart: List<CartProductDTO>,
        user: UserDTO,
        shippingAddress: String
    ): ResponseEntity<String> {
        LOGGER.info("Received request to place an order for user ${user.email}")
        val order = orderService.createNewOrder(cart, user)
        LOGGER.info("Order ${order.orderId} placed successfully!")
        return ResponseEntity.ok("Order placed successfully!\nOrder ID: ${order.orderId}")
    }

    @GetMapping("/{orderId}/status")
    fun orderStatus(@PathVariable orderId: ObjectId, user: UserDTO): ResponseEntity<String> {
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
        val order = orderService.getOrderById(orderId)
        if (order.isPresent) { // TODO valutare di cambiare la logica in modo che se un admin modifica il suo ordine lo fa come CUSTOMER
            LOGGER.info("Found order for id $orderId")
            if (RoleType.ROLE_ADMIN == user.role) { // TODO spostare questo controllo sull'admin nel Service
                val modifiedOrder = orderService.modifyOrder(orderId, newStatus)
                return ResponseEntity.ok("Order modified by admin! Order status: ${modifiedOrder.status}")
            } else if (order.get().buyer == user.email) {
                if (StatusType.PAID == order.get().status) {
                    val modifiedOrder = orderService.modifyOrder(orderId, StatusType.CANCELLED)
                    return ResponseEntity.ok("Order modified! Order status: ${modifiedOrder.status}")
                } else {
                    return ResponseEntity("Unauthorized request", HttpStatus.UNAUTHORIZED)
                }
            }
        }
        LOGGER.info("No order found with id $orderId")
        return ResponseEntity.badRequest().body("No order found with the requested Id")

    }

    @GetMapping("/test")
    fun test () : String {
        return "ciao\n"
    }

}
