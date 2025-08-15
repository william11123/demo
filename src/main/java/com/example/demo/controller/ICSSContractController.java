package com.example.demo.controller;

import com.example.demo.service.ICSSContractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/icss-contracts")
@CrossOrigin(origins = "*") // 允許所有來源的跨域請求，方便前端開發
public class ICSSContractController {

    private static final Logger logger = LoggerFactory.getLogger(ICSSContractController.class);

    private final ICSSContractService icssContractService;

    @Autowired
    public ICSSContractController(ICSSContractService icssContractService) {
        this.icssContractService = icssContractService;
    }

    /**
     * 提供一個 API 端點來接收上傳的 Excel 檔案並觸發匯入流程。
     * 端點: POST /api/icss-contracts/upload
     *
     * @param file 前端上傳的 Excel 檔案 (請求中的 part name 必須是 "file")。
     * @return 包含處理結果訊息列表的 ResponseEntity。
     */
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadContractFile(@RequestParam("file") MultipartFile file) {
        // 檢查檔案是否為空
        if (file.isEmpty()) {
            logger.warn("上傳請求失敗：檔案為空。");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of("錯誤：請選擇一個檔案上傳。"));
        }

        // 檢查檔案類型 (可選但建議)
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".xls") && !fileName.toLowerCase().endsWith(".xlsx"))) {
             logger.warn("上傳請求失敗：檔案類型不正確 - {}", fileName);
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of("錯誤：檔案格式不正確，請上傳 Excel 檔案 (.xls 或 .xlsx)。"));
        }

        try {
            logger.info("開始處理合約匯入，檔案名稱：{}", file.getOriginalFilename());
            // 呼叫 Service 層的核心方法來處理檔案
            List<String> messages = icssContractService.importContractsFromExcel(file);
            
            // 檢查是否有任何錯誤訊息，以決定 HTTP 狀態碼
            boolean hasErrors = messages.stream().anyMatch(msg -> msg.toLowerCase().contains("錯誤"));
            
            if (hasErrors) {
                logger.warn("檔案 {} 匯入完成，但包含錯誤。", file.getOriginalFilename());
                // 使用 207 Multi-Status 表示部分成功部分失敗
                return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(messages);
            }
            
            logger.info("檔案 {} 已成功匯入。", file.getOriginalFilename());
            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            logger.error("處理檔案 {} 時發生未預期的嚴重錯誤。", file.getOriginalFilename(), e);
            // 如果 Service 層拋出未捕獲的例外，回傳 500 伺服器內部錯誤
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of("伺服器內部錯誤，請聯繫管理員。"));
        }
    }
}
