package com.example.demo.service;

import com.example.demo.dto.TaskSummaryDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 提供將大型報表直接串流到 HTTP 回應的服務。
 * 這種方法記憶體效率高，適合處理潛在的大型檔案。
 */
@Service
public class ExcelStreamingService {

    /**
     * 將任務統計數據直接串流匯出為 Excel 檔案。
     * @param summaryList 任務統計數據列表
     * @param response HttpServletResponse 物件
     * @throws IOException 當寫入 response 時發生錯誤
     */
    public void exportTaskSummaryToExcel(List<TaskSummaryDTO> summaryList, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"task_summary.xlsx\"");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("任務統計報表");

            // 更新標頭，加入新欄位
            Row headerRow = sheet.createRow(0);
            String[] headers = {"銀行", "作業別", "月份", "數量", "正常完成數量(排除軟派)", "共用軟派數量"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填入資料
            int rowNum = 1;
            for (TaskSummaryDTO summary : summaryList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(summary.getBank());
                row.createCell(1).setCellValue(summary.getTaskType());
                row.createCell(2).setCellValue(summary.getMonth());
                row.createCell(3).setCellValue(summary.getQuantity());
                row.createCell(4).setCellValue(summary.getCompletedQuantity());
                // 寫入新欄位的資料
                row.createCell(5).setCellValue(summary.getSharedSoftwareQuantity());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(response.getOutputStream());
        }
    }
}
