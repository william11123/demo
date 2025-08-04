package com.example.demo.service;

import com.example.demo.dto.BankInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 提供執行銀行資訊查詢的服務。
 */
@Service
public class BankInfoService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 根據給定的開始和結束日期執行原生 SQL 查詢以獲取銀行資訊。
     * @param startDate 查詢的開始日期 (yyyy-MM-dd 格式)。
     * @param endDate 查詢的結束日期 (yyyy-MM-dd 格式，不包含)。
     * @return BankInfoDTO 物件的列表。
     */
    public List<BankInfoDTO> getBankInfo(String startDate, String endDate) {
        // 您的原生 SQL 查詢，日期部分已參數化以防止 SQL 注入
        String sql = "WITH RankedResults AS (" +
            "    SELECT " +
            "        CONCAT(t_o.CustomNo,'.',c.NickName) AS bankname, " +
            "        t_oterm.NickName, " +
            "        t_oterm.Addr, " +
            "        t_oterm.SaleLia, " +
            "        t_oterm.Tel1, " +
            "        t_oterm.Tel2, " +
            "        SUBSTRING(t_o.CompDate, 1, 7) AS CompMonth, " +
            "        ROW_NUMBER() OVER(PARTITION BY t_oterm.NickName ORDER BY t_o.CompDate DESC) as rn " +
            "    FROM " +
            "        TaskOrder AS t_o " +
            "    INNER JOIN " +
            "        Custom C  ON C.CustomNo=t_o.CustomNo " +
            "    INNER JOIN " +
            "        TaskReplyTerm AS t_rterm ON t_o.TONo = t_rterm.TONo " +
            "    INNER JOIN " +
            "        TaskOrderTerm AS t_oterm ON t_o.TONo = t_oterm.TONo AND t_oterm.TermNo = t_rterm.TermNo " +
            "    WHERE " +
            "        t_o.TaskType = '2' " +
            "        AND t_o.TaskNo = '05' " +
            "        AND t_o.iscomp = 'Y' " +
            "        AND t_rterm.RlyNo = '00' " +
            "        AND t_o.CustomNo NOT IN ('02', '71') " +
            "        AND t_o.CompDate >= ? AND t_o.CompDate < ? " +
            ") " +
            "SELECT " +
            "    bankname, " +
            "    NickName, " +
            "    Addr, " +
            "    SaleLia, " +
            "    Tel1, " +
            "    Tel2, " +
            "    CompMonth " +
            "FROM " +
            "    RankedResults " +
            "WHERE " +
            "    rn = 1 " +
            "ORDER BY " +
            "    NickName;";

        // 使用 jdbcTemplate 執行查詢並將結果映射到 BankInfoDTO 列表
        return jdbcTemplate.query(
                sql,
                new Object[]{startDate, endDate},
                (rs, rowNum) ->
                        new BankInfoDTO(
                                rs.getString("bankname"),
                                rs.getString("NickName"),
                                rs.getString("Addr"),
                                rs.getString("SaleLia"),
                                rs.getString("Tel1"),
                                rs.getString("Tel2"),
                                rs.getString("CompMonth")
                        )
        );
    }
}
