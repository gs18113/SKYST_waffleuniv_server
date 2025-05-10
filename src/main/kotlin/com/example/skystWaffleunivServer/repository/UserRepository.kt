package com.example.skystWaffleunivServer.repository

import com.example.skystWaffleunivServer.domain.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long>{
    fun countByColorHex(colorHex: String): Int
}
