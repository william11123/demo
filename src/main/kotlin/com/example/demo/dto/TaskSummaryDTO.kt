package com.example.demo.dto

/**
 * 用於封裝任務統計查詢結果的數據傳輸對象 (DTO)。
 * 這個類別對應於您提供的 SQL 查詢所回傳的欄位。
 */
data class TaskSummaryDTO(
    // 銀行名稱
    val bank: String,

    // 作業類別 (例如：安裝, 回收, 維修)
    val taskType: String,

    // 任務通知的月份 (格式：YYYY-MM)
    val month: String,

    // 該月份的總任務數量
    val quantity: Long,

    // 正常完成的任務數量
    val completedQuantity: Long,

    // 共用軟體的任務數量
    val sharedSoftwareQuantity: Long
)
