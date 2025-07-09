package com.example.demo.repository;

import com.example.demo.dto.ProcessQueryDTO; // 匯入您的 DTO
import com.example.demo.model.TaskReplyTermDet; // 假設 Repository 主要操作 TaskReplyTermDet
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
// 假設這個 Repository 是針對 TaskReplyTermDet 或某個相關實體
public interface ProcessQueryRepository extends JpaRepository<TaskReplyTermDet, String> {

    @Query("""
           SELECT distinct NEW com.example.demo.dto.ProcessQueryDTO(t.compdate,t.shopno,t.iscomp,trtd.tono,trtd.mateno,trtd.termno, trtd.realsn,trtd.changesn)
           FROM TaskReplyTermDet trtd
           JOIN TaskOrder t ON trtd.tono = t.tono
           WHERE trtd.realsn = :whatyouinput OR trtd.changesn = :whatyouinput
           ORDER BY t.compdate DESC
           """)
    List<ProcessQueryDTO> findProcess(@Param("whatyouinput") String inputValue);
}