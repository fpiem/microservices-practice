package it.polito.ap.warehouseservice.service.mapper

import it.polito.ap.common.dto.WarehouseAlarmDTO
import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.model.WarehouseProduct
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface WarehouseMapper {

    fun toAlarmDTO(warehouseProduct: WarehouseProduct): WarehouseAlarmDTO
    fun toWarehouseDTO(warehouseProduct: WarehouseProduct): WarehouseProductDTO
    fun toModel(warehouseProductDTO: WarehouseProductDTO): WarehouseProduct
    fun toModel(warehouseAlarmDTO: WarehouseAlarmDTO): WarehouseProduct

}