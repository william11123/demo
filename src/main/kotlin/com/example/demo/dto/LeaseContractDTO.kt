package com.example.demo.dto

/**
 * 用於封裝租賃合約查詢結果的數據傳輸對象 (DTO)。
 * 這個類別對應於原生 SQL 查詢所回傳的欄位。
 */
data class LeaseContractDTO(
    val bankname: String?,
    val tasktype: String?,
    val taskno: String?,
    val maint: String?,
    val edc: String?,
    val compDate: String?,
    val iscomp: String?,
    val prjno: String?,
    val saveyear: String?,
    val requestDate: String?,
    val tono: String?,
    val sn: String?
)
