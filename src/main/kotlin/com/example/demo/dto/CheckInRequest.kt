package com.example.demo.dto;

data class CheckInRequest(
    val userId: String? = null,
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)


