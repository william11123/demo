package com.example.demo.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Column
import java.time.LocalDate

/**
 * Invsndata 實體類別 (Kotlin 版本)，對應資料庫中的 'invsndata' 資料表。
 * 用於儲存庫存異動的詳細資料。
 */
@Entity // 標記這是一個 JPA 實體
@Table(name = "invsndata") // 指定對應的資料庫表名
data class Invsndata( // Kotlin 的 data class 會自動產生 getter, setter, equals, hashCode, toString 等方法

    @Id // 標記此欄位為主鍵
    @Column(name = "SN", nullable = false)
    var sn: String, // 主鍵 SN，定義為非空，因為資料庫不允許為 null。

    @Column(name = "Shopno") // 對應資料庫欄位 'shopno'
    var shopNo: String? = null, // 店鋪代碼，可為 null。

    @Column(name = "TermNo") // 對應資料庫欄位 'TermNo'
    var termNo: String? = null, // 庫位代碼，可為 null。

    @Column(name = "location", nullable = false) // 對應資料庫欄位 'location'
    var location: String ,// 庫位位置，定義為非空，因為資料庫不允許為 null。

    @Column(name = "WHNo", nullable = false) // 對應資料庫欄位 'quantity'
    var whno: String, // 倉庫代碼，定義為非空，因為資料庫不允許為 null。
)