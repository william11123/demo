package com.example.demo.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Column

/**
 * User_Info 實體類別 (Kotlin 版本)，對應資料庫中的 'user_info' 資料表。
 * 用於儲存使用者的帳號和密碼資訊。
 */
@Entity // 標記這是一個 JPA 實體
@Table(name = "USER_INFOS") // 指定對應的資料庫表名
data class User_Info( // Kotlin 的 data class 會自動產生 getter, setter, equals, hashCode, toString 等方法

    @Id // 標記此欄位為主鍵
    @Column(name = "USER_ID", nullable = false)
    var userid: String, 

    @Column(name = "Password", nullable = false)
    var password: String,

    @Column(name = "SecLevel", nullable = false)
    var seclevel: String
)