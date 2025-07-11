package com.example.demo.controller;

import com.example.demo.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/location")
public class LocationController {

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
    public ResponseEntity<String> handleArrivalRequest(@PathVariable String userId, @RequestBody Map<String, Double> location) {
        // 假設目標地點在台北 101
        final double TARGET_LAT = 25.033964;
        final double TARGET_LON = 121.564468;

        double userLat = location.get("latitude");
        double userLon = location.get("longitude");

        boolean isApproved = locationService.processArrivalRequest(userLat, userLon, TARGET_LAT, TARGET_LON);

        if (isApproved) {
            System.out.println("使用者 " + userId + " 的到達請求已核准！");
            // 在這裡觸發給管理者的通知 (例如，透過 WebSocket)
            return ResponseEntity.ok("請求已核准：您在目標範圍內。");
        } else {
            System.out.println("使用者 " + userId + " 的到達請求被拒絕。");
            return ResponseEntity.badRequest().body("請求被拒絕：您不在目標範圍內。");
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
