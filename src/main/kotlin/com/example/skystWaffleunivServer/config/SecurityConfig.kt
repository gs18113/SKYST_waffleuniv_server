package com.example.skystWaffleunivServer.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins =
            listOf(
                "http://localhost:3000",
                "https://skyst-waffleuniv-client.vercel.app/",
            ) // Vite
        configuration.allowCredentials = true
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE")
        configuration.allowedHeaders =
            listOf(
                "Origin",
                "X-Requested-With",
                "Content-Type",
                "Authorization",
                "Location",
            )
        configuration.exposedHeaders = listOf("Authorization", "Location")
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            cors { configurationSource = corsConfigurationSource() }
            csrf { disable() }
            authorizeHttpRequests {
//                authorize("/", permitAll)
//                authorize("/error", permitAll)
//                authorize("/swagger-ui/**", permitAll)
//                authorize("/api-docs/**", permitAll)
//                authorize("/v3/api-docs/**", permitAll)
//                authorize("/swagger-resources/**", permitAll)
//
//                // APIs that do not require authentication
//                authorize("/api/auth/**", permitAll)
//                authorize("/api/v1/pingpong/**", permitAll)
//                authorize("/error", permitAll)
//                authorize("/redirect", permitAll)
//                authorize(anyRequest, authenticated)
                authorize(anyRequest, permitAll)
            }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            exceptionHandling {
                // authenticationEntryPoint = customAuthenticationEntryPoint
            }
            addFilterAfter<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
            // addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
        }
        return http.build()
    }
}
