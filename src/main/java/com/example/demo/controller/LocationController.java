package com.example.demo.controller;

// 1. 匯入 Logger 相關類別
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.demo.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    // 2. 建立一個 Logger 實例
    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    private final LocationService locationService;

    // 為了簡化範例，我們在記憶體中儲存使用者位置。
    // 在真實應用中，您應該將其存入資料庫。
    // Map<UserId, LocationData>
    private static final Map<String, Map<String, Double>> userLocations = new ConcurrentHashMap<>();

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * 更新或記錄使用者的目前位置。
     * Flutter App 會呼叫這個 API。
     */
    @PostMapping("/update/{userId}")
    public ResponseEntity<Void> updateUserLocation(@PathVariable String userId, @RequestBody Map<String, Double> location) {
        userLocations.put(userId, location);
        System.out.println("更新使用者 " + userId + " 位置: " + location);
        return ResponseEntity.ok().build();
    }

    /**
     * 處理使用者的到達請求。
     * Flutter App 會在使用者按下按鈕時呼叫這個 API。
     */
    @PostMapping("/arrive/{userId}")
    public ResponseEntity<Map<String, String>> handleArrivalRequest(@PathVariable String userId, @RequestBody Map<String, Double> location) {// <-- 將回傳類型從 String 改為 Map
        final double TARGET_LAT = 37.42200;
        final double TARGET_LON = -122.08400;

        double userLat = location.get("latitude");
        double userLon = location.get("longitude");

        boolean isApproved = locationService.processArrivalRequest(userLat, userLon, TARGET_LAT, TARGET_LON);

        // 建立一個 Map 來存放要回傳的訊息
        Map<String, String> response = new java.util.HashMap<>();

        if (isApproved) {
            logger.info("使用者 {} 的到達請求已核准！", userId);
            response.put("mes***REMOVED***ge", "請求已核准：您在目標範圍內。");
            return ResponseEntity.ok(response); // <-- 回傳 Map 物件
        } else {
            logger.warn("使用者 {} 的到達請求被拒絕。", userId);
            response.put("mes***REMOVED***ge", "請求被拒絕：您不在目標範圍內。");
            // 注意：對於業務邏輯上的拒絕，我們仍然可以回傳 200 OK，
            // 只是在 JSON 內容中告知結果，這樣前端比較好處理。
            // 當然，您也可以維持 badRequest()，取決於您的 API 設計風格。
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 讓管理者取得所有使用者的位置。
     * 管理者地圖頁面會呼叫這個 API。
     */
    @GetMapping("/admin/all")
    public ResponseEntity<Map<String, Map<String, Double>>> getAllUserLocations() {
        return ResponseEntity.ok(userLocations);
    }
}
