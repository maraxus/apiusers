package aniq.dev.apiusers.controller

import aniq.dev.apiusers.dto.UserDTO

data class UserCollectionResponse(
    val records: List<UserDTO> = emptyList()
)
