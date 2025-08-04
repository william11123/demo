package com.example.demo.service;

import com.example.demo.dto.BankInfoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BankInfoService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 擷取所有符合條件的銀行資訊，用於匯出。
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 包含所有符合條件的銀行資訊列表
     */
    public List<BankInfoDTO> getBankInfoForExport(String startDate, String endDate) {
        // 您的原生 SQL 查詢，這裡不作任何筆數限制
        String sql = "WITH RankedResults AS (" +
            "    SELECT " +
            "        CONCAT(t_o.CustomNo,'.',c.NickName) AS bankname, " +
            "        t_oterm.NickName, t_oterm.Addr, t_oterm.SaleLia, t_oterm.Tel1, t_oterm.Tel2, " +
            "        SUBSTRING(t_o.CompDate, 1, 7) AS CompMonth, " +
            "        ROW_NUMBER() OVER(PARTITION BY t_oterm.NickName ORDER BY t_o.CompDate DESC) as rn " +
            "    FROM TaskOrder AS t_o " +
            "    INNER JOIN Custom C ON C.CustomNo=t_o.CustomNo " +
            "    INNER JOIN TaskReplyTerm AS t_rterm ON t_o.TONo = t_rterm.TONo " +
            "    INNER JOIN TaskOrderTerm AS t_oterm ON t_o.TONo = t_oterm.TONo AND t_oterm.TermNo = t_rterm.TermNo " +
            "    WHERE t_o.TaskType = '2' " +
            "        AND t_o.TaskNo = '05' " +
            "        AND t_o.iscomp = 'Y' " +
            "        AND t_rterm.RlyNo = '00' " +
            "        AND t_o.CustomNo NOT IN ('02', '71') " +
            "        AND t_o.CompDate >= ? AND t_o.CompDate < ? " +
            ") " +
            "SELECT bankname, NickName, Addr, SaleLia, Tel1, Tel2, CompMonth " +
            "FROM RankedResults " +
            "WHERE rn = 1 " +
            "ORDER BY NickName;";

        return jdbcTemplate.query(
                sql,
                new Object[]{startDate, endDate},
                (rs, rowNum) -> new BankInfoDTO(
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
