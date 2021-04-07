package it.polito.ap.catalogservice

import it.polito.ap.catalogservice.model.Product
import it.polito.ap.catalogservice.repository.ProductRepository
import it.polito.ap.common.utils.CategoryType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CatalogServiceApplication(
    productRepository: ProductRepository
) {
    init {
        productRepository.deleteAll()

        val product1 = Product(
            "prod1",
            "The first product",
            "prod1 picture",
            CategoryType.BOOK,
            10.0,
            5
        )
        val product2 = Product(
            "prod2",
            "The second product",
            "prod2 picture",
            CategoryType.TECH,
            100.0,
            10
        )
        val product5 = Product(
            "prod5",
            "The fifth product",
            "prod5 picture",
            CategoryType.FOODANDDRINK,
            5.0,
            20
        )
        val product42 = Product(
            "prod42",
            "The forty-second product",
            "prod42 picture",
            CategoryType.JEWELRY,
            500.0,
            2
        )
        productRepository.saveAll(listOf(product1, product2, product5, product42))
    }
}

fun main(args: Array<String>) {
    runApplication<CatalogServiceApplication>(*args)
}
