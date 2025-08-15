package com.example.demo.service;

import com.example.demo.dto.LeaseContractDTO;
import com.example.demo.dto.TaskSummaryDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook; // *** 關鍵修改：匯入 SXSSFWorkbook ***
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * 提供將大型報表直接串流到 HTTP 回應的服務。
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

        // 對於這個報表，資料量可能不大，XSSFWorkbook 即可。
        // 但為了統一，也可以換成 SXSSFWorkbook。
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("任務統計報表");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"銀行", "作業別", "月份", "數量", "正常完成數量(排除軟派)", "共用軟派數量"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (TaskSummaryDTO summary : summaryList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(summary.getBank());
                row.createCell(1).setCellValue(summary.getTaskType());
                row.createCell(2).setCellValue(summary.getMonth());
                row.createCell(3).setCellValue(summary.getQuantity());
                row.createCell(4).setCellValue(summary.getCompletedQuantity());
                row.createCell(5).setCellValue(summary.getSharedSoftwareQuantity());
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 【優化版】將租賃合約數據直接串流匯出為 Excel 檔案。
     * @param leaseContractList 租賃合約數據列表
     * @param response HttpServletResponse 物件
     * @throws IOException 當寫入 response 時發生錯誤
     */
    public void exportLeaseContractToExcel(List<LeaseContractDTO> leaseContractList, HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"lease_contract_export.xlsx\"");

        // *** 關鍵修改：使用 SXSSFWorkbook ***
        // 設置滑動窗口大小為 100，表示記憶體中最多只保留 100 行資料
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) { 
            Sheet sheet = workbook.createSheet("合約報表");

            // 定義表頭
            Row headerRow = sheet.createRow(0);
            String[] headers = {"銀行名稱", "作業類型", "作業編號", "維護類型", "EDC", "完成日期", "是否完成", "專案編號", "保固年限", "請求日期", "作業單號", "SN"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // 填入資料
            int rowNum = 1;
            for (LeaseContractDTO contract : leaseContractList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(contract.getBankname());
                row.createCell(1).setCellValue(contract.getTasktype());
                row.createCell(2).setCellValue(contract.getTaskno());
                row.createCell(3).setCellValue(contract.getMaint());
                row.createCell(4).setCellValue(contract.getEdc());
                row.createCell(5).setCellValue(contract.getCompDate());
                row.createCell(6).setCellValue(contract.getIscomp());
                row.createCell(7).setCellValue(contract.getPrjno());
                row.createCell(8).setCellValue(contract.getSaveyear());
                row.createCell(9).setCellValue(contract.getRequestDate());
                row.createCell(10).setCellValue(contract.getTono());
                row.createCell(11).setCellValue(contract.getSn());
            }

            // 注意：SXSSFWorkbook 不支援 autoSizeColumn，因為它無法存取已經寫入磁碟的行。
            // 您可以為欄位設置一個合理的預估寬度。
            // for (int i = 0; i < headers.length; i++) {
            //     sheet.setColumnWidth(i, 4000); // 範例寬度
            // }

            workbook.write(response.getOutputStream());
            
            // *** 關鍵修改：SXSSFWorkbook 需要手動清理暫存檔案 ***
            workbook.dispose();
        }
    }
}
