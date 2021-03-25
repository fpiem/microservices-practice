package it.polito.ap.warehouseservice

import it.polito.ap.warehouseservice.model.Warehouse
import it.polito.ap.warehouseservice.model.WarehouseProduct
import it.polito.ap.warehouseservice.repository.WarehouseRepository
import org.bson.types.ObjectId
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WarehouseServiceApplication(
    warehouseRepository: WarehouseRepository
) {
    init {
        // Example warehouses for testing purposes
        warehouseRepository.deleteAll()

        val warehouse1 = Warehouse(
            ObjectId("111111111111111111111111"), "warehouse1",
            mutableListOf(
                WarehouseProduct("prod1", 0, 5),
                WarehouseProduct("prod2", 10, 3),
                WarehouseProduct("prod3", 3, 4)
            ),
            mutableListOf()
        )
        val warehouse2 = Warehouse(
            ObjectId("222222222222222222222222"), "warehouse2",
            mutableListOf(
                WarehouseProduct("prod3", 3, 5),
                WarehouseProduct("prod4", 7, 3),
                WarehouseProduct("prod5", 3, 4)
            ),
            mutableListOf()
        )
        warehouseRepository.saveAll(listOf(warehouse1, warehouse2))
    }
}

fun main(args: Array<String>) {
    runApplication<WarehouseServiceApplication>(*args)
}
