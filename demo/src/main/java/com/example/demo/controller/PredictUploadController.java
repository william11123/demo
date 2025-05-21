package com.example.demo.controller;

import com.example.demo.dto.UserCreatePredictUpload;
import com.example.demo.model.PredictUpload;
//import com.example.demo.model.User;
import com.example.demo.service.PredictUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
            logger.error("建立 PredictUpload 記錄時發生錯誤: {}", e.getMes***REMOVED***ge(), e);
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
            List<String> importMes***REMOVED***ges = predictUploadService.importPredictUploadsFromExcel(file);
            // 檢查是否有任何錯誤訊息，以決定 HTTP 狀態碼
            if (importMes***REMOVED***ges.stream().anyMatch(msg -> msg.startsWith("錯誤"))) {
                 // 如果有錯誤，但也有成功或警告，可以使用 MULTI_STATUS
                 // 如果所有都是錯誤，或者您想更明確地表示失敗，可以使用 BAD_REQUEST 或 INTERNAL_SERVER_ERROR
                 return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(importMes***REMOVED***ges);
            }
            return ResponseEntity.ok(importMes***REMOVED***ges);
        } catch (IOException e) {
            logger.error("處理檔案時發生 IO 錯誤: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of("處理檔案時發生錯誤：" + e.getMes***REMOVED***ge()));
        } catch (Exception e) {
            logger.error("匯入過程中發生未知錯誤: {}", file.getOriginalFilename(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of("匯入過程中發生未知錯誤：" + e.getMes***REMOVED***ge()));
        }
    }

    // --- 以下是一些 PredictUpload 實體可能的其他 CRUD 端點範例 ---

    /**
     * 建立新的 PredictUpload 記錄。
     * 端點: POST /api/predict-uploads
     * @param predictUpload 要建立的 PredictUpload 物件。
     * @return 建立的 PredictUpload 物件和 HTTP 狀態 201 (Created)。
     */
    // @PostMapping
    // public ResponseEntity<PredictUpload> createPredictUpload(@RequestBody PredictUpload predictUpload) {
    //     // 假設 PredictUploadService 中有 createPredictUpload 方法
    //     // PredictUpload newRecord = predictUploadService.createPredictUpload(predictUpload);
    //     // return new ResponseEntity<>(newRecord, HttpStatus.CREATED);
    //     // 如果您的 PredictUploadService 沒有此方法，您需要先在服務層實作它
    //     return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build(); // 暫時返回未實作
    // }

    /**
     * 根據 customNo 獲取 PredictUpload 記錄。
     * 端點: GET /api/predict-uploads/customNo/{customNo}
     * @param customNo 要查詢的 customNo。
     * @return 找到的 PredictUpload 物件或 HTTP 狀態 404 (Not Found)。
     */
    // @GetMapping("/customNo/{customNo}")
    // public ResponseEntity<PredictUpload> getPredictUploadByCustomNo(@PathVariable String customNo) {
    //     // 假設 PredictUploadService 中有 getPredictUploadByCustomNo 方法
    //     // Optional<PredictUpload> recordOptional = predictUploadService.getPredictUploadByCustomNo(customNo);
    //     // return recordOptional.map(record -> new ResponseEntity<>(record, HttpStatus.OK))
    //     //                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    //     return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build(); // 暫時返回未實作
    // }

    /**
     * 獲取所有 PredictUpload 記錄。
     * 端點: GET /api/predict-uploads
     * @return 所有 PredictUpload 記錄的列表。
     */
    // @GetMapping
    // public ResponseEntity<List<PredictUpload>> getAllPredictUploads() {
    //     // 假設 PredictUploadService 中有 getAllPredictUploads 方法
    //     // List<PredictUpload> records = predictUploadService.getAllPredictUploads();
    //     // return new ResponseEntity<>(records, HttpStatus.OK);
    //     return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build(); // 暫時返回未實作
    // }

    // 您可以根據需要加入 PUT (更新), DELETE (刪除) 等其他端點
}