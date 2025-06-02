package com.example.demo.controller;

import com.example.demo.dto.ProcessQueryDTO;
import com.example.demo.service.ProcessQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // 標記這是一個 RESTful Controller，並且其方法預設返回 @ResponseBody
@RequestMapping("/api/process") // 為這個 Controller 下的所有請求路徑設定一個基礎前綴
public class ProcessQueryController {

    private final ProcessQueryService processQueryService;

    // 透過建構子注入 Service
    @Autowired
    public ProcessQueryController(ProcessQueryService processQueryService) {
        this.processQueryService = processQueryService;
    }

    /**
     * 根據查詢參數獲取處理詳細資訊。
     * 例如：GET /api/process/details?queryValue=YOUR_SN_VALUE
     *
     * @param queryValue 用於查詢的 SN 值。
     * @return 包含 ProcessQueryDTO 列表的 ResponseEntity。
     */
    @GetMapping("/details") // 將 HTTP GET 請求映射到這個方法，路徑為 /api/process/details
    public ResponseEntity<List<ProcessQueryDTO>> getProcessDetailsByQuery(
            @RequestParam("queryValue") String queryValue) { // 從請求參數中獲取 "queryValue"

        if (queryValue == null || queryValue.trim().isEmpty()) {
            // 可以選擇返回錯誤請求或空列表，這裡返回錯誤請求示例
            return ResponseEntity.badRequest().build(); // 或者 ResponseEntity.ok(Collections.emptyList());
        }

        List<ProcessQueryDTO> details = processQueryService.getProcessDetails(queryValue);

        // ResponseEntity.ok() 會將結果包裝成 HTTP 200 OK 回應，
        // 並且 Spring MVC 會自動將 List<ProcessQueryDTO> 轉換為 JSON (如果 classpath 中有 Jackson 等函式庫)
        return ResponseEntity.ok(details);
    }
}