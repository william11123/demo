package com.example.demo.service;

import com.example.demo.dto.TaskSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 提供獲取任務統計數據相關的服務。
 * 此版本使用 JdbcTemplate 以獲得對複雜 SQL 查詢更好的控制和性能。
 */
@Service
public class TaskSummaryService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TaskSummaryService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根據指定的條件查詢任務統計數據。
     *
     * @param startDate 開始月份 (格式 YYYY-MM)
     * @param endDate   結束月份 (格式 YYYY-MM)
     * @param taskType  作業類別 ('1', '2', '3', '4', or 'all')
     * @param customNo  銀行編號 (可選，如果為 null 或空，則查詢所有銀行)
     * @return TaskSummaryDTO 物件的列表
     */
    public List<TaskSummaryDTO> getTaskSummary(String startDate, String endDate, String taskType, String customNo) {

        // 使用您提供的單一查詢，邏輯更清晰
        String baseQuery = """
            SELECT
                CONCAT(t.customno, '.', c.NickName) AS bank,
                CASE t.tasktype
                    WHEN 1 THEN '安裝'
                    WHEN 2 THEN '回收'
                    WHEN 3 THEN '維修'
                    WHEN 4 THEN '共用'
                END AS taskType,
                SUBSTRING(t.NotifyDate, 1, 7) AS month,
                COUNT(t.tono) AS quantity,
                SUM(CASE WHEN (t.tasktype IN (1, 2, 4) AND tr.rlyno = '00' and t.taskno != '88') OR (t.tasktype = 3) THEN 1 ELSE 0 END) AS completedQuantity,
                SUM(CASE WHEN (t.tasktype = 4 AND t.TaskNo = '88') THEN 1 ELSE 0 END) AS sharedSoftwareQuantity
            FROM
                taskorder t
            INNER JOIN
                Custom c ON c.customno = t.customno
            LEFT JOIN
                taskreplyterm tr ON t.tono = tr.TONo
            WHERE 1=1
        """;

        StringBuilder finalQuery = new StringBuilder(baseQuery);
        List<Object> params = new ArrayList<>();

        // 動態附加篩選條件
        if (startDate != null && !startDate.isEmpty()) {
            finalQuery.append(" AND SUBSTRING(t.NotifyDate, 1, 7) >= ?");
            params.add(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            finalQuery.append(" AND SUBSTRING(t.NotifyDate, 1, 7) <= ?");
            params.add(endDate);
        }
        if (customNo != null && !customNo.isEmpty()) {
            finalQuery.append(" AND t.customno = ?");
            params.add(customNo);
        }

        // 處理作業類別的篩選
        if (taskType != null && !taskType.isEmpty() && !"all".equalsIgnoreCase(taskType) && !"88".equalsIgnoreCase(taskType)) {
            finalQuery.append(" AND t.tasktype = ?");
            params.add(taskType);
        } else {
            // 如果是 'all' 或無效選項，則查詢所有相關類型
            finalQuery.append(" AND t.tasktype IN ('1', '2', '3', '4')");
        }

        // 將 GROUP BY 和 ORDER BY 合併，附加到查詢末尾
        finalQuery.append(" GROUP BY t.customno, c.NickName, SUBSTRING(t.NotifyDate, 1, 7), t.tasktype ORDER BY bank, month, taskType");

        // 使用 jdbcTemplate 執行查詢，並將結果映射到更新後的 DTO
        return jdbcTemplate.query(
                finalQuery.toString(),
                params.toArray(),
                (rs, rowNum) -> new TaskSummaryDTO(
                        rs.getString("bank"),
                        rs.getString("taskType"),
                        rs.getString("month"),
                        rs.getLong("quantity"),
                        rs.getLong("completedQuantity"),
                        rs.getLong("sharedSoftwareQuantity") // 映射新欄位
                )
        );
    }
}
