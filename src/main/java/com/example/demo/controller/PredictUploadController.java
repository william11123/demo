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
import java.util.ArrayList;
import java.util.Date; // <--- 新增匯入
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors; 

// 修正後的 JPA 相關 import
import jakarta.persistence.EntityManager;      // ✅ 使用 jakarta
import jakarta.persistence.PersistenceContext; // ✅ 使用 jakarta
import jakarta.persistence.Query;              // ✅ 使用 jakarta
@RestController
@RequestMapping("/api/predict-uploads") // 所有此控制器的端點都會以 /api/predict-uploads 開頭
public class PredictUploadController {

    private static final Logger logger = LoggerFactory.getLogger(PredictUploadController.class);
    private final PredictUploadService predictUploadService;

    @Autowired
    public PredictUploadController(PredictUploadService predictUploadService) {
        this.predictUploadService = predictUploadService;
    }
    @PersistenceContext
    private EntityManager entityManager; // Spring 自動注入
    
    @GetMapping("")  // 對應 /api/predict-uploads 的 GET 請求
    public ResponseEntity<List<Map<String, Object>>> getAllWithConditions(
            @RequestParam(required = false) String customNo,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String inMonth) {
        
        try {
            // 使用原生 SQL 查詢
            String sql = "SELECT customNo, taskType, inMonth, income FROM bankincome WHERE 1=1";
            List<Object> params = new ArrayList<>();
            
            if (customNo != null && !customNo.isEmpty()) {
                sql += " AND customNo = ?";
                params.add(customNo);
            }
            if (taskType != null && !taskType.isEmpty()) {
                sql += " AND taskType = ?";
                params.add(taskType);
            }
            if (inMonth != null && !inMonth.isEmpty()) {
                sql += " AND inMonth = ?";
                params.add(inMonth);
            }
            
            // 執行原生 SQL
            Query query = entityManager.createNativeQuery(sql);
            for (int i = 0; i < params.size(); i++) {
                query.setParameter(i + 1, params.get(i));
            }
            
            List<Object[]> results = query.getResultList();
            
            // 轉換結果為 Map 格式（保持與前端相容）
            List<Map<String, Object>> responseList = results.stream()
                    .map(row -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("customNo", row[0]);
                        map.put("taskType", row[1]);
                        map.put("inMonth", row[2]);
                        map.put("income", row[3]);
                        return map;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responseList);
            
        } catch (Exception e) {
            logger.error("查詢時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping
    public ResponseEntity<PredictUpload> createManualPredictUpload(@RequestBody UserCreatePredictUpload request) {
        try {
            // 從 DTO 中獲取資料並呼叫服務層方法
            PredictUpload newPredictUpload = predictUploadService.createPredictUpload(request);
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
     * 根據查詢條件搜尋記錄 - 新增端點
     * 端點: GET /api/predict-uploads/search?customNo=ABC&taskType=TypeA&inMonth=2024-01
    
    @GetMapping("/search")
    public ResponseEntity<List<PredictUpload>> searchPredictUploads(
            @RequestParam(required = false) String customNo,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) String inMonth) {
        try {
            PredictUploadQueryCriteria criteria = new PredictUploadQueryCriteria(customNo, taskType, inMonth);
            List<PredictUpload> results = predictUploadService.findPredictUploadsByCriteria(criteria);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("搜尋 PredictUpload 記錄時發生錯誤", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
 */
    /**
     * 根據客戶編號查詢單一記錄 - 新增端點
     * 端點: GET /api/predict-uploads/by-custom-no/{customNo}
     */
    @GetMapping("/by-custom-no/{customNo}")
    public ResponseEntity<PredictUpload> getPredictUploadByCustomNo(@PathVariable String customNo) {
        try {
            Optional<PredictUpload> result = predictUploadService.getPredictUploadByCustomNo(customNo);
            return result.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("查詢 PredictUpload 記錄時發生錯誤: customNo={}", customNo, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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