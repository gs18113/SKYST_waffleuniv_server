package com.example.skystWaffleunivServer.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "jwt")
class JwtConfig {
    lateinit var secret: String
    var accessTokenExpiration: Long = 3600000 // 1 hour by default
}
