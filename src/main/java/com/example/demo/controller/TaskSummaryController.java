package com.example.demo.controller;

import com.example.demo.dto.TaskSummaryDTO;
import com.example.demo.service.ExcelStreamingService; // 使用新的串流服務
import com.example.demo.service.TaskSummaryService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

/**
 * 處理任務統計報表相關的 HTTP 請求。
 */
@Controller
public class TaskSummaryController {

    private final TaskSummaryService taskSummaryService;
    private final ExcelStreamingService excelStreamingService; // 改為注入新的服務

    @Autowired
    public TaskSummaryController(TaskSummaryService taskSummaryService, ExcelStreamingService excelStreamingService) { // 更新建構函式
        this.taskSummaryService = taskSummaryService;
        this.excelStreamingService = excelStreamingService;
    }

    /**
     * 接收前端請求，查詢數據並以串流方式匯出為 Excel 檔案。
     * @param startDate 開始月份 (格式 YYYY-MM)
     * @param endDate 結束月份 (格式 YYYY-MM)
     * @param taskType 作業類別
     * @param customNo 銀行編號 (可選)
     * @param response HttpServletResponse 物件，用於寫入 Excel 檔案
     * @throws IOException 當寫入 response 時發生錯誤
     */
    @GetMapping("/export-task-summary")
    public void exportTaskSummary(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String taskType,
            @RequestParam(required = false) String customNo,
            HttpServletResponse response) throws IOException {

        // 1. 呼叫 Service 獲取統計數據
        List<TaskSummaryDTO> summaryData = taskSummaryService.getTaskSummary(startDate, endDate, taskType, customNo);

        // 2. 呼叫串流服務將數據匯出為 Excel
        excelStreamingService.exportTaskSummaryToExcel(summaryData, response);
    }
}
