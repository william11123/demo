package com.example.demo.model;

import jakarta.persistence.Column; // 如果需要，確保匯入
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "BankIncome") // 對應資料庫中的 'BankIncome' 表格
public class PredictUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 欄位改為小駝峰命名
    // 如果資料庫欄位名是 "CustomNo"，則使用 @Column(name = "CustomNo")
    @Column(name = "CustomNo") // 假設資料庫欄位是 CustomNo
    private String customNo;

    @Column(name = "TaskType") // 假設資料庫欄位是 TaskType
    private String taskType;

    @Column(name = "InMonth")  // 假設資料庫欄位是 InMonth
    private String inMonth;

    @Column(name = "Income")   // 假設資料庫欄位是 Income
    private int income;

    // Getters and Setters
 
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomNo() {
        return customNo; // 對應修改後的欄位名
    }

    public void setCustomNo(String customNo) {
        this.customNo = customNo; // 對應修改後的欄位名
    }

    public String getTaskType() {
        return taskType; // 對應修改後的欄位名
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType; // 對應修改後的欄位名
    }

    public String getInMonth() {
        return inMonth; // 對應修改後的欄位名
    }

    public void setInMonth(String inMonth) {
        this.inMonth = inMonth; // 對應修改後的欄位名
    }

    public int getIncome() {
        return income; // 對應修改後的欄位名
    }

    public void setIncome(int income) {
        this.income = income; // 對應修改後的欄位名
    }
}