package it.polito.ap.orderservice.service.mapper

import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.orderservice.model.utils.CartElement
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring")
interface OrderMapper {

    @Mapping(target = ".", source = "productDTO")
    fun toModel(cartProductDTO: CartProductDTO): CartElement

}