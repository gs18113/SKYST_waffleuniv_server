package com.example.skystWaffleunivServer.jwt.service

import com.example.skystWaffleunivServer.config.JwtConfig
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date

@Service
class TokenService(private val jwtConfig: JwtConfig) {
    fun generateToken(userId: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + jwtConfig.accessTokenExpiration)

        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray()))
            .compact()
    }

    fun getUserIdFromToken(token: String): Long {
        val claims =
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray()))
                .build()
                .parseClaimsJws(token)
                .body

        return claims.subject.toLong()
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtConfig.secret.toByteArray()))
                .build()
                .parseClaimsJws(token)
                .body
                .subject
                .toLong()
            return true
        } catch (ex: Exception) {
            return false
        }
    }
}
