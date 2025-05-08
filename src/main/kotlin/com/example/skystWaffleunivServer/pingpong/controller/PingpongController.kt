package com.example.skystWaffleunivServer.pingpong.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/pingpong")
@Tag(name = "Pingpong controller", description = "Pingpong controller API")
class PingpongController(
    // private val pingpongService: PingpongService
) {
    @GetMapping("/ping")
    @Operation(summary = "Pingpong", description = "Pingpong API for testing!")
    fun ping(): ResponseEntity<String> {
        return ResponseEntity.ok("Pong!")
    }
}
