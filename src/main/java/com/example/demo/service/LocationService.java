package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class LocationService {

    // 地球半徑，單位為公里
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * 處理使用者的到達請求。
     *
     * @param userLat  使用者的緯度
     * @param userLon  使用者的經度
     * @param targetLat 目標地點的緯度
     * @param targetLon 目標地點的經度
     * @return 如果在 5 公里內，則返回 true
     */
    public boolean processArrivalRequest(double userLat, double userLon, double targetLat, double targetLon) {
        double distance = calculateDistance(userLat, userLon, targetLat, targetLon);
        
        // 檢查距離是否在 5 公里範圍內
        return distance <= 5.0;
    }

    /**
     * 使用 Haversine 公式計算兩個經緯度座標之間的距離。
     *
     * @return 距離，單位為公里
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                   Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1Rad) * Math.cos(lat2Rad);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}
