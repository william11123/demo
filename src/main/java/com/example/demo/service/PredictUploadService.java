package com.example.demo.service;

import com.example.demo.dto.PredictUploadQueryCriteria;
import com.example.demo.dto.PredictUploadImportData;  // 🆕 新增匯入
import com.example.demo.dto.UserCreatePredictUpload;   // 🆕 新增匯入
import com.example.demo.model.PredictUpload;
import com.example.demo.repository.PredictUploadRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
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

/**
 * PredictUpload 實體的服務層。
 * 負責處理與 PredictUpload 相關的業務邏輯，例如從 Excel 匯入資料。
 */
@Service
public class PredictUploadService {

    // 常數定義 - 避免魔術數字
    private static final int CUSTOM_NO_COLUMN = 0;
    private static final int TASK_TYPE_COLUMN = 1;
    private static final int IN_MONTH_COLUMN = 2;
    private static final int INCOME_COLUMN = 3;
    private static final String[] EXCEL_EXTENSIONS = {".xlsx", ".xls"};
    private static final String[] EXPORT_COLUMNS = {"CustomNo", "TaskType", "InMonth", "Income"};
    private static final int DEFAULT_INCOME = 0;

    private final PredictUploadRepository predictUploadRepository;

    /**
     * 建構子注入 - Spring 4.3+ 不需要 @Autowired
     */
    public PredictUploadService(PredictUploadRepository predictUploadRepository) {
        this.predictUploadRepository = predictUploadRepository;
    }

    /**
     * 創建新的 PredictUpload 記錄 - 🔄 使用 DTO
     */
    public PredictUpload createPredictUpload(UserCreatePredictUpload createDto) {
        PredictUpload newPredictUpload = new PredictUpload();
        newPredictUpload.setCustomNo(createDto.getCustomNo());
        newPredictUpload.setTaskType(createDto.getTaskType());
        newPredictUpload.setInMonth(createDto.getInMonth());
        newPredictUpload.setIncome(createDto.getIncome());
        return predictUploadRepository.save(newPredictUpload);
    }

    /**
     * 創建新的 PredictUpload 記錄 - 保持向後兼容
     */
    public PredictUpload createPredictUpload(String customNo, String taskType, String inMonth, int income) {
        UserCreatePredictUpload createDto = new UserCreatePredictUpload(customNo, taskType, inMonth, income);
        return createPredictUpload(createDto);
    }

