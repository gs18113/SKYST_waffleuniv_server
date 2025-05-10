package com.example.skystWaffleunivServer.controller

import com.example.skystWaffleunivServer.dto.UserDto
import com.example.skystWaffleunivServer.jwt.service.TokenService
import com.example.skystWaffleunivServer.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService,
    private val tokenService: TokenService,
) {
    @PostMapping("/register")
    fun register(
        @RequestBody userData: UserService.UserCreateDto,
    ): ResponseEntity<String> {
        // Create a new user in the database
        val user = userService.createUser(userData)

        // Generate JWT token with the user's ID
        val token = tokenService.generateToken(user.id!!)

        return ResponseEntity.ok(token)
    }

    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserDto> {
        val userId = authentication.principal as Long
        val user =
            userService.getUserById(userId)
                ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(UserDto.fromEntity(user))
    }
}
