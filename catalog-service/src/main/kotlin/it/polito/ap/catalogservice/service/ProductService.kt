package it.polito.ap.catalogservice.service

import it.polito.ap.catalogservice.model.Product
import it.polito.ap.catalogservice.model.User
import it.polito.ap.catalogservice.repository.ProductRepository
import it.polito.ap.catalogservice.service.mapper.UserMapper
import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.OrderDTO
import it.polito.ap.common.dto.OrderPlacingDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod

import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException


@Service
class ProductService(
    val productRepository: ProductRepository,
    val userService: UserService,
    val userMapper: UserMapper
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

    @Value("\${application.order_address}") lateinit private var orderServiceAddress: String

    // check if product already exists, if not product is added
    fun addProduct(product: Product): String {
        LOGGER.debug("received request to add product ${product.name}")
        if (product.name.isBlank()) {
            LOGGER.debug("product name must be well formed (not empty or blank)")
            return "product name must be well formed (not empty or blank)"
        }
        val productCheck: Product? = getProductByName(product.name)
        productCheck?.let {
            LOGGER.debug("product ${product.name} already present into the DB")
            return "product ${product.name} already present into the DB"
        } ?: kotlin.run {
            productRepository.save(product)
            LOGGER.debug("product ${product.name} added successfully")
            return "product ${product.name} added successfully"
        }
    }

    fun getProductByName(productName: String): Product? {
        LOGGER.debug("received request to retrieve product $productName")
        val product: Product? = productRepository.findByName(productName)
        product?.let {
            LOGGER.debug("found product $productName")
        } ?: kotlin.run {
            LOGGER.debug("product $productName not found into the DB")
        }
        return product
    }

    fun getProductById(productId: String): Optional<Product> {
        LOGGER.debug("received request to retrieve product with id $productId")
        return productRepository.findById(productId)
    }

    fun getAll(): List<Product> {
        LOGGER.debug("received request to retrieve all the product")
        return productRepository.findAll()
    }

    fun deleteProductById(productId: String) {
        LOGGER.debug("received request to delete product with id $productId")
        productRepository.deleteById(productId)
    }

    fun editProduct(productId: String, newProduct: Product): Product? {
        val product = getProductById(productId)
        if (product.isEmpty) { // if not present it doesn't create a new product. For this use addProduct
            LOGGER.debug("received request to edit a product that in not present in the DB: $productId")
            return null
        }
        if ("" != newProduct.name)
            product.get().name = newProduct.name
        if ("" != newProduct.category)
            product.get().category = newProduct.category
        if ("" != newProduct.description)
            product.get().description = newProduct.description
        if ("" != newProduct.picture)
            product.get().picture = newProduct.picture
        if (!0.0.equals(newProduct.price))
            product.get().price = newProduct.price
        productRepository.save(product.get())

        return product.get()
    }

    // TODO valutare del refactoring per questa funzione (placeOrder)
    fun placeOrder(cart: List<CartProductDTO>, shippingAddress: String, authentication: Authentication): OrderDTO? {
        if (cart.isEmpty())
            return null

        // get user info
        val authenticationUser = authentication.principal as User
        val user = userService.getUserByEmail(authenticationUser.email)
        if (user == null) { // if user not exists return null
            LOGGER.debug("User not found, can't place order")
            return null
        }
        LOGGER.info("Received request to place an order for user ${user.email}")
        val userDTO = userMapper.toUserDTO(user)

        // update product prices in cart
        cart.forEach {
            val product = getProductById(it.productDTO.productId)
            if (product.isEmpty) {
                LOGGER.debug("received request to place a order but a product is not present in the DB: ${it.productDTO.productId}")
                return null
            }
            it.productDTO.price = product.get().price
        }

        // send info to order-service
        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        // TODO usare un mapper
        val orderPlacingDTO = OrderPlacingDTO(cart, userDTO, shippingAddress)

        val requestEntity = HttpEntity<OrderPlacingDTO>(orderPlacingDTO, headers)
        try {
            val responseEntity: ResponseEntity<OrderDTO> = restTemplate.exchange(
                orderServiceAddress,
                HttpMethod.POST,
                requestEntity,
                OrderDTO::class.java
            )

            val statusCode = responseEntity.statusCode
            if (statusCode == HttpStatus.OK)
                return responseEntity.body
        } catch (e: HttpServerErrorException) {
            LOGGER.debug("Cannot place order: ${e.message}")
            return null
        } catch (e: HttpClientErrorException) {
            LOGGER.debug("Cannot place order: ${e.message}")
            return null
        }

        return null
    }
}
