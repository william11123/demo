package com.example.demo.service;

import com.example.demo.dto.PredictUploadQueryCriteria; // <--- 新增匯入
import com.example.demo.model.PredictUpload;
import com.example.demo.repository.PredictUploadRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification; // <--- 新增匯入
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.criteria.Predicate; // <--- 新增匯入 (JPA Criteria API)

import java.io.ByteArrayInputStream; // <--- 新增匯入
import java.io.ByteArrayOutputStream; // <--- 新增匯入
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * PredictUpload 實體的服務層。
 * 負責處理與 PredictUpload 相關的業務邏輯，例如從 Excel 匯入資料。
 */
@Service
public class PredictUploadService {

    private final PredictUploadRepository predictUploadRepository;

    /**
     * 建構子，用於注入 PredictUploadRepository。
     * @param predictUploadRepository PredictUpload 資料的倉儲層介面。
     */
    @Autowired
    public PredictUploadService(PredictUploadRepository predictUploadRepository) {
        this.predictUploadRepository = predictUploadRepository;
    }

    public PredictUpload createPredictUpload(String customNo, String taskType, String inMonth, int income) {
        PredictUpload newPredictUpload = new PredictUpload();
        newPredictUpload.setCustomNo(customNo);
        newPredictUpload.setTaskType(taskType);
        newPredictUpload.setInMonth(inMonth);
        newPredictUpload.setIncome(income);
        return predictUploadRepository.save(newPredictUpload); // 使用 save 方法儲存到資料庫
    }
    /**
     * 從 Excel 檔案匯入 PredictUpload 資料。
     * 假設 Excel 檔案的欄位順序為：CustomNo, TaskType, InMonth, Income。
     * @param file 上傳的 Excel 檔案 (MultipartFile)。
     * @return 包含匯入結果訊息的列表。
     * @throws IOException 如果讀取檔案時發生 IO 錯誤。
     */
    public List<String> importPredictUploadsFromExcel(MultipartFile file) throws IOException {
        List<String> messages = new ArrayList<>();
        Workbook workbook = null;
        InputStream inputStream = file.getInputStream();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.toLowerCase().endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(inputStream); // 處理 .xlsx
        } else if (originalFilename != null && originalFilename.toLowerCase().endsWith(".xls")) {
            workbook = new HSSFWorkbook(inputStream); // 處理 .xls
        } else {
            messages.add("錯誤：不支援的檔案格式。請上傳 .xls 或 .xlsx 檔案。");
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // 記錄關閉流時的錯誤 (可選)
                }
            }
            return messages;
        }

        Sheet sheet = workbook.getSheetAt(0); // 假設資料在第一個工作表
        Iterator<Row> rowIterator = sheet.iterator();

        int rowNumber = 0;
        // 嘗試跳過標頭列 (如果 Excel 檔案有標頭)
        if (rowIterator.hasNext()) {
            rowIterator.next(); // 讀取並忽略第一行 (標頭)
            rowNumber++;
        }

        DataFormatter formatter = new DataFormatter(); // 用於將儲存格內容安全地格式化為字串

        while (rowIterator.hasNext()) {
            Row currentRow = rowIterator.next();
            rowNumber++;

            // 獲取儲存格，如果儲存格不存在或為空，則返回 null
            // 欄位順序：CustomNo (0), TaskType (1), InMonth (2), Income (3)
            Cell customNoCell = currentRow.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            Cell taskTypeCell = currentRow.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            Cell inMonthCell = currentRow.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            Cell incomeCell = currentRow.getCell(3, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

            String customNo = null;
            String taskType = null;
            String inMonth = null;
            int income = 0; // 預設值，如果 income 欄位為空或格式不正確

            if (customNoCell != null) {
                customNo = formatter.formatCellValue(customNoCell).trim();
            }
            if (taskTypeCell != null) {
                taskType = formatter.formatCellValue(taskTypeCell).trim();
            }
            if (inMonthCell != null) {
                inMonth = formatter.formatCellValue(inMonthCell).trim();
            }
            if (incomeCell != null) {
                try {
                    // 嘗試將 income 儲存格的值轉換為整數
                    // 如果儲存格是數字型別，直接獲取數字值可以避免 DataFormatter 可能帶來的格式問題 (如千分位符號)
                    if (incomeCell.getCellType() == CellType.NUMERIC) {
                        income = (int) incomeCell.getNumericCellValue();
                    } else {
                        String incomeStr = formatter.formatCellValue(incomeCell).trim();
                        if (!incomeStr.isEmpty()) {
                            income = Integer.parseInt(incomeStr);
                        }
                    }
                } catch (NumberFormatException e) {
                    messages.add("警告：第 " + rowNumber + " 行的 Income 欄位格式不正確，將使用預設值 0。原始值: '" + formatter.formatCellValue(incomeCell) + "'");
                    // 保持 income 為預設值 0
                }
            }

            // 基礎驗證：例如 customNo 不應為空
            if (customNo == null || customNo.isEmpty()) {
                messages.add("警告：第 " + rowNumber + " 行的 CustomNo 為空，已跳過。");
                continue; // 跳過此行，處理下一行
            }

            try {
                // 檢查記錄是否已存在 (例如，根據 customNo)
                // 這裡假設 customNo 是唯一的，如果不是，您需要調整判斷邏輯
                Optional<PredictUpload> existingRecord = predictUploadRepository.findByCustomNo(customNo);
                if (existingRecord.isPresent()) {
                    // 如果記錄已存在，您可以選擇更新它或跳過
                    // 此處範例為跳過
                    messages.add("警告：第 " + rowNumber + " 行的記錄 (CustomNo: " + customNo + ") 已存在，已跳過。");
                    continue;
                }

                PredictUpload newRecord = new PredictUpload();
                newRecord.setCustomNo(customNo);
                newRecord.setTaskType(taskType);
                newRecord.setInMonth(inMonth);
                newRecord.setIncome(income);

                predictUploadRepository.save(newRecord);
                messages.add("成功：第 " + rowNumber + " 行的記錄 (CustomNo: " + customNo + ") 已匯入。");

            } catch (Exception e) {
                // 在實際應用中，您可能想使用日誌框架記錄錯誤
                // logger.error("Error importing record with CustomNo {} at row {}: {}", customNo, rowNumber, e.getMessage());
                messages.add("錯誤：第 " + rowNumber + " 行的記錄 (CustomNo: " + customNo + ") 匯入失敗：" + e.getMessage());
            }
        }

        try {
            if (workbook != null) {
                workbook.close(); // 關閉工作簿以釋放資源
            }
        } catch (IOException e) {
            // 記錄關閉工作簿時的錯誤 (可選)
        }
        try {
            if (inputStream != null) {
                inputStream.close(); // 關閉輸入流
            }
        } catch (IOException e) {
            // 記錄關閉流時的錯誤 (可選)
        }


        return messages;
    }

    public Optional<PredictUpload> getPredictUploadByCustomNo(String customNo) {
         return predictUploadRepository.findByCustomNo(customNo);
    }
    public Optional<PredictUpload> getPredictUploadByTaskType(String taskType) {
         return predictUploadRepository.findByTaskType(taskType);
    }
    public Optional<PredictUpload> getPredictUploadByInMonth(String inmonth) {
         return predictUploadRepository.findByInMonth(inmonth);
    }

    /**
     * 根據查詢條件查找 PredictUpload 記錄。
     * @param criteria 查詢條件 DTO。
     * @return 符合條件的 PredictUpload 記錄列表。
     */
    public List<PredictUpload> findPredictUploadsByCriteria(PredictUploadQueryCriteria criteria) {
        // 使用 Specification 動態構建查詢條件
        return predictUploadRepository.findAll((Specification<PredictUpload>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getCustomNo() != null && !criteria.getCustomNo().isEmpty()) {
                // 模糊查詢，忽略大小寫
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("customNo")), "%" + criteria.getCustomNo().toLowerCase() + "%"));
            }
            if (criteria.getTaskType() != null && !criteria.getTaskType().isEmpty()) {
                // 精確查詢，忽略大小寫
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("taskType")), criteria.getTaskType().toLowerCase()));
            }
            if (criteria.getInMonth() != null && !criteria.getInMonth().isEmpty()) {
                // 精確查詢
                predicates.add(criteriaBuilder.equal(root.get("inMonth"), criteria.getInMonth()));
            }
            if (criteria.getMinIncome() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("income"), criteria.getMinIncome()));
            }
            if (criteria.getMaxIncome() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("income"), criteria.getMaxIncome()));
            }
            // 您可以在這裡加入更多基於 criteria 的條件

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }

    /**
     * 將符合查詢條件的 PredictUpload 記錄匯出為 Excel 檔案的位元組流。
     * @param criteria 查詢條件 DTO。
     * @return 包含 Excel 檔案內容的 ByteArrayInputStream。
     * @throws IOException 如果在生成 Excel 過程中發生 IO 錯誤。
     */
    public ByteArrayInputStream exportPredictUploadsToExcel(PredictUploadQueryCriteria criteria) throws IOException {
        List<PredictUpload> recordsToExport = findPredictUploadsByCriteria(criteria);

        String[] columns = {"ID", "CustomNo", "TaskType", "InMonth", "Income"}; // Excel 標頭
        try (
            Workbook workbook = new XSSFWorkbook(); // 創建 .xlsx 格式的工作簿
            ByteArrayOutputStream out = new ByteArrayOutputStream(); // 用於將工作簿內容寫入記憶體
        ) {
            Sheet sheet = workbook.createSheet("PredictUploads"); // 創建一個工作表

            // 創建標頭列
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                // 您可以在這裡為標頭儲存格添加樣式
            }

            // 填充資料列
            int rowIdx = 1;
            for (PredictUpload record : recordsToExport) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(record.getId() != null ? record.getId() : -1L); // 處理 ID 可能為 null 的情況
                row.createCell(1).setCellValue(record.getCustomNo());
                row.createCell(2).setCellValue(record.getTaskType());
                row.createCell(3).setCellValue(record.getInMonth());
                row.createCell(4).setCellValue(record.getIncome());
            }

            workbook.write(out); // 將工作簿內容寫入 ByteArrayOutputStream
            return new ByteArrayInputStream(out.toByteArray()); // 將 OutputStream 轉換為 InputStream
        }
    }
}