package it.polito.ap.warehouseservice.service

import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.DeliveryDTO
import it.polito.ap.common.dto.WarehouseAlarmDTO
import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.controller.WarehouseController
import it.polito.ap.warehouseservice.model.Warehouse
import it.polito.ap.warehouseservice.model.WarehouseProduct
import it.polito.ap.warehouseservice.repository.WarehouseRepository
import it.polito.ap.warehouseservice.service.mapper.WarehouseMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WarehouseService2 (val warehouseRepository: WarehouseRepository, val mapper: WarehouseMapper) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

    fun getWarehouseByWarehouseId(warehouseId: String): Warehouse? {
        return warehouseRepository.getWarehouseByWarehouseId(warehouseId)
    }

    fun saveWarehouse(warehouse: Warehouse) {
        warehouseRepository.save(warehouse)
    }

    fun findWarehouseProduct(warehouse: Warehouse, productId: String): Pair<Int, WarehouseProduct>? {
        val index = warehouse.inventory.indexOfFirst { it.productId == productId }
        return if (index == -1) {
            null
        } else {
            Pair(index, warehouse.inventory[index])
        }
    }

//    fun findWarehouseProduct1(warehouse: Warehouse, productId: String): Pair<Int?, WarehouseProduct?> {
//        val index = warehouse.inventory.indexOfFirst { it.productId == productId }
//        return if (index == -1) {
//            Pair(null, null)
//        } else {
//            Pair(index, warehouse.inventory[index])
//        }
//    }

    fun warehouseInventory(warehouseId: String): List<WarehouseProductDTO>? {
        LOGGER.debug("Received request for the inventory of warehouse $warehouseId")
        val warehouse = warehouseRepository.getWarehouseByWarehouseId(warehouseId)
        warehouse?.let {
            LOGGER.debug("Retrieved inventory of warehouse $warehouseId")
            return mapper.toProductDTOList(warehouse.inventory)
        }?:kotlin.run {
            LOGGER.debug("Could not find warehouse $warehouseId")
            return null
        }
    }

    fun editProduct(warehouseId: String, warehouseProductDTO: WarehouseProductDTO): String {
        LOGGER.info("Received request to edit product ${warehouseProductDTO.productId} alarm in $warehouseId")
        val warehouse = warehouseRepository.getWarehouseByWarehouseId(warehouseId)
        if (warehouse == null) {
            LOGGER.debug("Could not find warehouse $warehouseId")
            return "warehouse not found"
        } else {
            val newWarehouseProduct = mapper.toModel(warehouseProductDTO)
            val (index, oldWarehouseProduct) = findWarehouseProduct(
                warehouse, newWarehouseProduct.productId
            ) ?: Pair(null, null)

            oldWarehouseProduct?.let {
                newWarehouseProduct.alarmThreshold = it.alarmThreshold
                newWarehouseProduct.quantity += oldWarehouseProduct.quantity
                if (newWarehouseProduct.quantity < 0) {
                    LOGGER.debug("Negative quantity for product ${newWarehouseProduct.productId}")
                    return "insufficient product quantity"
                }
                warehouse.inventory[index!!] = newWarehouseProduct
                warehouseRepository.save(warehouse)
                LOGGER.debug("Updated product ${newWarehouseProduct.productId} in warehouse $warehouse")
                return "product updated"
            } ?: kotlin.run {
                LOGGER.debug(
                    "Could not find product ${newWarehouseProduct.productId} in warehouse $warehouse, adding it"
                )
                warehouse.inventory.add(newWarehouseProduct)
                warehouseRepository.save(warehouse)
                LOGGER.debug("Added product ${newWarehouseProduct.productId} to warehouse $warehouse")
                return "product added"
            }
        }
    }

//    fun editAlarm1(warehouseId: String, warehouseAlarmDTO: WarehouseAlarmDTO): String {
//        LOGGER.debug("Received request tço edit ${warehouseAlarmDTO.productId} alarm in $warehouseId")
//        val warehouse = getWarehouseByWarehouseId(warehouseId)
//        warehouse?.let {
//            LOGGER.debug("Could not find warehouse $warehouseId")
//            return "could not find warehouse"
//        } ?: kotlin.run {
//            return updateWarehouseProductAlarm(warehouse, warehouseAlarmDTO)
//        }
//    }

