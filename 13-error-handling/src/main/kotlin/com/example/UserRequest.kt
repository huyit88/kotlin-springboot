package com.example

import jakarta.validation.constraints.*

data class UserReq(
    @field:NotBlank val name: String, 
    @field:Email val email: String)