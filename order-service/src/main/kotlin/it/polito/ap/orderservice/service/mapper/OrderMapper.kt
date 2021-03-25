package it.polito.ap.orderservice.service.mapper

import it.polito.ap.common.dto.CartProductDTO
import it.polito.ap.common.dto.OrderDTO
import it.polito.ap.orderservice.model.Order
import it.polito.ap.orderservice.model.utils.CartElement
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring")
interface OrderMapper {

    // TODO spostare queste due funzioni in un altro mapper, questa interface dovrebbe servire solo per Order
    @Mapping(target = ".", source = "productDTO")
    fun toModel(cartProductDTO: CartProductDTO): CartElement

    // TODO questo non funziona, perchè?
    fun toCartProductDTO(cartElement: CartElement): CartProductDTO

    // TODO non va perchè orderId è un ObjectId in uno e String nell'altro
    // fun toDTO(order: Order): OrderDTO
}