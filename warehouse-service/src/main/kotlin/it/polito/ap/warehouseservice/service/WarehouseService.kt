package it.polito.ap.warehouseservice.service

import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.DeliveryDTO
import it.polito.ap.common.dto.ProductDTO
import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.model.Warehouse
import it.polito.ap.warehouseservice.model.WarehouseProduct
import it.polito.ap.warehouseservice.model.WarehouseTransaction
import it.polito.ap.warehouseservice.model.utils.WarehouseTransactionStatus
import it.polito.ap.warehouseservice.repository.WarehouseRepository
import it.polito.ap.warehouseservice.service.mapper.WarehouseMapper
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.FindAndModifyOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class WarehouseService(
    val warehouseRepository: WarehouseRepository,
    val mapper: WarehouseMapper,
    val mongoTemplate: MongoTemplate
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(WarehouseService::class.java)
    }

    // TODO: add cache
    private fun getWarehouseByWarehouseId(warehouseId: String): Warehouse? {
        LOGGER.debug("Attempting to retrieve warehouse $warehouseId from the database")
        val warehouse = warehouseRepository.getWarehouseByWarehouseId(warehouseId)
        warehouse?.let {
            LOGGER.debug("Found warehouse $warehouseId in the DB")
        } ?: kotlin.run {
            LOGGER.debug("Could not find warehouse $warehouseId in the DB")
        }
        return warehouse
    }

    // TODO: cache
    private fun selectWarehouse(productId: String): String? {
        val warehouseIds = warehouseRepository.getWarehouseByMaxProductQuantity(productId)
        return if (warehouseIds.isEmpty()) {
            LOGGER.debug("No warehouse containing product $productId found")
            null
        } else {
            LOGGER.debug("Warehouse ${warehouseIds[0]} was selected")
            warehouseIds[0]
        }
    }

    // Delivery list will contain only the committed warehouse modifications
    // TODO: implement
    private fun deliveryRollback(deliveryList: MutableList<DeliveryDTO>) {
//        deliveryList.forEach {
//            val warehouse = getWarehouseByWarehouseId(it.warehouseId)
//            it.deliveryProducts.forEach { cartProductDTO ->
//                updateWarehouseProductQuantity(
//                    warehouse!!,
//                    WarehouseProduct(cartProductDTO.productDTO.productId, cartProductDTO.quantity),
//
//                )
//            }
//        }
    }

    private fun persistWarehouse(warehouse: Warehouse) {
        LOGGER.debug("Updating warehouse ${warehouse.warehouseId}")
        warehouseRepository.save(warehouse)
        LOGGER.debug("Updated warehouse ${warehouse.warehouseId}")
    }

    fun warehouseInventory(warehouseId: String): List<WarehouseProductDTO>? {
        LOGGER.debug("Received request for the inventory of warehouse $warehouseId")
        val warehouse = getWarehouseByWarehouseId(warehouseId)
        warehouse?.let {
            LOGGER.debug("Retrieved inventory of warehouse $warehouseId")
            return mapper.toProductDTOList(warehouse.inventory)
        } ?: kotlin.run {
            LOGGER.debug("Could not find warehouse $warehouseId")
            return null
        }
    }

    private fun updateWarehouseProductQuantity(
        warehouse: Warehouse, warehouseProduct: WarehouseProduct, orderId: String?
    ): String {
        LOGGER.debug(
            "Received request to update product ${warehouseProduct.productId} in warehouse ${warehouse.warehouseId}"
        )
        val product = warehouse.inventory.firstOrNull { it.productId == warehouseProduct.productId }
        if (product == null) {
            LOGGER.debug("Could not find product ${warehouseProduct.productId} in warehouse ${warehouse.warehouseId}")
            return "product not found"
        }

        val query = Query().addCriteria(
            Criteria.where("warehouseId").`is`(warehouse.warehouseId)
                .and("inventory").elemMatch(
                    Criteria.where("productId").`is`(warehouseProduct.productId)
                        .and("quantity").gte(-warehouseProduct.quantity)
                )
        )
        val transaction = WarehouseTransaction(
            null,
            mutableMapOf((warehouseProduct.productId to warehouseProduct.quantity)),
            WarehouseTransactionStatus.ADMIN_MODIFICATION
        )
        val update = Update()
            .inc("inventory.$.quantity", warehouseProduct.quantity)
            .push("transactionList", transaction)

        val updatedWarehouse = mongoTemplate.findAndModify(
            query, update, FindAndModifyOptions().returnNew(true), Warehouse::class.java
        )
        updatedWarehouse?.let {
            LOGGER.debug(
                "Updated quantity of product ${warehouseProduct.productId} in warehouse ${warehouse.warehouseId}"
            )
            return "product updated"
        } ?: kotlin.run {
            LOGGER.debug(
                "Insufficient quantity of product ${warehouseProduct.productId} in warehouse ${warehouse.warehouseId}"
            )
            return "insufficient quantity"
        }
    }

    private fun updateWarehouseProductAlarmThreshold(warehouse: Warehouse, warehouseProduct: WarehouseProduct): String {
        LOGGER.debug("Received request to update alarm threshold for product ${warehouseProduct.productId} in warehouse ${warehouse.warehouseId}")
        val product = warehouse.inventory.firstOrNull { it.productId == warehouseProduct.productId }
        if (product == null) {
            LOGGER.debug("Could not find product ${warehouseProduct.productId} in warehouse ${warehouse.warehouseId}")
            return "product not found"
        }

        val query = Query().addCriteria(
            Criteria.where("warehouseId").`is`(warehouse.warehouseId)
                .and("inventory").elemMatch(
                    Criteria.where("productId").`is`(warehouseProduct.productId)
                )
        )
        val update = Update().set("inventory.$.alarmThreshold", warehouseProduct.alarmThreshold)
        val updatedWarehouse = mongoTemplate.findAndModify(
            query, update, FindAndModifyOptions().returnNew(true), Warehouse::class.java
        )
        updatedWarehouse?.let {
            LOGGER.debug("Updated alarm threshold for product ${warehouseProduct.productId} in ${warehouse.warehouseId} to ${warehouseProduct.alarmThreshold}")
            return "alarm updated"
        } ?: kotlin.run {
            LOGGER.debug("Could not update alarm threshold for product ${warehouseProduct.productId} in ${warehouse.warehouseId}")
            return "alarm update failed"
        }
    }

    private fun addWarehouseProduct(warehouseId: String, warehouseProduct: WarehouseProduct): String {
        // Assumes warehouse exists and product is not found
        LOGGER.debug("Adding product ${warehouseProduct.productId} to warehouse $warehouseId")

        // TODO: add criteria that product does not exist
        // In case product was added after the "product already exists" check
        val query = Query().addCriteria(Criteria.where("warehouseId").`is`(warehouseId))

        val transaction = WarehouseTransaction(
            null,
            mutableMapOf((warehouseProduct.productId to warehouseProduct.quantity)),
            WarehouseTransactionStatus.ADMIN_MODIFICATION
        )
        val update = Update().push("inventory", warehouseProduct).push("transactionList", transaction)

        val updatedWarehouse = mongoTemplate.findAndModify(
            query, update, FindAndModifyOptions().returnNew(true), Warehouse::class.java
        )

        updatedWarehouse?.let {
            LOGGER.debug("Added product ${warehouseProduct.productId} to warehouse $warehouseId")
            return "product added"
        } ?: kotlin.run {
            LOGGER.debug("Could not add product ${warehouseProduct.productId} to warehouse $warehouseId")
            return "failed to add transaction"
        }
    }

    fun editProduct(warehouseId: String, warehouseProductDTO: WarehouseProductDTO): String {
        LOGGER.debug("Received request to edit product ${warehouseProductDTO.productId} alarm in $warehouseId")
        val warehouseProduct = mapper.toModel(warehouseProductDTO)
        // Additional read, but it allows us to differentiate between failures owed to warehouse not existing and the
        // product not being present in the inventory
        val warehouse = getWarehouseByWarehouseId(warehouseId)
        warehouse?.let {
            val outcome = updateWarehouseProductQuantity(warehouse, warehouseProduct, null)
            if (outcome == "product not found") {
                return when {
                    warehouseProduct.quantity < 0 -> "negative product quantity"
                    warehouseProduct.alarmThreshold < 0 -> "invalid alarm threshold"
                    else -> addWarehouseProduct(warehouse.warehouseId.toString(), warehouseProduct)
                }
            }
            return outcome
        } ?: kotlin.run {
            return "warehouse not found"
        }
    }

    fun editAlarm(warehouseId: String, warehouseProductDTO: WarehouseProductDTO): String {
        LOGGER.debug("Received request to edit alarm threshold for product ${warehouseProductDTO.productId} in warehouse $warehouseId")
        val warehouseProduct = mapper.toModel(warehouseProductDTO)
        if (warehouseProduct.alarmThreshold < 0) {
            return "invalid alarm threshold"
        }

        val warehouse = getWarehouseByWarehouseId(warehouseId)
        warehouse?.let {
            return updateWarehouseProductAlarmThreshold(warehouse, warehouseProduct)
        } ?: kotlin.run {
            return "warehouse not found"
        }
    }

