package it.polito.ap.catalogservice

import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.OrderDTO
import it.polito.ap.common.dto.ProductDTO
import org.springframework.http.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate

class OrderStatusRoutine

fun main(args: Array<String>) {
    // place order and take orderDTO
    var order: OrderDTO? = null
    var restTemplate = RestTemplate()
    val headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_JSON
    headers.setBasicAuth("m.michelini@yopmail.com", "\$2a\$10\$PaRVbn4bQHrMcep52jcv2Oky0WbEBllVE2irKNzUgoGQrrF376Bli") // change password

    // create cart
    val cart = ArrayList<CartProductDTO>()
    cart.add(CartProductDTO(ProductDTO("prod1"), 2))
    cart.add(CartProductDTO(ProductDTO("prod2"), 3))

    val requestEntity = HttpEntity<List<CartProductDTO>>(cart, headers)
    try {
        val responseEntity: ResponseEntity<OrderDTO> = restTemplate.exchange(
            "http://localhost:8181/products/placeOrder?shippingAddress=Test_Address",
            HttpMethod.POST,
            requestEntity,
            OrderDTO::class.java
        )

        val statusCode = responseEntity.statusCode
        if (statusCode == HttpStatus.OK) {
            order = responseEntity.body
            if (order != null) {
                print("Order ${order.orderId} placed, status: ${order.status}")
            }
        }
    } catch (e: HttpServerErrorException) {
        print("Error in catalog-service when place order: ${e.message}")
    } catch (e: HttpClientErrorException) {
        print("Error in catalog-service when place order: ${e.message}")
    }
    // timer
    Thread.sleep(10000)
    // change status to DELIVERING
    restTemplate = RestTemplate()
    val headers2 = HttpHeaders()
    headers2.contentType = MediaType.APPLICATION_JSON
    headers2.setBasicAuth("m.rossini@yopmail.com", "\$2a\$10\$6RbR9dLQkS2FZC9VczMgq.o3q6.zJ8IWNUan/1zk760w./d1i6.uy")

    val requestEntity2 = HttpEntity<String>("", headers2)
    try {
        val responseEntity: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:8181/products/order/${order?.orderId}?newStatus=DELIVERING",
            HttpMethod.PUT,
            requestEntity2,
            String::class.java
        )

        val statusCode = responseEntity.statusCode
        if (statusCode == HttpStatus.OK)
            print(responseEntity.body)
    } catch (e: HttpServerErrorException) {
        print("Error in catalog-service when change status to DELIVERING: ${e.message}")
    } catch (e: HttpClientErrorException) {
        print("Error in catalog-service when change status to DELIVERING: ${e.message}")
    }
    // timer
    Thread.sleep(10000)
    // change status to DELIVERED
    try {
        val responseEntity: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:8181/products/order/${order?.orderId}?newStatus=DELIVERED",
            HttpMethod.PUT,
            requestEntity2,
            String::class.java
        )

        val statusCode = responseEntity.statusCode
        if (statusCode == HttpStatus.OK)
            print(responseEntity.body)
    } catch (e: HttpServerErrorException) {
        print("Error in catalog-service when change status to DELIVERED: ${e.message}")
    } catch (e: HttpClientErrorException) {
        print("Error in catalog-service when change status to DELIVERED: ${e.message}")
    }
}