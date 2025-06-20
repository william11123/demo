package com.example.demo.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Column

@Entity // 標記這是一個 JPA 實體
@Table(name = "TaskReplyTermDet") // 指定對應的資料庫表名
data class TaskReplyTermDet( // Kotlin 的 data class 會自動產生 getter, setter, equals, hashCode, toString 等方法

    @Id // 標記此欄位為主鍵
    @Column(name = "termno", nullable = false) // 懶得管大小寫了，以後再來改成駝峰式命名
    var termno: String, // 主鍵 termno，定義為非空，因為資料庫不允許為 null。

    @Column(name = "tono")   // 對應資料庫欄位 'tono'
    var tono: String? = null,   // 任務單號，可為 null。

    @Column(name = "mateno")   
    var mateno: String? = null, 

    @Column(name = "realsn")   
    var realsn: String? = null, 

    @Column(name = "changesn")   
    var changesn: String? = null, 
    
    @Column(name = "upd_date")   
    var upd_date: String? = null, 
)