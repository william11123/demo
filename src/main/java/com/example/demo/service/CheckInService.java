package com.example.demo.service;

import com.example.demo.dto.CheckInRequest;
import com.example.demo.model.CheckInRecord;
import com.example.demo.model.LocationTarget; // 匯入新的地點模型
import com.example.demo.repository.CheckInRecordRepository;
import com.example.demo.repository.LocationTargetRepository; // 匯入新的地點 Repository
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CheckInService {

    private final CheckInRecordRepository checkInRecordRepository;
    private final LocationService locationService;
    private final LocationTargetRepository locationTargetRepository; // 注入新的 Repository

    public CheckInService(
        CheckInRecordRepository checkInRecordRepository,
        LocationService locationService,
        LocationTargetRepository locationTargetRepository // 在建構子中加入
    ) {
        this.checkInRecordRepository = checkInRecordRepository;
        this.locationService = locationService;
        this.locationTargetRepository = locationTargetRepository;
    }

    /**
     * 處理簽到請求的核心業務邏輯。
     * @param request 來自前端的簽到請求資料
     * @return 一個表示處理結果的字串訊息
     */
    public String performCheckIn(CheckInRequest request) {
        // 1. 從「資料庫」中，根據名稱找出目標地點
        Optional<LocationTarget> targetOpt = locationTargetRepository.findByName(request.getLocationName());

        if (targetOpt.isEmpty()) {
            return "簽到失敗：系統中找不到目標地點 '" + request.getLocationName() + "'";
        }
        LocationTarget target = targetOpt.get();

        // 2. 呼叫 LocationService 來判斷使用者是否在 5 公里範圍內
        boolean isInRange = locationService.processArrivalRequest(
            request.getLatitude(),
            request.getLongitude(),
            target.getLatitude(),  // 使用 getter 方法
            target.getLongitude() // 使用 getter 方法
        );

        // 3. 如果在範圍內，就建立紀錄並存入資料庫
        if (isInRange) {
            CheckInRecord record = new CheckInRecord(
                null,
                request.getUserId(),
                request.getLocationName(),
                LocalDateTime.now()
            );
            checkInRecordRepository.***REMOVED***ve(record);
            return "簽到成功！已在 " + request.getLocationName() + " 記錄您的簽到。";
        } else {
            // 4. 如果不在範圍內，就回傳失敗訊息
            return "簽到失敗：您距離目標地點太遠。";
        }
    }

    /**
     * 【新功能】從資料庫獲取所有可用的簽到地點。
     * @return 地點列表
     */
    public List<LocationTarget> getAllLocationTargets() {
        return locationTargetRepository.findAll();
    }
    /**
     * 【新功能】從資料庫獲取所有簽到歷史紀錄。
     * @return 根據簽到時間倒序排列的歷史紀錄列表
     */
    public List<CheckInRecord> getCheckInHistory() {
        // 使用 JpaRepository 的 findAll 方法，並提供一個排序條件
        return checkInRecordRepository.findAll(Sort.by(Sort.Direction.DESC, "checkInTime"));
    }
}
