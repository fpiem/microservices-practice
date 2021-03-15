package it.polito.ap.warehouseservice.service

import it.polito.ap.common.dto.WarehouseAlarmDTO
import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.model.Warehouse
import it.polito.ap.warehouseservice.model.WarehouseProduct
import it.polito.ap.warehouseservice.repository.WarehouseRepository
import it.polito.ap.warehouseservice.service.mapper.WarehouseMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WarehouseService (val warehouseRepository: WarehouseRepository, val mapper: WarehouseMapper) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

    fun findWarehouseProduct(warehouse: Warehouse, productId: String): Pair<Int, WarehouseProduct>? {
        val index = warehouse.inventory.indexOfFirst { it.productId == productId }
        return if (index == -1) {
            null
        } else {
            Pair(index, warehouse.inventory[index])
        }
    }

    fun editProduct(warehouseId: String, warehouseProductDTO: WarehouseProductDTO): String {
        LOGGER.info("Received request to edit product ${warehouseProductDTO.productId} alarm in $warehouseId")
        val warehouse = warehouseRepository.getWarehouseByWarehouseId(warehouseId)
        if (warehouse == null) {
            LOGGER.info("Could not find warehouse $warehouseId")
            return "warehouse not found"
        } else {
            val newWarehouseProduct = mapper.toModel(warehouseProductDTO)
            val (index, oldWarehouseProduct) = findWarehouseProduct(
                warehouse, newWarehouseProduct.productId
            ) ?: Pair(null, null)

            oldWarehouseProduct?.let {
                newWarehouseProduct.alarmThreshold = it.alarmThreshold
                warehouse.inventory[index!!] = newWarehouseProduct
                warehouseRepository.save(warehouse)
                LOGGER.info("Updated product ${newWarehouseProduct.productId} in warehouse $warehouse")
                return "product updated"
            } ?: kotlin.run {
                LOGGER.info(
                    "Could not find product ${newWarehouseProduct.productId} in warehouse $warehouse, adding it"
                )
                warehouse.inventory.add(newWarehouseProduct)
                warehouseRepository.save(warehouse)
                LOGGER.info("Added product ${newWarehouseProduct.productId} to warehouse $warehouse")
                return "product added"
            }
        }
    }

    fun editAlarm(warehouseId: String, warehouseAlarmDTO: WarehouseAlarmDTO): String {
        LOGGER.info("Received request to edit ${warehouseAlarmDTO.productId} alarm in $warehouseId")
        val warehouse = warehouseRepository.getWarehouseByWarehouseId(warehouseId)
        if (warehouse == null) {
            LOGGER.info("Could not find warehouse $warehouseId")
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
                LOGGER.info(
                    "Updated alarm threshold for product ${newWarehouseProduct.productId} in warehouse $warehouseId"
                )
                return "success"
            } ?: kotlin.run {
                LOGGER.info("Could not find product ${newWarehouseProduct.productId} in warehouse $warehouseId")
                // If product is missing, it is **not** created in this case
                return "could not find product"
            }
        }
    }

}