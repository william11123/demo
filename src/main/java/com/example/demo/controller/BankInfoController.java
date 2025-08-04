package com.example.demo.controller;

import com.example.demo.dto.BankInfoDTO;
import com.example.demo.service.BankInfoService;
import com.example.demo.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * 處理銀行資訊查詢與匯出請求的 REST Controller (最終簡化版)。
 */
@RestController
public class BankInfoController {

    @Autowired
    private BankInfoService bankInfoService;

    @Autowired
    private ExcelExportService excelExportService;

    /**
     * 處理對銀行資訊的 GET 請求，支援日期區間。
     * @param startMonthStr 以 "yyyy-MM" 格式表示的開始年月。
     * @param endMonthStr   以 "yyyy-MM" 格式表示的結束年月。
     * @return 包含查詢結果的 ResponseEntity。
     */
    @GetMapping("/api/bank-info")
    public ResponseEntity<List<BankInfoDTO>> getBankInfo(
            @RequestParam("startMonth") String startMonthStr,
            @RequestParam("endMonth") String endMonthStr) {
        try {
            YearMonth startYearMonth = YearMonth.parse(startMonthStr);
            YearMonth endYearMonth = YearMonth.parse(endMonthStr);

            LocalDate startDate = startYearMonth.atDay(1);
            LocalDate endDate = endYearMonth.atEndOfMonth().plusDays(1);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // 直接呼叫 Service 執行查詢並等待結果
            List<BankInfoDTO> results = bankInfoService.getBankInfo(startDate.format(formatter), endDate.format(formatter));
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            // 如果查詢失敗或參數錯誤，回傳 400 錯誤，並回傳一個空的列表
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    /**
     * 處理將銀行資訊匯出為 Excel 的 GET 請求。
     * @param startMonthStr 以 "yyyy-MM" 格式表示的開始年月。
     * @param endMonthStr   以 "yyyy-MM" 格式表示的結束年月。
     * @return 包含 Excel 檔案的 ResponseEntity。
     */
    @GetMapping("/api/bank-info/export")
    public ResponseEntity<InputStreamResource> exportBankInfo(
            @RequestParam("startMonth") String startMonthStr,
            @RequestParam("endMonth") String endMonthStr) {
        try {
            YearMonth startYearMonth = YearMonth.parse(startMonthStr);
            YearMonth endYearMonth = YearMonth.parse(endMonthStr);

            LocalDate startDate = startYearMonth.atDay(1);
            LocalDate endDate = endYearMonth.atEndOfMonth().plusDays(1);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            List<BankInfoDTO> results = bankInfoService.getBankInfo(startDate.format(formatter), endDate.format(formatter));
            ByteArrayInputStream in = excelExportService.exportBankInfoToExcel(results);

            HttpHeaders headers = new HttpHeaders();
            String filename = String.format("BankInfo-%s-to-%s.xlsx", startMonthStr, endMonthStr);
            String encodedFilename = new String(filename.getBytes("UTF-8"), "ISO-8859-1");
            headers.add("Content-Disposition", "attachment; filename=" + encodedFilename);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));

        } catch (Exception e) {
            // 如果匯出失敗，回傳 500 伺服器內部錯誤
            return ResponseEntity.status(500).build();
        }
    }
}
