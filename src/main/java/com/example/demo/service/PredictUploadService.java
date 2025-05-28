package com.example.demo.service;

import com.example.demo.dto.PredictUploadQueryCriteria;
import com.example.demo.dto.PredictUploadImportData;
import com.example.demo.dto.UserCreatePredictUpload;
import com.example.demo.model.PredictUpload;
import com.example.demo.repository.PredictUploadRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
// import java.util.regex.Matcher; // Matcher 未在當前精簡版中使用，可考慮移除
// import java.util.regex.Pattern; // Pattern 未在當前精簡版中使用，可考慮移除

@Service
public class PredictUploadService {

    private static final Logger logger = LoggerFactory.getLogger(PredictUploadService.class);

    // 常數定義
    private static final int IMPORT_NEW_CUSTOM_NO_COL = 0;
    private static final int IMPORT_NEW_TASK_TYPE_COL = 1;
    private static final int IMPORT_NEW_IN_MONTH_COL = 2;
    private static final int IMPORT_NEW_INCOME_COL = 3;

    private static final int UPDATE_BANK_COL = 0;
    private static final int UPDATE_RECOGNITION_MONTH_COL = 1;
    private static final int UPDATE_OPERATION_TYPE_COL = 2;
    private static final int UPDATE_AMOUNT_COL = 3;

    private static final String[] EXCEL_EXTENSIONS = {".xlsx", ".xls"};
    private static final String[] EXPORT_COLUMNS = {"CustomNo", "TaskType", "InMonth", "Income"};
    private static final int DEFAULT_INCOME = 0;

    private final PredictUploadRepository predictUploadRepository;

    public PredictUploadService(PredictUploadRepository predictUploadRepository) {
        this.predictUploadRepository = predictUploadRepository;
    }

    // --- 核心 Excel 處理邏輯 ---

    @FunctionalInterface
    private interface ExcelRowProcessor {
        void process(Row row, DataFormatter formatter, int rowNumber, List<String> messages);
    }

