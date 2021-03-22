package it.polito.ap.catalogservice.service

import it.polito.ap.catalogservice.model.Product
import it.polito.ap.catalogservice.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class ProductService (val productRepository: ProductRepository){
    companion object {
        private val LOGGER
                = LoggerFactory.getLogger(javaClass)
    }

    // check if product already exists, if not product is added
    fun addProduct(product: Product) : String {
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

    fun deleteProductById(productId: String){
        LOGGER.debug("received request to delete product with id $productId")
        productRepository.deleteById(productId)
    }

    fun editProduct(productId: String, newProduct: Product): Product? {
        val product = getProductById(productId)
        if (product.isEmpty){ // if not present it doesn't create a new product. For this use addProduct
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

}
