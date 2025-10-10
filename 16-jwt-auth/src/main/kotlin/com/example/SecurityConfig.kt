package com.example

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.http.HttpMethod

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val authEntryPoint: JsonAuthEntryPoint,
    private val accessDeniedHandler: JsonAccessDeniedHandler
){
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(encoder: PasswordEncoder) : UserDetailsService = InMemoryUserDetailsManager(
        User.withUsername("user").password(encoder.encode("pass")).roles("USER").build(),
        User.withUsername("admin").password(encoder.encode("pass")).roles("ADMIN").build(),
    )

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager =
        authenticationConfiguration.authenticationManager

    @Bean
    fun securityFilterChan(http: HttpSecurity, jwtAuthFilter: JwtAuthFilter): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors{}
            .sessionManagement{ it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .httpBasic {}
            .authorizeHttpRequests {
                it.requestMatchers(
                "/health", 
                "/auth/login", 
                "/swagger-ui/**", 
                "/v3/api-docs/**"
                ).permitAll()
                it.requestMatchers(HttpMethod.GET, "/api/users/**").hasAnyRole("USER","ADMIN")
                it.requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                it.anyRequest().authenticated()
            }
            .exceptionHandling{
                it.authenticationEntryPoint(authEntryPoint)
                it.accessDeniedHandler(accessDeniedHandler)
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()

    }
}