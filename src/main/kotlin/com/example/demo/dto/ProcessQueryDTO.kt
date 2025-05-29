package com.example.demo.dto 

import com.example.demo.model.TaskReplyTermDet // 確保 TaskReplyTermDet 的路徑正確


data class ProcessQueryDTO(
    val shopno: String? = null,
    val termno: String,
    val taskReplyTermDet: TaskReplyTermDet
)