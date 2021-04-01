package it.polito.ap.orderservice

import it.polito.ap.common.utils.StatusType
import it.polito.ap.orderservice.model.Delivery
import it.polito.ap.orderservice.model.Order
import it.polito.ap.orderservice.model.utils.CartElement
import it.polito.ap.orderservice.repository.OrderRepository
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OrderServiceApplication

fun main(args: Array<String>) {
    var context = runApplication<OrderServiceApplication>(*args)

    var orderRepository = context.getBean<OrderRepository>("orderRepository")
    var order = Order(mutableListOf<CartElement>(), "user.email", mutableListOf<Delivery>(), StatusType.ISSUED)

    print(order.orderId)
    orderRepository.save(order)
}
