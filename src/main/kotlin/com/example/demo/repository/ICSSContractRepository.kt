package com.example.demo.repository

import com.example.demo.model.ICSSContract
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 用於存取 iCSSContract 資料的 Repository 介面。
 * JpaRepository 提供了所有基本的 CRUD (建立、讀取、更新、刪除) 操作。
 */
@Repository
interface ICSSContractRepository : JpaRepository<ICSSContract, String> {
    // JpaRepository<ICSSContract, String> 中的
    // ICSSContract 是這個 Repository 管理的實體 (Model)
    // String 是主鍵 (我們在 ICSSContract 中定義的 contractNo) 的資料型態

    // Spring Data JPA 會自動提供如 ***REMOVED***ve(), findById(), findAll() 等方法。
    // 如果未來有更複雜的查詢需求 (例如：透過客戶編號 CustomNo 尋找合約)，
    // 可以在這裡定義自訂的查詢方法，例如：
    // fun findByCustomNo(customNo: String): List<ICSSContract>
}
