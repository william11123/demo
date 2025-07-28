package com.example.demo.repository;

import com.example.demo.model.CheckInRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInRecordRepository extends JpaRepository<CheckInRecord, Long> {
    // Spring Data JPA 會自動提供儲存、刪除、查詢等基本方法。
    // 如果未來有更複雜的查詢需求，可以在這裡定義。
}
