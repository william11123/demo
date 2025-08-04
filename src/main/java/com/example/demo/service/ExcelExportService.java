package com.example.demo.service;

import com.example.demo.dto.BankInfoDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 提供將資料匯出為 Excel 檔案的服務。
 */
@Service
public class ExcelExportService {

    /**
     * 將銀行資訊列表轉換為 Excel 檔案的位元組陣列輸入流。
     * @param bankInfoList 要匯出的銀行資訊列表。
     * @return 包含 Excel 資料的 ByteArrayInputStream。
     */
    public ByteArrayInputStream exportBankInfoToExcel(List<BankInfoDTO> bankInfoList) throws IOException {
        String[] headers = {"銀行名稱", "商店名稱", "地址", "商店聯絡人", "電話一", "電話二", "完工月份"};
        
        // 使用 try-with-resources 確保資源被正確關閉
        try (
            Workbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
        ) {
            Sheet sheet = workbook.createSheet("銀行資訊");

            // 建立表頭
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填入資料
            int rowIdx = 1;
            for (BankInfoDTO info : bankInfoList) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(info.getBankname());
                row.createCell(1).setCellValue(info.getNickName());
                row.createCell(2).setCellValue(info.getAddr());
                row.createCell(3).setCellValue(info.getSaleLia());
                row.createCell(4).setCellValue(info.getTel1());
                row.createCell(5).setCellValue(info.getTel2());
                row.createCell(6).setCellValue(info.getCompMonth());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
