package com.example.demo.controller;

import com.example.demo.dto.UserCreatePredictUpload;
import com.example.demo.dto.PredictUploadQueryCriteria; // <--- 新增匯入
import com.example.demo.model.PredictUpload;
//import com.example.demo.model.User;
import com.example.demo.service.PredictUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource; // <--- 新增匯入
import org.springframework.http.HttpHeaders; // <--- 新增匯入
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // <--- 新增匯入
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream; // <--- 新增匯入
import java.io.IOException;
import java.text.SimpleDateFormat; // <--- 新增匯入
import java.util.Date; // <--- 新增匯入
import java.util.List;

@RestController
@RequestMapping("/api/predict-uploads") // 所有此控制器的端點都會以 /api/predict-uploads 開頭
public class PredictUploadController {

    private static final Logger logger = LoggerFactory.getLogger(PredictUploadController.class);
    private final PredictUploadService predictUploadService;

    @Autowired
    public PredictUploadController(PredictUploadService predictUploadService) {
        this.predictUploadService = predictUploadService;
    }
    @PostMapping
    public ResponseEntity<PredictUpload> createManualPredictUpload(@RequestBody UserCreatePredictUpload request) {
        try {
            // 從 DTO 中獲取資料並呼叫服務層方法
            PredictUpload newPredictUpload = predictUploadService.createPredictUpload(
                    request.getCustomNo(),
                    request.getTaskType(), // DTO 中的 taskType 是可選的，如果為 null 會直接傳遞 null
                    request.getInMonth(),  // DTO 中的 inMonth 是可選的，如果為 null 會直接傳遞 null
                    request.getIncome()
            );
            return new ResponseEntity<>(newPredictUpload, HttpStatus.CREATED);
        } catch (Exception e) {
            // 考慮更細緻的錯誤處理，例如，如果 customNo 已存在，服務層可以拋出特定例外
            logger.error("建立 PredictUpload 記錄時發生錯誤: {}", e.getMessage(), e);
            // 這裡可以回傳更具體的錯誤訊息給客戶端，而不是通用的 INTERNAL_SERVER_ERROR
            // 例如，如果 customNo 重複，可以回傳 HttpStatus.CONFLICT
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(null); // 或者一個包含錯誤訊息的 DTO
        }
    }
    /**
     * 從 Excel 檔案匯入 PredictUpload 資料。
     * 端點: POST /api/predict-uploads/upload-excel
     * @param file 上傳的 Excel 檔案。
     * @return 包含匯入結果訊息的列表。
     */
    @PostMapping("/upload-excel")
    public ResponseEntity<List<String>> uploadPredictDataFromExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of("請選擇一個檔案上傳。"));
        }

        // 檢查檔案類型是否為 Excel (可選，但建議)
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        boolean isExcel = false;
        if (originalFilename != null && (originalFilename.toLowerCase().endsWith(".xls") || originalFilename.toLowerCase().endsWith(".xlsx"))) {
            isExcel = true;
        } else if (contentType != null && (contentType.equals("application/vnd.ms-excel") || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))) {
            isExcel = true;
        }

        if (!isExcel) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of("檔案格式不正確，請上傳 Excel 檔案 (.xls 或 .xlsx)。"));
        }

        try {
            List<String> importMessages = predictUploadService.importPredictUploadsFromExcel(file);
            // 檢查是否有任何錯誤訊息，以決定 HTTP 狀態碼
            if (importMessages.stream().anyMatch(msg -> msg.startsWith("錯誤"))) {
                 // 如果有錯誤，但也有成功或警告，可以使用 MULTI_STATUS
                 // 如果所有都是錯誤，或者您想更明確地表示失敗，可以使用 BAD_REQUEST 或 INTERNAL_SERVER_ERROR
                 return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(importMessages);
            }
            return ResponseEntity.ok(importMessages);
        } catch (IOException e) {
            logger.error("處理檔案時發生 IO 錯誤: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of("處理檔案時發生錯誤：" + e.getMessage()));
        } catch (Exception e) {
            logger.error("匯入過程中發生未知錯誤: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of("匯入過程中發生未知錯誤：" + e.getMessage()));
        }
    }

    /**
     * 根據查詢條件匯出 PredictUpload 資料到 Excel 檔案。
     * 端點: GET /api/predict-uploads/export
     * @param criteria 查詢條件，從請求參數中獲取。
     * @return 包含 Excel 檔案的 ResponseEntity，供瀏覽器下載。
     */
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportPredictUploads(
            @ModelAttribute PredictUploadQueryCriteria criteria // Spring 會自動將請求參數對應到 DTO 的欄位
    ) {
        try {
            ByteArrayInputStream bis = predictUploadService.exportPredictUploadsToExcel(criteria);

            HttpHeaders headers = new HttpHeaders();
            // 產生帶有時間戳的檔案名稱
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "predict_uploads_" + timestamp + ".xlsx";
            // 設定 Content-Disposition 標頭，使瀏覽器觸發下載
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    // 設定正確的 Content-Type 以識別 Excel 檔案
                    .contentType(MediaType.APPLICATION_OCTET_STREAM) // 通用二進位流
                    // 或者更精確的 .xlsx 類型:
                    // .contentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(bis));

        } catch (IOException e) {
            logger.error("匯出 PredictUpload 資料到 Excel 時發生 IO 錯誤", e);
            // 可以回傳一個錯誤訊息的 JSON 或一個空的 500 回應
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(null); // 或者一個錯誤訊息的 DTO
        } catch (Exception e) {
            // 捕獲其他可能的未知錯誤
            logger.error("匯出 PredictUpload 資料時發生未知錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(null);
        }
    }


    // --- 以下是一些 PredictUpload 實體可能的其他 CRUD 端點範例 ---
    // ... (保持您現有的被註解掉的 CRUD 方法) ...
}