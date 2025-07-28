package com.example.demo.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "check_in_records") // 對應資料庫中的 'check_in_records' 表格
data class CheckInRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null, // ID 在儲存前是 null，所以設為可選

    @Column(name = "user_id", nullable = false)
    var userId: String,

    @Column(name = "location_name", nullable = false)
    var locationName: String,

    @Column(name = "check_in_time", nullable = false)
    var checkInTime: LocalDateTime
)
