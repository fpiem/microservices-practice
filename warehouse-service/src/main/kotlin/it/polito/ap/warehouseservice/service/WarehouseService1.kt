package it.polito.ap.warehouseservice.service

import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.DeliveryDTO
import it.polito.ap.common.dto.ProductDTO
import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.model.Warehouse
import it.polito.ap.warehouseservice.model.WarehouseProduct
import it.polito.ap.warehouseservice.repository.WarehouseRepository
import it.polito.ap.warehouseservice.service.mapper.WarehouseMapper
import org.slf4j.LoggerFactory

class WarehouseService1(val warehouseRepository: WarehouseRepository, val mapper: WarehouseMapper) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

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

    private fun selectWarehouse(productId: String): Warehouse? {
        val warehouseIds = warehouseRepository.getWarehouseByMaxProductQuantity(productId)
        return if (warehouseIds.isEmpty()) {
            LOGGER.debug("No warehouse containing product $productId found")
            null
        } else {
            LOGGER.debug("Warehouse ${warehouseIds[0]} was selected")
            getWarehouseByWarehouseId(warehouseIds[0])
        }
    }

    // Delivery list will contain only the committed warehouse modifications
    private fun deliveryRollback(deliveryList: MutableList<DeliveryDTO>) {
        deliveryList.forEach {
            val warehouse = getWarehouseByWarehouseId(it.warehouseId)
            it.deliveryProducts.forEach { cartProductDTO ->
                updateWarehouseProductQuantity(
                    warehouse!!,
                    WarehouseProduct(cartProductDTO.productDTO.productId, cartProductDTO.quantity)
                )
            }
        }
    }

    private fun persistWarehouse(warehouse: Warehouse) {
        LOGGER.debug("Updating warehouse ${warehouse.warehouseId}")
        warehouseRepository.save(warehouse)
        LOGGER.debug("Updated warehouse ${warehouse.warehouseId}")
    }

    private fun updateWarehouseProductQuantity(warehouse: Warehouse, warehouseProduct: WarehouseProduct): String {
        LOGGER.debug(
            "Received request to update product ${warehouseProduct.productId} in warehouse ${warehouse.warehouseId}"
        )
        val index = warehouse.inventory.indexOfFirst {it.productId == warehouseProduct.productId}
        if (index == -1) {
            return "product not found"
        }
        warehouse.inventory[index].quantity += warehouseProduct.quantity
        return if (warehouse.inventory[index].quantity < 0) {
            LOGGER.debug("Not updating warehouse ${warehouse.warehouseId} - insufficient quantity of product ${warehouseProduct.productId}")
            "insufficient product quantity"
        } else {
            persistWarehouse(warehouse)
            "product updated"
        }
    }

    private fun addNewProduct(warehouse: Warehouse, warehouseProduct: WarehouseProduct) {
        LOGGER.debug("Adding product ${warehouseProduct.productId} to warehouse $warehouse")
        warehouse.inventory.add(warehouseProduct)
        persistWarehouse(warehouse)
        LOGGER.debug("Added product ${warehouseProduct.productId} to warehouse $warehouse")
    }

    fun editProduct(warehouseId: String, warehouseProductDTO: WarehouseProductDTO): String {
        LOGGER.info("Received request to edit product ${warehouseProductDTO.productId} alarm in $warehouseId")
        val warehouse = getWarehouseByWarehouseId(warehouseId)
        warehouse?.let {
            val outcome = updateWarehouseProductQuantity(warehouse, mapper.toModel(warehouseProductDTO))
            if (outcome == "product not found") {
                if (warehouseProductDTO.alarmThreshold > 0) {
                    addNewProduct(warehouse, mapper.toModel(warehouseProductDTO))
                    return "product added"
                }
                return "alarm threshold not set"
            }
            return outcome
        } ?: kotlin.run {
            return "warehouse not found"
        }
    }

    private fun createWarehouseDelivery(warehouse: Warehouse, orderItems: MutableMap<String, Int>): DeliveryDTO {
        LOGGER.debug("Creating DeliveryDTO for warehouse ${warehouse.warehouseId}")
        val deliveryProducts = mutableListOf<CartProductDTO>()
        val relevantInventory = warehouse.inventory.filter { it.productId in orderItems }
        for (warehouseProduct in relevantInventory) {
            val productId = warehouseProduct.productId
            val requestedQuantity = orderItems[productId]!!
            val warehouseQuantity = warehouseProduct.quantity
            if (warehouseQuantity > requestedQuantity) {
                orderItems[productId] = 0
                deliveryProducts.add(CartProductDTO(ProductDTO(productId), requestedQuantity))
                updateWarehouseProductQuantity(warehouse, WarehouseProduct(productId, -requestedQuantity))
            } else {
                orderItems[productId] = requestedQuantity - warehouseQuantity
                deliveryProducts.add(CartProductDTO(ProductDTO(productId), warehouseQuantity))
                updateWarehouseProductQuantity(warehouse, WarehouseProduct(productId, -warehouseQuantity))
            }
        }
        return DeliveryDTO(deliveryProducts, warehouse.warehouseId.toString())
    }

    fun createDeliveryList(cart: List<CartProductDTO>): List<DeliveryDTO>? {
        val deliveryList = mutableListOf<DeliveryDTO>()
        val orderItems = cart.associateBy({it.productDTO.productId}, {it.quantity}).toMutableMap()

        while (orderItems.values.sum() > 0) {
            val mostRequestedProductId = orderItems.maxByOrNull { it.value }!!.key
            val selectedWarehouse = selectWarehouse(mostRequestedProductId)
            if (selectedWarehouse == null) {
                deliveryRollback(deliveryList)
                return null
            }
            deliveryList.add(createWarehouseDelivery(selectedWarehouse, orderItems))
        }

        return deliveryList
    }

    // TODO: edit Alarm

}