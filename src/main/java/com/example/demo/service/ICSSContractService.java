package com.example.demo.service;

import com.example.demo.model.ICSSContract;
import com.example.demo.model.ICSSContractId;
import com.example.demo.repository.ICSSContractRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ICSSContractService {

    private static final Logger logger = LoggerFactory.getLogger(ICSSContractService.class);

    private final ICSSContractRepository icssContractRepository;
    // 【更新1】: 將 DataFormatter 提升為成員變數，以供全類別使用並避免重複建立。
    private final DataFormatter dataFormatter = new DataFormatter();

    @Autowired
    public ICSSContractService(ICSSContractRepository icssContractRepository) {
        this.icssContractRepository = icssContractRepository;
    }

    /**
     * 從上傳的 Excel 檔案匯入合約資料。
     *
     * @param file 使用者上傳的 MultipartFile 檔案。
     * @return 一個包含處理結果訊息的列表。
     */
    public List<String> importContractsFromExcel(MultipartFile file) {
        List<String> messages = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream()) {
            
            // 【更新2】: 動態偵測 .xls 和 .xlsx 檔案，並使用對應的 Workbook 實作。
            Workbook workbook;
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.toLowerCase().endsWith(".xls")) {
                workbook = new HSSFWorkbook(inputStream); // 用於處理舊版 .xls
            } else if (fileName != null && fileName.toLowerCase().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream); // 用於處理新版 .xlsx
            } else {
                messages.add("嚴重錯誤：不支援的檔案格式。請上傳 .xls 或 .xlsx 檔案。");
                return messages;
            }

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // 跳過標頭行
            if (rows.hasNext()) {
                rows.next();
            }

            int rowNumber = 1;

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                rowNumber++;
                try {
                    // --- 【更新3】: 處理複合主鍵 ---
             
                    String contractNo = dataFormatter.formatCellValue(currentRow.getCell(3)).trim();
                    String startDate = dataFormatter.formatCellValue(currentRow.getCell(5)).trim();
                    String contractName = dataFormatter.formatCellValue(currentRow.getCell(2)).trim();

                    if (contractNo.isEmpty() && contractName.isEmpty()) {
                        messages.add("警告：第 " + rowNumber + " 行的合約編號和合約名稱均為空，已跳過。");
                        continue;
                    }

                    ICSSContractId contractId = new ICSSContractId(contractNo, contractName, startDate);
                    ICSSContract contract = new ICSSContract(contractId);
                    
                    // --- 讀取其他所有欄位 ---
                    contract.setCustomNo(dataFormatter.formatCellValue(currentRow.getCell(0)));
                    contract.setInvoiceName(dataFormatter.formatCellValue(currentRow.getCell(1)));
                    contract.setBinCode(dataFormatter.formatCellValue(currentRow.getCell(4)));
                    
                    // --- 【更新4】: 將日期直接讀取為文字，以符合 Model 的 String 型別 ---
                    contract.setEndDate(dataFormatter.formatCellValue(currentRow.getCell(6)));
                    
                    contract.setContractAmt(getBigDecimalFromCell(currentRow.getCell(7)));
                    contract.setNote(dataFormatter.formatCellValue(currentRow.getCell(8)));
                    contract.setSales(dataFormatter.formatCellValue(currentRow.getCell(9)));
                    contract.setTermSalesMail(dataFormatter.formatCellValue(currentRow.getCell(10)));

                    // 儲存到資料庫 (JPA 的 save 方法會自動處理新增或更新)
                    icssContractRepository.save(contract);
                    messages.add("成功：第 " + rowNumber + " 行的合約 [" + contractId.getContractNo() + " / " + contractId.getContractName() + "] 已成功匯入/更新。");

                } catch (Exception e) {
                    logger.error("處理第 " + rowNumber + " 行時發生錯誤", e);
                    messages.add("錯誤：處理第 " + rowNumber + " 行時失敗：" + e.getMessage());
                }
            }
            if (messages.isEmpty() && rowNumber > 1) {
                 messages.add("資訊：檔案已成功處理，但所有資料行都因格式問題被跳過。");
            } else if (messages.isEmpty()) {
                messages.add("資訊：檔案已成功處理，但未找到任何可匯入的資料行。");
            }
        } catch (Exception e) {
            logger.error("匯入 Excel 檔案時發生嚴重錯誤", e);
            messages.add("嚴重錯誤：無法處理 Excel 檔案。請檢查檔案格式是否正確。錯誤：" + e.getMessage());
        }
        return messages;
    }
    
    /**
     * 從儲存格中安全地獲取 BigDecimal。
     */
    private BigDecimal getBigDecimalFromCell(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        
        // --- 【更新5】: 修正了讀取金額的錯誤，統一使用 DataFormatter ---
        String cellValue = dataFormatter.formatCellValue(cell).trim();

        if (cellValue.isEmpty()) {
            return null;
        }

        try {
            return new BigDecimal(cellValue.replace(",", ""));
        } catch (NumberFormatException e) {
            logger.warn("無法將儲存格值 '{}' 解析為 BigDecimal", cellValue);
            return null;
        }
    }
}
