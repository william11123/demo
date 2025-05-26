package com.example.demo.dto 

data class UserCreatePredictUpload(
    val customNo: String,
    val taskType: String,
    val inMonth: String,
    val income: Int
)

data class PredictUploadQueryCriteria(
    val customNo: String? = null,
    val taskType: String? = null,
    val inMonth: String? = null,
    val minIncome: Int? = null,
    val maxIncome: Int? = null
    // 您可以根據需要加入更多查詢欄位，例如日期範圍等
)