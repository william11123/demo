package com.example.demo.repository;

import com.example.demo.model.LocationTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationTargetRepository extends JpaRepository<LocationTarget, String> {
    
    /**
     * 根據地點名稱查詢地點。
     * @param name 地點名稱
     * @return 一個包含 LocationTarget 的 Optional 物件
     */
    Optional<LocationTarget> findByName(String name);
}