//    fun updateWarehouseProductAlarm(warehouse: Warehouse, warehouseAlarmDTO: WarehouseAlarmDTO) {
//        val newWarehouseProduct = mapper.toModel(warehouseAlarmDTO)
//        val (index, oldWarehouseProduct) = findWarehouseProduct1(warehouse, newWarehouseProduct.productId)
//        oldWarehouseProduct?.let {
//            newWarehouseProduct.quantity = it.quantity
//            warehouse.inventory[index!!] = newWarehouseProduct
//            saveWarehouse()
//        }
//    }

    fun editAlarm(warehouseId: String, warehouseAlarmDTO: WarehouseAlarmDTO): String {
        LOGGER.debug("Received request to edit ${warehouseAlarmDTO.productId} alarm in $warehouseId")
        val warehouse = warehouseRepository.getWarehouseByWarehouseId(warehouseId)
        if (warehouse == null) {
            LOGGER.debug("Could not find warehouse $warehouseId")
            return "could not find warehouse"
        } else {
            val newWarehouseProduct = mapper.toModel(warehouseAlarmDTO)
            val (index, oldWarehouseProduct) = findWarehouseProduct(
                warehouse, newWarehouseProduct.productId
            ) ?: Pair(null, null)

            oldWarehouseProduct?.let {
                newWarehouseProduct.quantity = it.quantity
                warehouse.inventory[index!!] = newWarehouseProduct
                warehouseRepository.save(warehouse)
                LOGGER.debug(
                    "Updated alarm threshold for product ${newWarehouseProduct.productId} in warehouse $warehouseId"
                )
                return "success"
            } ?: kotlin.run {
                LOGGER.debug("Could not find product ${newWarehouseProduct.productId} in warehouse $warehouseId")
                // If product is missing, it is **not** created in this case
                return "could not find product"
            }
        }
    }

//    fun selectWarehouse(orderItems: Map<String, Int>): Warehouse {
//        // Warehouse selection logic should be such that the same warehouse is never selected twice
//        // If selectWarehouse() is called more than once, order could not be completed in the previous call
//        val highestPriorityProductId = orderItems.maxByOrNull { it.value }!!.key
//
//    }

    // TODO: figure this shit out
    fun updateWarehouseProductQuantity(productId: String, quantity: Int) {}

    fun createWarehouseDeliveryList(warehouseId: String, orderItems: MutableMap<String, Int>) {
        // TODO: probably pass the whole warehouse instead of the ID
        // TODO: this still does not create a delivery!
        val warehouse = getWarehouseByWarehouseId(warehouseId)!!
        for ((requestedProductId, requestedQuantity) in orderItems) {
            val currentWarehouseProduct = warehouse.inventory.first { it.productId == requestedProductId }
            // TODO: account for product not being the warehouse
            val currentQuantity = currentWarehouseProduct.quantity
            if (currentQuantity >= requestedQuantity) {
                // TODO: this function doesn't make sense like this since we already have the warehouse
                updateWarehouseProductQuantity(requestedProductId, -requestedQuantity)
                orderItems.replace(requestedProductId, 0)
            } else {
                updateWarehouseProductQuantity(requestedProductId, -currentQuantity)
                orderItems.replace(requestedProductId, requestedQuantity - currentQuantity)
            }
        }
    }

    // TODO: implement
    fun deliveryList(cart: List<CartProductDTO>): List<DeliveryDTO>? {

        val orderItems: Map<String, Int> = cart
            .associateBy({it.productDTO.productId}, {it.quantity})
            .toMutableMap()

        // TODO: replace with actual logic
        // Assume this is the output of selectWarehouse
        val warehouses = warehouseRepository.findAll()
        val warehouseIds = warehouses.map { it.warehouseId }

        return null
    }

    fun test() {
        val result = warehouseRepository.getWarehouseByMaxProductQuantity("prod3")
        println(result)
    }

}