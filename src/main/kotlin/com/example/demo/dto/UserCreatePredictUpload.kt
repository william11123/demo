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
    // 您可以根據需要加入更多查詢欄位，例如日期範圍等
)

/**
 * 🆕 Excel 匯入專用的資料傳輸物件
 * 允許欄位為 null，用於處理 Excel 匯入時的不完整資料
 */
data class PredictUploadImportData(
    val customNo: String? = null,
    val taskType: String? = null,  
    val inMonth: String? = null,
    val income: Int? = 0
) {
    /**
     * 將匯入資料轉換為創建 DTO
     * 只有在所有必要欄位都不為空時才會轉換成功
     * 
     * @return UserCreatePredictUpload 如果轉換成功，否則為 null
     */
    fun toCreateDto(): UserCreatePredictUpload? {
        return if (isValid()) {
            // 因為 isValid() 已經檢查過，這裡可以安全地使用 !!
            UserCreatePredictUpload(customNo!!, taskType!!, inMonth!!, income!!)
        } else {
            null
        }
    }
    
    /**
     * 檢查資料是否有效（所有必要欄位都不為空或空白）
     */
    fun isValid(): Boolean {
        return customNo?.isNotBlank() == true &&
               taskType?.isNotBlank() == true &&
               inMonth?.isNotBlank() == true &&
               income != null
    }
}