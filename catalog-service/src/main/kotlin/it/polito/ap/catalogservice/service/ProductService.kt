package it.polito.ap.catalogservice.service

import it.polito.ap.catalogservice.model.Product
import it.polito.ap.catalogservice.model.User
import it.polito.ap.catalogservice.repository.ProductRepository
import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.OrderDTO
import it.polito.ap.common.dto.OrderPlacingDTO
import it.polito.ap.common.dto.UserDTO
import it.polito.ap.common.utils.StatusType
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import java.util.*


@Service
class ProductService(
    val productRepository: ProductRepository,
    val userService: UserService,
    //val userMapper: UserMapper
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

    private val restTemplate = RestTemplate()
    private val headers = HttpHeaders()

    @Value("\${application.order-address}")
    private lateinit var orderServiceAddress: String

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
        val userDTO = createUserDTOFromLoggedUser(authentication) ?: return null // if user not exists return null
        LOGGER.info("Received request to place an order for user ${userDTO.userId}")

        // update product prices in cart
        cart.forEach {
            // check if quantity is > 0, else return null
            if (it.quantity < 1)
                return null
            // update prices in cart
            val product = getProductById(it.productDTO.productId)
            if (product.isEmpty) {
                LOGGER.debug("received request to place a order but a product is not present in the DB: ${it.productDTO.productId}")
                return null
            }
            it.productDTO.price = product.get().price
        }

        // send info to order-service
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

    // TODO verificare che i controlli vadano fatti solo nell'order-service oppure anche qui
    fun modifyOrder(orderId: ObjectId, newStatus: StatusType, authentication: Authentication): ResponseEntity<String> {
        // get user info
        val userDTO = createUserDTOFromLoggedUser(authentication)
        if (userDTO == null) {
            val statusString = "Cannot find user"
            LOGGER.debug(statusString)
            return ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
        }

        headers.contentType = MediaType.APPLICATION_JSON
        val requestEntity = HttpEntity<UserDTO>(userDTO, headers)
        return try {
            restTemplate.exchange(
                "$orderServiceAddress/$orderId?newStatus=$newStatus",
                HttpMethod.PUT,
                requestEntity,
                String::class.java
            )
        } catch (e: HttpServerErrorException) {
            val statusString = "Cannot change order status: ${e.message}"
            LOGGER.debug(statusString)
            ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
        } catch (e: HttpClientErrorException) {
            val statusString = "Cannot change order status: ${e.message}"
            LOGGER.debug(statusString)
            ResponseEntity(statusString, HttpStatus.BAD_REQUEST)
        }
    }

    private fun createUserDTOFromLoggedUser(authentication: Authentication): UserDTO? {
        val authenticationUser = authentication.principal as User
        val user = userService.getUserByEmail(authenticationUser.email)
        if (user == null) { // if user not exists return null
            LOGGER.debug("User not found")
            return null
        }
        return UserDTO(user.userId.toString(), user.role)
    }
}
