package com.example.demo.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Column

@Entity // 標記這是一個 JPA 實體
@Table(name = "TaskOrder") // 指定對應的資料庫表名
data class TaskOrder( // Kotlin 的 data class 會自動產生 getter, setter, equals, hashCode, toString 等方法

    @Id // 標記此欄位為主鍵
    @Column(name = "tono", nullable = false) // 懶得管大小寫了
    var tono: String, // 主鍵 tono，定義為非空，因為資料庫不允許為 null。

    @Column(name = "shopno")   // 對應資料庫欄位 'shopno'
    var shopno: String? = null,     // 店鋪代碼，可為 null。

    @Column(name = "custmno")   // 對應資料庫欄位 'custmno'
    var custmno: String? = null, // 客戶代碼，可為 null。
)