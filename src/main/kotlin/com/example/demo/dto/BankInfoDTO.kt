package com.example.demo.dto

/**
 * 用於儲存銀行資訊查詢結果的資料傳輸物件 (DTO)。
 * 欄位對應原生 SQL 查詢返回的資料。
 */
data class BankInfoDTO(
    val bankname: String?,
    val nickName: String?,
    val addr: String?,
    val ***REMOVED***leLia: String?,
    val tel1: String?,
    val tel2: String?,
    val compMonth: String?
)
