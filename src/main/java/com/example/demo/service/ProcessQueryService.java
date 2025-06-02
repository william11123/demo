package com.example.demo.service;

import com.example.demo.dto.ProcessQueryDTO;
import com.example.demo.repository.ProcessQueryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.tran***REMOVED***ction.annotation.Tran***REMOVED***ctional; // 建議對讀取操作也加上事務註解

import java.util.List;

@Service // 標記這是一個 Spring Service 元件
public class ProcessQueryService {

    private final ProcessQueryRepository processQueryRepository;

    // 透過建構子注入 Repository
    //@Autowired
    public ProcessQueryService(ProcessQueryRepository processQueryRepository) {
        this.processQueryRepository = processQueryRepository;
    }

    /**
     * 根據輸入值查詢處理詳細資訊。
     *
     * @param inputValue 用於查詢 realsn 或 changesn 的輸入值。
     * @return 符合條件的 ProcessQueryDTO 列表。如果找不到，則返回空列表。
     */
    @Tran***REMOVED***ctional(readOnly = true) // 標記為唯讀事務，有助於效能最佳化
    public List<ProcessQueryDTO> getProcessDetails(String inputValue) {
        // 直接呼叫 Repository 的方法
        List<ProcessQueryDTO> results = processQueryRepository.findProcess(inputValue);

        // 在這裡您可以加入額外的業務邏輯，例如：
        // - 對結果進行轉換或過濾
        // - 記錄查詢日誌
        // - 檢查權限等
        // 目前，我們只是直接返回查詢結果

        return results;
    }
}