    /**
     * 從 Excel 檔案匯入 PredictUpload 資料 - 優化版本
     * 使用 try-with-resources 自動管理資源
     */
    public List<String> importPredictUploadsFromExcel(MultipartFile file) throws IOException {
        List<String> messages = new ArrayList<>();
        
        String originalFilename = file.getOriginalFilename();
        if (!isValidExcelFile(originalFilename)) {
            messages.add("錯誤：不支援的檔案格式。請上傳 .xls 或 .xlsx 檔案。");
            return messages;
        }

        // 使用 try-with-resources 自動管理資源
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = createWorkbook(inputStream, originalFilename)) {
            
            return processExcelSheet(workbook, messages);
        }
    }

    /**
     * 驗證 Excel 檔案格式
     */
    private boolean isValidExcelFile(String filename) {
        if (filename == null) return false;
        
        String lowerFilename = filename.toLowerCase();
        for (String extension : EXCEL_EXTENSIONS) {
            if (lowerFilename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根據檔案類型創建對應的 Workbook
     */
    private Workbook createWorkbook(InputStream inputStream, String filename) throws IOException {
        if (filename.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else {
            return new HSSFWorkbook(inputStream);
        }
    }

    /**
     * 處理 Excel 工作表 - 拆分後的方法，職責更單一
     */
    private List<String> processExcelSheet(Workbook workbook, List<String> messages) {
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        
        // 跳過標頭列
        skipHeaderRow(rowIterator);
        
        int rowNumber = 1;
        DataFormatter formatter = new DataFormatter();
        
        while (rowIterator.hasNext()) {
            Row currentRow = rowIterator.next();
            rowNumber++;
            processRow(currentRow, formatter, rowNumber, messages);
        }
        
        return messages;
    }

    /**
     * 跳過標頭列
     */
    private void skipHeaderRow(Iterator<Row> rowIterator) {
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }
    }

    /**
     * 處理單一行資料 - 🔄 使用新的匯入 DTO
     */
    private void processRow(Row row, DataFormatter formatter, int rowNumber, List<String> messages) {
        try {
            PredictUploadImportData importData = extractDataFromRow(row, formatter, rowNumber, messages);
            if (importData != null && validateImportData(importData, rowNumber, messages)) {
                saveImportRecord(importData, rowNumber, messages);
            }
        } catch (Exception e) {
            messages.add("錯誤：第 " + rowNumber + " 行處理失敗：" + e.getMessage());
        }
    }

    /**
     * 從行中提取資料 - 🔄 返回匯入 DTO
     */
    private PredictUploadImportData extractDataFromRow(Row row, DataFormatter formatter, int rowNumber, List<String> messages) {
        String customNo = getCellValue(row, CUSTOM_NO_COLUMN, formatter);
        String taskType = getCellValue(row, TASK_TYPE_COLUMN, formatter);
        String inMonth = getCellValue(row, IN_MONTH_COLUMN, formatter);
        Integer income = getIntegerCellValue(row, INCOME_COLUMN, formatter, rowNumber, messages);
        
        return new PredictUploadImportData(customNo, taskType, inMonth, income);
    }

    /**
     * 獲取儲存格字串值
     */
    private String getCellValue(Row row, int cellIndex, DataFormatter formatter) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell != null ? formatter.formatCellValue(cell).trim() : null;
    }

    /**
     * 獲取儲存格整數值
     */
    private Integer getIntegerCellValue(Row row, int cellIndex, DataFormatter formatter, int rowNumber, List<String> messages) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return DEFAULT_INCOME;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else {
                String value = formatter.formatCellValue(cell).trim();
                return value.isEmpty() ? DEFAULT_INCOME : Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            messages.add("警告：第 " + rowNumber + " 行的 Income 欄位格式不正確，將使用預設值 " + DEFAULT_INCOME + "。");
            return DEFAULT_INCOME;
        }
    }

    /**
     * 驗證匯入資料 - 🔄 使用匯入 DTO
     */
    private boolean validateImportData(PredictUploadImportData importData, int rowNumber, List<String> messages) {
        if (!importData.isValid()) {
            messages.add("警告：第 " + rowNumber + " 行的 CustomNo 為空，已跳過。");
            return false;
        }
        return true;
    }

    /**
     * 儲存匯入記錄 - 🔄 使用匯入 DTO 和轉換邏輯
     */
    private void saveImportRecord(PredictUploadImportData importData, int rowNumber, List<String> messages) {
        try {
            // 轉換為創建 DTO
            UserCreatePredictUpload createDto = importData.toCreateDto();
            if (createDto == null) {
                messages.add("警告：第 " + rowNumber + " 行的資料不完整，已跳過。");
                return;
            }

            // 檢查記錄是否已存在
            Optional<PredictUpload> existingRecord = predictUploadRepository.findByCustomNo(createDto.getCustomNo());
            if (existingRecord.isPresent()) {
                messages.add("警告：第 " + rowNumber + " 行的記錄 (CustomNo: " + createDto.getCustomNo() + ") 已存在，已跳過。");
                return;
            }

            // 使用創建方法儲存
            PredictUpload savedRecord = createPredictUpload(createDto);
            messages.add("成功：第 " + rowNumber + " 行的記錄 (CustomNo: " + savedRecord.getCustomNo() + ") 已匯入。");

        } catch (Exception e) {
            messages.add("錯誤：第 " + rowNumber + " 行的記錄匯入失敗：" + e.getMessage());
        }
    }

    // 原有的查詢方法
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
     * 根據查詢條件查找 PredictUpload 記錄 - 優化版本
     */
    public List<PredictUpload> findPredictUploadsByCriteria(PredictUploadQueryCriteria criteria) {
        return predictUploadRepository.findAll(createSpecification(criteria));
    }

/**
 * 創建動態查詢 Specification - 簡化版本（僅精確搜尋）
 */
private Specification<PredictUpload> createSpecification(PredictUploadQueryCriteria criteria) {
    return (root, query, criteriaBuilder) -> {
        List<Predicate> predicates = new ArrayList<>();
        
        // 所有條件都使用精確搜尋
        addExactCondition(predicates, criteriaBuilder, root, "customNo", criteria.getCustomNo());
        addExactCondition(predicates, criteriaBuilder, root, "taskType", criteria.getTaskType());
        addExactCondition(predicates, criteriaBuilder, root, "inMonth", criteria.getInMonth());
        
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
}

/**
 * 精確條件添加方法 - 簡化版本
 */
private void addExactCondition(List<Predicate> predicates, CriteriaBuilder cb,
                              Root<PredictUpload> root, String fieldName, String value) {
    if (value != null && !value.trim().isEmpty()) {
        String normalizedValue = value.trim();
        predicates.add(cb.equal(root.get(fieldName), normalizedValue));
    }
}

    // ... 其他方法保持不變 (匯出 Excel 相關方法)
    /**
     * 匯出 Excel - 優化版本，增加樣式和更好的錯誤處理
     */
    public ByteArrayInputStream exportPredictUploadsToExcel(PredictUploadQueryCriteria criteria) throws IOException {
        List<PredictUpload> recordsToExport = findPredictUploadsByCriteria(criteria);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("PredictUploads");
            
            // 創建並設置標頭列
            createHeaderRow(workbook, sheet);
            
            // 填充資料列
            fillDataRows(sheet, recordsToExport);
            
            // 自動調整欄寬
            autoSizeColumns(sheet, EXPORT_COLUMNS.length);
            
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    /**
     * 創建標頭列
     */
    private void createHeaderRow(Workbook workbook, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        
        for (int col = 0; col < EXPORT_COLUMNS.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(EXPORT_COLUMNS[col]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * 創建標頭樣式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 填充資料列
     */
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

    /**
     * 自動調整欄寬
     */
    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int col = 0; col < columnCount; col++) {
            sheet.autoSizeColumn(col);
        }
    }
}