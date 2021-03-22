package it.polito.ap.catalogservice.controller

import it.polito.ap.catalogservice.model.Product
import it.polito.ap.catalogservice.service.ProductService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/products")
class ProductController(val productService: ProductService) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

    @GetMapping("")
    fun getAll(): ResponseEntity<List<Product>> {
        LOGGER.info("received request to retrieve all the product")
        val products = productService.getAll()
        if (products.isEmpty()) {
            LOGGER.info("found no product")
            return ResponseEntity(null, HttpStatus.NOT_FOUND)
        }
        LOGGER.info("found ${products.size} products")
        return ResponseEntity.ok(products)
    }

    @GetMapping("/{productName}")
    fun getProductByName(@PathVariable productName: String): ResponseEntity<Product> {
        LOGGER.info("received request for $productName")
        val product = productService.getProductByName(productName)
        product?.let {
            LOGGER.info("found product $productName")
            return ResponseEntity.ok(product)
        }
        return ResponseEntity.badRequest().body(null)
    }

    @PostMapping("")
    fun addProduct(@RequestBody product: Product): ResponseEntity<String> {
        LOGGER.info("received request to add product ${product.name}")
        return ResponseEntity.ok(productService.addProduct(product))
    }

    @PutMapping("/{productId}")
    fun editProduct(@PathVariable productId: String, @RequestBody newProduct: Product): ResponseEntity<Product> {
        LOGGER.info("received request to modify product with id $productId")
        val product = productService.editProduct(productId, newProduct)
        product?.let {
            LOGGER.info("modification on $productId worked")
            return ResponseEntity.ok(product)
        } ?: kotlin.run {
            LOGGER.info("modification on $productId failed")
            return ResponseEntity.badRequest().body(null)
        }
    }

    @DeleteMapping("/{productId}")
    fun deleteProductById(@PathVariable productId: String): ResponseEntity<String> {
        LOGGER.info("received request to delete the product with id $productId")
        productService.deleteProductById(productId)
        return ResponseEntity.ok("Deletion of product with id $productId completed")
    }

    @GetMapping("/test")
    fun test(): ResponseEntity<String> {
        LOGGER.info("test comunication")
        val restTemplate = RestTemplate()
        val res = restTemplate.getForObject("http://localhost:8082/orders/test", String::class.java)
        return ResponseEntity.ok(res.toString())
    }

    // TODO creare AdminController e inserire API per modificare il warehouse (load/unload product)
}