package it.polito.ap.warehouseservice.service

import it.polito.ap.common.dto.WarehouseAlarmDTO
import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.model.Warehouse
import it.polito.ap.warehouseservice.model.WarehouseProduct
import it.polito.ap.warehouseservice.repository.WarehouseRepository
import it.polito.ap.warehouseservice.service.mapper.WarehouseMapper
import org.bson.types.ObjectId
import org.mapstruct.Mapper
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

    fun editAlarm(warehouseId: String, productAlarm: WarehouseAlarmDTO): String {
        LOGGER.info("Received request to edit ${productAlarm.productId} alarm in $warehouseId")
        val warehouse = warehouseRepository.getWarehouseById(warehouseId)
        if (warehouse == null) {
            LOGGER.info("Could not find warehouse $warehouseId")
            return "could not find warehouse"
        } else {
            val newWarehouseProduct = mapper.toModel(productAlarm)
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
                return "could not find product"
            }
        }
    }

}