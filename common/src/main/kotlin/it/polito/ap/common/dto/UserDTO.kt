package it.polito.ap.common.dto

import it.polito.ap.common.utils.RoleType

data class UserDTO (
    var email: String,
    var role: RoleType
)