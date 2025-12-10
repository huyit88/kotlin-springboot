package com.example.orders.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RemoteCustomerDto(
    val id: Long,
    val name: String
)