    private List<String> processUploadedExcel(MultipartFile file, ExcelRowProcessor rowProcessor) throws IOException {
        List<String> messages = new ArrayList<>();
        String originalFilename = file.getOriginalFilename();

        if (!isValidExcelFile(originalFilename)) {
            messages.add("錯誤：不支援的檔案格式。請上傳 .xls 或 .xlsx 檔案。");
            return messages;
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = createWorkbook(inputStream, originalFilename)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                messages.add("錯誤：Excel 檔案中找不到工作表。");
                return messages;
            }
            Iterator<Row> rowIterator = sheet.iterator();
            skipHeaderRow(rowIterator);

            int rowNumber = 1; // Excel 行號從1開始，數據從第2行開始
            DataFormatter formatter = new DataFormatter();

            while (rowIterator.hasNext()) {
                Row currentRow = rowIterator.next();
                rowNumber++; // 代表實際 Excel 中的行號
                try {
                    rowProcessor.process(currentRow, formatter, rowNumber, messages);
                } catch (Exception e) {
                    messages.add("錯誤：處理第 " + rowNumber + " 行時發生未預期錯誤：" + e.getMessage());
                    logger.error("Unexpected error processing row " + rowNumber + " for file " + originalFilename, e);
                }
            }
        }
        if (messages.isEmpty() && file.getSize() > 0) { // 僅當檔案非空且無訊息時提示
            messages.add("資訊：檔案已處理，但沒有找到任何可操作的資料或所有資料行均不符合處理條件。");
        } else if (messages.isEmpty() && file.getSize() == 0) {
             messages.add("錯誤：上傳的檔案是空的。");
        }
        return messages;
    }

    // --- 模式一：僅匯入新記錄 ---

    public List<String> importPredictUploadsFromExcel(MultipartFile file) throws IOException {
        return processUploadedExcel(file, this::processRowForImportNew);
    }

    private void processRowForImportNew(Row row, DataFormatter formatter, int rowNumber, List<String> messages) {
        PredictUploadImportData importData = extractDataForImportNew(row, formatter, rowNumber, messages);

        if (importData == null) { // extractDataForImportNew 在嚴重錯誤時可能返回 null
            return;
        }
        
        if (importData.getCustomNo() == null || importData.getCustomNo().trim().isEmpty()) {
            messages.add("警告：第 " + rowNumber + " 行的 CustomNo 為空，已跳過。");
            return;
        }
        // 可選：根據業務邏輯添加對 taskType, inMonth 的必要性驗證

        UserCreatePredictUpload createDto = new UserCreatePredictUpload(
                importData.getCustomNo(),
                importData.getTaskType(),
                importData.getInMonth(),
                importData.getIncome() != null ? importData.getIncome() : DEFAULT_INCOME
        );

        // 假設唯一性由 customNo, taskType, inMonth 共同決定
        Optional<PredictUpload> existingRecordOpt = predictUploadRepository.findByCustomNoAndInMonthAndTaskType(
                createDto.getCustomNo(), createDto.getInMonth(), createDto.getTaskType()
        );

        if (existingRecordOpt.isPresent()) {
            messages.add("警告：第 " + rowNumber + " 行的記錄 (CustomNo: " + createDto.getCustomNo() +
                         ", TaskType: " + createDto.getTaskType() +
                         ", InMonth: " + createDto.getInMonth() + ") 已存在，已跳過。");
            return;
        }

        createPredictUpload(createDto);
        messages.add("成功：第 " + rowNumber + " 行的記錄 (CustomNo: " + createDto.getCustomNo() + ") 已匯入。");
    }

    private PredictUploadImportData extractDataForImportNew(Row row, DataFormatter formatter, int rowNumber, List<String> messages) {
        String customNo = getCellValue(row, IMPORT_NEW_CUSTOM_NO_COL, formatter);
        String taskType = getCellValue(row, IMPORT_NEW_TASK_TYPE_COL, formatter);
        String inMonth = getCellValue(row, IMPORT_NEW_IN_MONTH_COL, formatter);
        Integer income = getIntegerCellValue(row, IMPORT_NEW_INCOME_COL, formatter, rowNumber, messages, "Income");
        
        // 如果 income 解析失敗 (返回 null) 且業務上 income 是可選的或有預設值，這裡直接傳遞 null
        // DEFAULT_INCOME 的應用已移至 processRowForImportNew 中創建 DTO 的地方
        return new PredictUploadImportData(customNo, taskType, inMonth, income);
    }

    // --- 模式二：匯入並更新金額 ---

    @Transactional
    public List<String> importAndUpdateIncomeFromExcel(MultipartFile file) throws IOException {
        return processUploadedExcel(file, this::processRowForUpdateIncome);
    }

    private void processRowForUpdateIncome(Row row, DataFormatter formatter, int rowNumber, List<String> messages) {
        String bankNameExcel = getCellValue(row, UPDATE_BANK_COL, formatter);
        String inMonthExcel = getCellValue(row, UPDATE_RECOGNITION_MONTH_COL, formatter);
        String operationTypeExcel = getCellValue(row, UPDATE_OPERATION_TYPE_COL, formatter);
        Integer newIncome = getIntegerCellValue(row, UPDATE_AMOUNT_COL, formatter, rowNumber, messages, "金額(未稅)");

        if (isNullOrEmpty(bankNameExcel)) {
            messages.add("警告：第 " + rowNumber + " 行銀行名稱為空，已跳過。"); return;
        }
        if (isNullOrEmpty(inMonthExcel)) {
            messages.add("警告：第 " + rowNumber + " 行認列月份為空，已跳過。"); return;
        }
        if (isNullOrEmpty(operationTypeExcel)) {
            messages.add("警告：第 " + rowNumber + " 行作業別為空，已跳過。"); return;
        }
        if (newIncome == null) {
            // 訊息已在 getIntegerCellValue 中添加
            return;
        }

        String customNo = extractCustomNoFromBank(bankNameExcel);
        if (customNo == null) {
            messages.add("警告：第 " + rowNumber + " 行銀行名稱 (\"" + bankNameExcel + "\") 無法解析出 CustomNo，已跳過。"); return;
        }

        String taskTypeDB = mapOperationTypeToTaskType(operationTypeExcel);
        if (taskTypeDB == null) {
            messages.add("警告：第 " + rowNumber + " 行作業別 (\"" + operationTypeExcel + "\") 無法對應到 TaskType，已跳過。"); return;
        }

        Optional<PredictUpload> existingRecordOpt = predictUploadRepository.findByCustomNoAndInMonthAndTaskType(customNo, inMonthExcel, taskTypeDB);

        if (existingRecordOpt.isPresent()) {
            PredictUpload recordToUpdate = existingRecordOpt.get();
            int oldIncome = recordToUpdate.getIncome();
            recordToUpdate.setIncome(newIncome);
            predictUploadRepository.save(recordToUpdate);
            messages.add("成功：第 " + rowNumber + " 行 (CustomNo: " + customNo + ", InMonth: " + inMonthExcel + ", TaskType: " + taskTypeDB + ") 金額已從 " + oldIncome + " 更新為 " + newIncome + "。");
        } else {
            messages.add("警告：第 " + rowNumber + " 行未找到符合條件的記錄 (CustomNo: " + customNo + ", InMonth: " + inMonthExcel + ", TaskType: " + taskTypeDB + ")，未執行更新。");
        }
    }

    // --- Excel 處理輔助方法 ---

    private boolean isValidExcelFile(String filename) {
        if (filename == null) return false;
        String lowerFilename = filename.toLowerCase();
        for (String extension : EXCEL_EXTENSIONS) {
            if (lowerFilename.endsWith(extension)) return true;
        }
        return false;
    }

    private Workbook createWorkbook(InputStream inputStream, String filename) throws IOException {
        return filename.toLowerCase().endsWith(".xlsx") ? new XSSFWorkbook(inputStream) : new HSSFWorkbook(inputStream);
    }

    private void skipHeaderRow(Iterator<Row> rowIterator) {
        if (rowIterator.hasNext()) rowIterator.next();
    }

    private String getCellValue(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell != null ? formatter.formatCellValue(cell).trim() : null;
    }

    private Integer getIntegerCellValue(Row row, int cellIndex, DataFormatter formatter, int rowNumber, List<String> messages, String fieldNameForLog) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            messages.add("警告：第 " + rowNumber + " 行的 " + fieldNameForLog + " 欄位為空。");
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) { // 檢查是否為整數
                    return (int) numericValue;
                } else {
                    messages.add("警告：第 " + rowNumber + " 行的 " + fieldNameForLog + " 欄位 (" + numericValue + ") 不是有效的整數，將嘗試四捨五入。");
                    return (int) Math.round(numericValue); // 或其他處理方式
                }
            } else {
                String value = formatter.formatCellValue(cell).trim();
                if (value.isEmpty()) {
                    messages.add("警告：第 " + rowNumber + " 行的 " + fieldNameForLog + " 欄位為空字串。");
                    return null;
                }
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            messages.add("錯誤：第 " + rowNumber + " 行的 " + fieldNameForLog + " 欄位格式不正確 ('" + formatter.formatCellValue(cell) + "')，無法解析為數字。");
            return null;
        }
    }
    
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // --- 特定業務邏輯輔助方法 ---

    private String extractCustomNoFromBank(String bankName) {
        if (isNullOrEmpty(bankName)) {
            logger.warn("Bank name is null or empty, cannot extract CustomNo.");
            return null;
        }
        String trimmedBankName = bankName.trim();
        String[] parts = trimmedBankName.split("\\.", 2); // 按第一個 "." 分割

        if (parts.length > 0 && !parts[0].isEmpty()) {
            // 只要 "." 前面有內容，就將其視為 CustomNo
            // 您也可以在這裡添加更複雜的驗證邏輯，例如檢查是否只包含字母和數字
            // if (parts[0].matches("^[a-zA-Z0-9]+$")) { // 例如：只允許字母和數字
            //    return parts[0];
            // }
            logger.debug("Extracted CustomNo '{}' from bank name '{}'", parts[0], trimmedBankName);
            return parts[0];
        }
        
        logger.warn("無法從銀行名稱 '{}' 中提取有效的 CustomNo。分割後的 parts[0] 為空或 parts 長度不足。", trimmedBankName);
        return null;
    }

    private String mapOperationTypeToTaskType(String operationTypeExcel) {
        if (isNullOrEmpty(operationTypeExcel)) return null;
        String trimmedOpType = operationTypeExcel.trim();
        if ("維護".equalsIgnoreCase(trimmedOpType)) return "1";
        if ("作業".equalsIgnoreCase(trimmedOpType)) return "2";
        logger.warn("無法將作業別 '{}' 映射到已知的 TaskType。", operationTypeExcel);
        return null;
    }

    // --- 資料庫操作方法 ---

    public PredictUpload createPredictUpload(UserCreatePredictUpload createDto) {
        PredictUpload newPredictUpload = new PredictUpload();
        newPredictUpload.setCustomNo(createDto.getCustomNo());
        newPredictUpload.setTaskType(createDto.getTaskType());
        newPredictUpload.setInMonth(createDto.getInMonth());
        newPredictUpload.setIncome(createDto.getIncome());
        return predictUploadRepository.save(newPredictUpload);
    }
    
    // 保留舊的 createPredictUpload 以兼容，但建議逐步淘汰
    public PredictUpload createPredictUpload(String customNo, String taskType, String inMonth, int income) {
        UserCreatePredictUpload createDto = new UserCreatePredictUpload(customNo, taskType, inMonth, income);
        return createPredictUpload(createDto);
    }

    public List<PredictUpload> findPredictUploadsByCriteria(PredictUploadQueryCriteria criteria) {
        return predictUploadRepository.findAll(createSpecification(criteria));
    }

    private Specification<PredictUpload> createSpecification(PredictUploadQueryCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            addExactCondition(predicates, criteriaBuilder, root, "customNo", criteria.getCustomNo());
            addExactCondition(predicates, criteriaBuilder, root, "taskType", criteria.getTaskType());
            addExactCondition(predicates, criteriaBuilder, root, "inMonth", criteria.getInMonth());
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void addExactCondition(List<Predicate> predicates, CriteriaBuilder cb, Root<PredictUpload> root, String fieldName, String value) {
        if (!isNullOrEmpty(value)) {
            predicates.add(cb.equal(root.get(fieldName), value.trim()));
        }
    }
    
    // --- 匯出 Excel 相關方法 (保持不變) ---
    public ByteArrayInputStream exportPredictUploadsToExcel(PredictUploadQueryCriteria criteria) throws IOException {
        List<PredictUpload> recordsToExport = findPredictUploadsByCriteria(criteria);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("PredictUploads");
            createHeaderRow(workbook, sheet);
            fillDataRows(sheet, recordsToExport);
            autoSizeColumns(sheet, EXPORT_COLUMNS.length);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    private void createHeaderRow(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int col = 0; col < EXPORT_COLUMNS.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(EXPORT_COLUMNS[col]);
            cell.setCellStyle(headerStyle);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void fillDataRows(Sheet sheet, List<PredictUpload> records) {
        int rowIdx = 1;
        for (PredictUpload record : records) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(record.getCustomNo() != null ? record.getCustomNo() : "");
            row.createCell(1).setCellValue(record.getTaskType() != null ? record.getTaskType() : "");
            row.createCell(2).setCellValue(record.getInMonth() != null ? record.getInMonth() : "");
            row.createCell(3).setCellValue(record.getIncome());
        }
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int col = 0; col < columnCount; col++) {
            sheet.autoSizeColumn(col);
        }
    }
    
    // 可選：如果您在 Repository 中沒有這些方法，需要添加
    public Optional<PredictUpload> getPredictUploadByCustomNo(String customNo) {
        if (isNullOrEmpty(customNo)) { // isNullOrEmpty 是您服務中已有的輔助方法
            return Optional.empty();
        }
        // 呼叫 Repository 的方法
        return predictUploadRepository.findByCustomNo(customNo.trim());
    }    // public List<PredictUpload> getPredictUploadsByTaskType(String taskType) { ... }
    // public List<PredictUpload> getPredictUploadsByInMonth(String inmonth) { ... }
}