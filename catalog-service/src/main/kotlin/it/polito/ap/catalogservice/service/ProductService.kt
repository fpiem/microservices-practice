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

    fun addProduct(product: Product) {
        LOGGER.info("received request to add product ${product.name}")
        productRepository.save(product)
        LOGGER.info("product ${product.name} added successfully")
    }

    fun getProductByName(productName: String): Product? {
        LOGGER.info("received request to retrieve product $productName")
        var product: Product? = productRepository.findByName(productName)
        product?.let {
            LOGGER.info("found product $productName")
        } ?: kotlin.run {
            LOGGER.info("product $productName not found into the DB")
        }
        return product
    }

    fun getProductById(productId: String): Optional<Product> {
        LOGGER.info("received request to retrieve product with id $productId")
        return productRepository.findById(productId)
    }

    fun getAll(): List<Product> {
        LOGGER.info("received request to retrieve all the product")
        return productRepository.findAll()
    }

    fun deleteProductById(productId: String){
        LOGGER.info("received request to delete product with id $productId")
        productRepository.deleteById(productId)
    }

    fun editProduct(productId: String, newProduct: Product): Product? {
        var product = getProductById(productId)
        if (product.isEmpty){
            if (newProduct.name.isEmpty()) {
                return null
            }
            addProduct(newProduct)
            return getProductByName(newProduct.name)
        }
        if (!"".equals(newProduct.name)){
            product.get().name = newProduct.name
        }
        if (!"".equals(newProduct.category)){
            product.get().category = newProduct.category
        }
        if (!"".equals(newProduct.description)){
            product.get().description = newProduct.description
        }
        if (!"".equals(newProduct.picture)){
            product.get().picture = newProduct.picture
        }
        if (!0.0.equals(newProduct.price)){
            product.get().price = newProduct.price
        }
        productRepository.save(product.get())
        return product.get()
    }

}
