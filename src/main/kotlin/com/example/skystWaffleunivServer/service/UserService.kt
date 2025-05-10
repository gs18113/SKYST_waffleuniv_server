package com.example.skystWaffleunivServer.service

import com.example.skystWaffleunivServer.domain.user.UserEntity
import com.example.skystWaffleunivServer.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {
    fun createUser(userData: UserCreateDto): UserEntity {
        val user = UserEntity()
        return userRepository.save(user)
    }

    fun getUserById(id: Long): UserEntity? {
        return userRepository.findById(id).orElse(null)
    }

    data class UserCreateDto(
        val email: String? = null,
        val name: String? = null,
    )
}
