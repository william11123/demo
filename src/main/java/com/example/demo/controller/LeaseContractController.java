package com.example.demo.controller;

import com.example.demo.dto.LeaseContractDTO;
import com.example.demo.service.ExcelStreamingService;
import com.example.demo.service.LeaseContractService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * 處理租賃與非租賃合約報表相關的 HTTP 請求。
 */
@Controller
public class LeaseContractController {

    private final LeaseContractService leaseContractService;
    private final ExcelStreamingService excelStreamingService;

    @Autowired
    public LeaseContractController(LeaseContractService leaseContractService, ExcelStreamingService excelStreamingService) {
        this.leaseContractService = leaseContractService;
        this.excelStreamingService = excelStreamingService;
    }

    /**
     * 接收前端請求，根據類型查詢數據並以串流方式匯出為 Excel 檔案。
     * @param startDate 開始日期 (格式 YYYY-MM-DD)
     * @param endDate 結束日期 (格式 YYYY-MM-DD)
     * @param type 查詢類型 ("lease" 或 "non-lease")
     * @param response HttpServletResponse 物件，用於寫入 Excel 檔案
     * @throws IOException 當寫入 response 時發生錯誤
     */
    @GetMapping("/export-lease-contract")
    public void exportLeaseContract(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "lease") String type, // 新增 type 參數，預設為 "lease"
            HttpServletResponse response) throws IOException {

        List<LeaseContractDTO> contractData;

        // 根據 type 參數決定呼叫哪個 Service 方法
        if ("non-lease".equalsIgnoreCase(type)) {
            contractData = leaseContractService.getNonLeaseContractData(startDate, endDate);
        } else { // 預設或 type 為 "lease"
            contractData = leaseContractService.getLeaseContractData(startDate, endDate);
        }

        // 檢查是否有資料
        if (contractData == null || contractData.isEmpty()) {
            response.setContentType("text/html; charset=UTF-8");
            response.getWriter().println("<script>alert('查無資料'); window.history.back();</script>");
            return;
        }

        // 呼叫串流服務將數據匯出為 Excel
        excelStreamingService.exportLeaseContractToExcel(contractData, response);
    }
}
