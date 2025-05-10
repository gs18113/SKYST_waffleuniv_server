package com.example.skystWaffleunivServer.repository

import com.example.skystWaffleunivServer.domain.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long>
