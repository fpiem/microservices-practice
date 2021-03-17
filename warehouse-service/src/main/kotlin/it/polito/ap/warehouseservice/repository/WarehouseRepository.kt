package it.polito.ap.warehouseservice.repository

import it.polito.ap.warehouseservice.model.Warehouse
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface WarehouseRepository : MongoRepository<Warehouse, String> {
    // TODO: check if String is fine or ObjectId is needed instead
    fun getWarehouseByWarehouseId(warehouseId: String): Warehouse?

    // TODO aggiungete 'match' per controllare che la quantità del prodotto ('inventory.quantity') sia > 0
    @Aggregation(
        "{\$unwind : '\$inventory'}",
        "{'\$match': { 'inventory.productId': ?0}}",
        "{'\$sort':{'inventory.quantity': -1}}",
        "{'\$limit':1}",
        "{'\$project':{_id: 1}}"
    )
    fun getWarehouseByMaxProductQuantity(productId: String): ObjectId?

}