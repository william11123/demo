package com.example.demo.dto 

data class ProcessQueryDTO(
    val compdate: String? = null,
    val shopno: String? = null,
    val iscomp:String? = null,
    val tono: String?,
    val mateno: String?,
    val termno: String? = null,
    val realsn: String?,
    val changesn: String?
)