//    private fun createWarehouseDelivery(warehouse: Warehouse, orderItems: MutableMap<String, Int>): DeliveryDTO {
//        LOGGER.debug("Creating DeliveryDTO for warehouse ${warehouse.warehouseId}")
//        val deliveryProducts = mutableListOf<CartProductDTO>()
//        val relevantInventory = warehouse.inventory.filter { it.productId in orderItems }
//        for (warehouseProduct in relevantInventory) {
//            val productId = warehouseProduct.productId
//            val requestedQuantity = orderItems[productId]!!
//            val warehouseQuantity = warehouseProduct.quantity
//            if (warehouseQuantity > requestedQuantity) {
//                orderItems[productId] = 0
//                deliveryProducts.add(CartProductDTO(ProductDTO(productId), requestedQuantity))
//                updateWarehouseProductQuantity(warehouse, WarehouseProduct(productId, -requestedQuantity))
//            } else {
//                orderItems[productId] = requestedQuantity - warehouseQuantity
//                deliveryProducts.add(CartProductDTO(ProductDTO(productId), warehouseQuantity))
//                updateWarehouseProductQuantity(warehouse, WarehouseProduct(productId, -warehouseQuantity))
//            }
//        }
//        return DeliveryDTO(deliveryProducts, warehouse.warehouseId.toString())
//    }

    private fun productDelivery(
        orderId: String, warehouseId: String, productId: String, quantity: Int
    ): CartProductDTO {

        val query = Query().addCriteria(
            Criteria.where("warehouseId").`is`(warehouseId)
                .and("inventory").elemMatch(
                    Criteria.where("productId").`is`(productId)
                )
        )

        // TODO: change to within update
        val transaction = WarehouseTransaction(
            orderId,
            mutableMapOf((productId to quantity)),
            WarehouseTransactionStatus.CONFIRMED
        )
        val update = Update()
            .inc("inventory.$.quantity", quantity)
            .push("transactionList", )

        val updatedWarehouse = mongoTemplate.findAndModify(
            query, update, FindAndModifyOptions().returnNew(true), Warehouse::class.java
        )

    }

    private fun createWarehouseDelivery(warehouseId: String, orderItems: MutableMap<String, Int>): DeliveryDTO {
        LOGGER.debug("Creating DeliveryDTO for warehouse $warehouseId")
        val deliveryProducts = mutableListOf<CartProductDTO>()
        orderItems.forEach { (productId, quantity) ->

        }
    }

    fun createDeliveryList(cart: List<CartProductDTO>): List<DeliveryDTO>? {
        val deliveryList = mutableListOf<DeliveryDTO>()
        val orderItems = cart.associateBy({ it.productDTO.productId }, { it.quantity }).toMutableMap()

        while (orderItems.values.sum() > 0) {
            val mostRequestedProductId = orderItems.maxByOrNull { it.value }!!.key
            val selectedWarehouseId = selectWarehouse(mostRequestedProductId)
            if (selectedWarehouseId == null) {
                deliveryRollback(deliveryList)
                return null
            }
            deliveryList.add(createWarehouseDelivery(selectedWarehouseId, orderItems))
        }

        return deliveryList
    }

    fun addProduct(warehouseId: String, warehouseProductDTO: WarehouseProductDTO): String {
        val warehouseProduct = mapper.toModel(warehouseProductDTO)
        LOGGER.debug("Received request to add product ${warehouseProduct.productId} to warehouse $warehouseId")
        when {
            warehouseProduct.quantity < 0 -> {
                return "negative product quantity"
            }
            warehouseProduct.alarmThreshold < 0 -> {
                return "negative alarm threshold"
            }
            else -> {
                // Additional read, but it allows us to differentiate between failures owed to warehouse not existing
                // and the product already being present
                val warehouse = getWarehouseByWarehouseId(warehouseId)
                if (warehouse == null) {
                    LOGGER.debug("Could not find warehouse $warehouseId")
                    return "warehouse not found"
                }
                val product = warehouse.inventory.firstOrNull { it.productId == warehouseProduct.productId }
                if (product != null) {
                    LOGGER.debug("Product ${warehouseProduct.productId} already present in warehouse $warehouseId")
                    return "product already present"
                }

                return addWarehouseProduct(warehouseId, warehouseProduct)
            }
        }
    }

}