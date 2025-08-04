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
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class BankInfoController {

    @Autowired
    private BankInfoService bankInfoService;

    @Autowired
    private ExcelExportService excelExportService;

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

            // 呼叫 Service 方法獲取所有資料
            List<BankInfoDTO> results = bankInfoService.getBankInfoForExport(startDate.format(formatter), endDate.format(formatter));
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
            // 如果匯出過程中發生任何錯誤，回傳 500 伺服器內部錯誤
            return ResponseEntity.status(500).build();
        }
    }
}
