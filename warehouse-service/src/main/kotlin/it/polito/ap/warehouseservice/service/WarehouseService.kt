package it.polito.ap.warehouseservice.service

import it.polito.ap.common.dto.WarehouseAlarmDTO
import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.model.Warehouse
import it.polito.ap.warehouseservice.model.WarehouseProduct
import it.polito.ap.warehouseservice.repository.WarehouseRepository
import it.polito.ap.warehouseservice.service.mapper.WarehouseMapper
import org.mapstruct.Mapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WarehouseService (val warehouseRepository: WarehouseRepository, val mapper: WarehouseMapper) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }

    fun editAlarm(warehouseId: String, productAlarm: WarehouseAlarmDTO): WarehouseAlarmDTO? {
        // TODO: differentiate between failures owed to not finding the warehouse or the product in the warehouse
        // TODO: is it right to return a DTO?
        LOGGER.info("Received request to edit ${productAlarm.productId} alarm in $warehouseId")
        val warehouseOptional = warehouseRepository.findById(warehouseId)
        if (warehouseOptional.isPresent) {
            val warehouse = warehouseOptional.get()
            val warehouseProduct = mapper.toModel(productAlarm)
            val oldWarehouseProduct = warehouse.inventory.find { it.productId == warehouseProduct.productId }
            oldWarehouseProduct?.let {
                warehouseProduct.quantity = it.quantity
                warehouse.inventory[warehouse.inventory.indexOf(oldWarehouseProduct)] = warehouseProduct
                warehouseRepository.save(warehouse)
                LOGGER.info(
                    "Updated alarm threshold for product ${warehouseProduct.productId} in warehouse $warehouseId"
                )
                return mapper.toAlarmDTO(warehouseProduct)
            } ?: kotlin.run {
                LOGGER.info("Could not find product ${warehouseProduct.productId} in warehouse $warehouseId")
                return null
            }
        } else {
            LOGGER.info("Could not find warehouse $warehouseId")
            return null
        }
    }

}