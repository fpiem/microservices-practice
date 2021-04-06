package it.polito.ap.catalogservice.service.mapper

import it.polito.ap.catalogservice.model.User
import it.polito.ap.common.dto.UserDTO
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface UserMapper {
    // TODO capire se si può correggere il mapping tra id in formato String e ObjectId
    // fun toUserDTO(user: User) : UserDTO
    // fun toUser(userDTO: UserDTO) : User
}