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

/**
 * 處理銀行資訊查詢與匯出請求的 REST Controller。
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

            // 開始日期為開始月份的第一天
            LocalDate startDate = startYearMonth.atDay(1);
            // 結束日期為結束月份的最後一天的隔天，以使用 '<' 進行比較
            LocalDate endDate = endYearMonth.atEndOfMonth().plusDays(1);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            List<BankInfoDTO> results = bankInfoService.getBankInfo(startDate.format(formatter), endDate.format(formatter));
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 處理將銀行資訊匯出為 Excel 的 GET 請求，支援日期區間。
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
            // *** 修正了這裡的拼字錯誤 ***
            YearMonth endYearMonth = YearMonth.parse(endMonthStr);

            LocalDate startDate = startYearMonth.atDay(1);
            LocalDate endDate = endYearMonth.atEndOfMonth().plusDays(1);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            List<BankInfoDTO> results = bankInfoService.getBankInfo(startDate.format(formatter), endDate.format(formatter));
            ByteArrayInputStream in = excelExportService.exportBankInfoToExcel(results);

            HttpHeaders headers = new HttpHeaders();
            String filename = String.format("BankInfo-%s-to-%s.xlsx", startMonthStr, endMonthStr);
            // 處理非 ASCII 檔名，使其在瀏覽器中能正確顯示
            String encodedFilename = new String(filename.getBytes("UTF-8"), "ISO-8859-1");
            headers.add("Content-Disposition", "attachment; filename=" + encodedFilename);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));

        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
