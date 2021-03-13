package it.polito.ap.warehouseservice.service.mapper

import it.polito.ap.common.dto.WarehouseProductDTO
import it.polito.ap.warehouseservice.model.WarehouseProduct
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.springframework.stereotype.Component

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
interface WarehouseMapper {

    //fun toAlarmDTO(warehouse: Warehouse): WarehouseAlarmDTO
    //fun toWarehouseDTO(warehouse: Warehouse): WarehouseProductDTO
    fun toModel(warehouseProductDTO: WarehouseProductDTO): WarehouseProduct
    //fun toModel(warehouseAlarmDTO: WarehouseAlarmDTO): WarehouseProduct

}