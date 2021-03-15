package it.polito.ap.warehouseservice.repository

import it.polito.ap.warehouseservice.model.Warehouse
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WarehouseRepository : MongoRepository<Warehouse, String> {
    // TODO: check if String is fine or ObjectId is needed instead
    fun getWarehouseById(warehouseId: String): Warehouse?
}