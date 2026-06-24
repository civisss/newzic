package com.newzic.config

import com.newzic.security.JwtAuthenticationFilter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
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
@EnableConfigurationProperties(JwtProperties::class)
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthenticationFilter,
    @org.springframework.beans.factory.annotation.Value("\${app.cors.allowed-origins}")
    private val corsOrigins: String
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/songs/recommended").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/songs/liked").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/songs/*/liked").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/songs/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/artists/recommended").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/artists/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/spotlight/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/feed/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/collaborations/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/albums/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/journal/feed").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/journal/search").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/journal/user/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/journal/*/comments").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/journal/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/messages/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/messages/**").authenticated()
                    .requestMatchers("/api/workspaces/**").authenticated()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOrigins = corsOrigins.split(",").map { it.trim() }
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true
        config.maxAge = 3600

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
