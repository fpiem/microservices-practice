package it.polito.ap.warehouseservice.service.mapper

import it.polito.ap.common.dto.WarehouseAlarmDTO
import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.model.WarehouseProduct
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface WarehouseMapper {

    fun toWarehouseAlarmDTO(warehouseProduct: WarehouseProduct): WarehouseAlarmDTO
    fun toWarehouseProductDTO(warehouseProduct: WarehouseProduct): WarehouseProductDTO
    fun toProductDTOList(inventory: List<WarehouseProduct>): List<WarehouseProductDTO>
    fun toModel(warehouseProductDTO: WarehouseProductDTO): WarehouseProduct
    fun toModel(warehouseAlarmDTO: WarehouseAlarmDTO): WarehouseProduct
}