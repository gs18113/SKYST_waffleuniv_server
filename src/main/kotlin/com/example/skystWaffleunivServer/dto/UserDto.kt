package com.example.skystWaffleunivServer.dto

class UserDto (

) {
    companion object {
        fun fromEntity(entity: com.example.skystWaffleunivServer.domain.user.UserEntity): UserDto {
            return UserDto(

            )
        }
    }
}