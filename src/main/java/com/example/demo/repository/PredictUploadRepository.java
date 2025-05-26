package com.example.demo.repository;

import com.example.demo.model.PredictUpload; // <--- 匯入 PredictUpload
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PredictUploadRepository extends JpaRepository<PredictUpload, Long>, JpaSpecificationExecutor<PredictUpload> { // <--- 繼承 JpaSpecificationExecutor
    Optional<PredictUpload> findByCustomNo(String customNo);
    Optional<PredictUpload> findByTaskType(String taskType);
    Optional<PredictUpload> findByInMonth(String inmonth);
}