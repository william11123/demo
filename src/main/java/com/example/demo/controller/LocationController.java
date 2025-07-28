package com.example.demo.controller;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.model.LocationTarget;
import com.example.demo.service.CheckInService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    private final CheckInService checkInService;

    private static final Map<String, Map<String, Object>> userLastLocations = new ConcurrentHashMap<>();

    public LocationController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @GetMapping("/all-targets")
    public ResponseEntity<List<LocationTarget>> getAllTargets() {
        List<LocationTarget> targets = checkInService.getAllLocationTargets();
        return ResponseEntity.ok(targets);
    }
    /**
     * 處理使用者簽到請求，包含距離驗證和資料庫儲存。
     * Flutter App 應該呼叫這個 API。
     * 端點: POST /api/location/check-in
     */
    @PostMapping("/check-in")
    public ResponseEntity<Map<String, String>> handleCheckIn(@RequestBody CheckInRequest request) {
        logger.info("收到來自使用者 '{}' 在 '{}' 的簽到請求", request.getUserId(), request.getLocationName());

        // 更新位置資訊時，一併存入地點名稱
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", request.getLatitude());
        locationData.put("longitude", request.getLongitude());
        locationData.put("locationName", request.getLocationName()); // 新增地點名稱
        userLastLocations.put(request.getUserId(), locationData);
        logger.info("已更新使用者 '{}' 的最後位置及地點。", request.getUserId());

        Map<String, String> response = new HashMap<>();
        try {
            String resultMes***REMOVED***ge = checkInService.performCheckIn(request);
            response.put("mes***REMOVED***ge", resultMes***REMOVED***ge);

            if (resultMes***REMOVED***ge.contains("成功")) {
                logger.info("使用者 '{}' 在 '{}' 簽到成功。", request.getUserId(), request.getLocationName());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("使用者 '{}' 在 '{}' 簽到失敗：{}", request.getUserId(), request.getLocationName(), resultMes***REMOVED***ge);
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("處理簽到請求時發生未預期錯誤", e);
            response.put("mes***REMOVED***ge", "伺服器內部錯誤，請稍後再試。");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 讓管理者取得所有使用者最後一次簽到時的位置和地點名稱。
     * 管理者地圖頁面可以呼叫這個 API。
     * 端點: GET /api/location/admin/all-locations
     */
    @GetMapping("/admin/all-locations")
    public ResponseEntity<Map<String, Map<String, Object>>> getAllUserLastLocations() {
        // 將 ConcurrentHashMap 轉換為新的 HashMap，以幫助編譯器進行類型推斷。
        return ResponseEntity.ok(new HashMap<>(userLastLocations));
    }
    /**
     * 【新功能】讓管理者取得所有永久儲存的簽到歷史紀錄。
     * 端點: GET /api/location/admin/history
     */
    @GetMapping("/admin/history")
    public ResponseEntity<List<CheckInRecord>> getCheckInHistory() {
        List<CheckInRecord> history = checkInService.getCheckInHistory();
        return ResponseEntity.ok(history);
    }
}
