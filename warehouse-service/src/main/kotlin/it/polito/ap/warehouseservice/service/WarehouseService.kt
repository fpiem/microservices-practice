package it.polito.ap.warehouseservice.service

import it.polito.ap.common.dto.WarehouseAlarmDTO
import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.model.Warehouse
import it.polito.ap.warehouseservice.model.WarehouseProduct
import it.polito.ap.warehouseservice.repository.WarehouseRepository
import org.mapstruct.Mapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class WarehouseService (val warehouseRepository: WarehouseRepository) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(javaClass)
    }
}