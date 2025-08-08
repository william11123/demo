package com.example.demo.model

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate

/**
 * 對應資料庫中的 iCSSContract (合約資料) 表格的實體類別。
 * 使用複合主鍵 (contractNo, contractName)。
 */
@Entity
@Table(name = "iCSSContract")
class ICSSContract {

    @EmbeddedId
    lateinit var id: ICSSContractId // 使用 @EmbeddedId 指向複合主鍵類別

    @Column(name = "CustomNo")
    var customNo: String? = null // 客戶編號

    @Column(name = "InvoiceName")
    var invoiceName: String? = null // 發票名稱 (客戶名稱)

    @Column(name = "BinCode")
    var binCode: String? = null // BinCode

    @Column(name = "EndDate")
    var endDate: String? = null // 合約結束日期

    @Column(name = "ContractAmt")
    var contractAmt: BigDecimal? = null // 合約金額

    @Column(name = "Note")
    var note: String? = null // 備註

    @Column(name = "Sales")
    var ***REMOVED***les: String? = null

    @Column(name = "SendMark")
    var sendMark: String? = null // 新增欄位

    @Column(name = "TermSalesMail")
    var termSalesMail: String? = null // 新增欄位

    // JPA 需要一個無參數的建構子
    constructor()

    // 提供一個方便的建構子來設定複合主鍵
    constructor(id: ICSSContractId) {
        this.id = id
    }
}
