package com.example.skystWaffleunivServer.dto

import com.example.skystWaffleunivServer.domain.user.UserEntity

data class UserDto(
    val id: Long,
) {
    companion object {
        fun fromEntity(user: UserEntity): UserDto {
            return UserDto(
                id = user.id!!,
            )
        }
    }
}
