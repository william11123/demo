package com.example.demo.model

import jakarta.persistence.*

@Entity
@Table(name = "location_targets") // 對應資料庫中的 'location_targets' 表格
data class LocationTarget(
    @Id
    @Column(name = "name", nullable = false, unique = true)
    var name: String,

    @Column(name = "latitude", nullable = false)
    var latitude: Double,

    @Column(name = "longitude", nullable = false)
    var longitude: Double
)
