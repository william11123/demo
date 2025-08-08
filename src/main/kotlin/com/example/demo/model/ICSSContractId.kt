package com.example.demo.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

/**
 * ICSSContract 的複合主鍵類別。
 * 必須實作 Serializable。
 */
@Embeddable
data class ICSSContractId(
    @Column(name = "ContractNo", nullable = false)
    var contractNo: String,

    @Column(name = "ContractName", nullable = false)
    var contractName: String,

    @Column(name = "StartDate", nullable = false)
    var startDate: String
) : Serializable
