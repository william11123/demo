package com.example.demo.service;

import com.example.demo.dto.LeaseContractDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaseContractService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LeaseContractService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 獲取【租賃】合約資料。
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return DTO 列表
     */
    public List<LeaseContractDTO> getLeaseContractData(String startDate, String endDate) {
        // 定義租賃合約的客戶編號
        List<String> customNo1 = List.of("28", "43");
        List<String> customNo2 = List.of("105", "108", "75", "25", "74", "109");

        String customNo1Params = customNo1.stream().map(s -> "'" + s + "'").collect(Collectors.joining(","));
        String customNo2Params = customNo2.stream().map(s -> "'" + s + "'").collect(Collectors.joining(","));

        String sql = String.format("""
            -- 這部分是您的原始租賃查詢
            SELECT
                CONCAT(t.CustomNo, '.', c.NickName) AS bankname,
                t.tasktype,
                CONCAT(t.taskno, '.', tk.taskname) AS taskno,
                CONCAT(t.maintno, '.', p.paramname) AS maint,
                CONCAT(trtd.mateno, ' - ', m.matename) AS edc,
                t.CompDate,
                t.iscomp,
                t.prjno,
                case
                    when t.CustomNo in ('25','28','105') then '3'
                    when t.customno = '108'  and r.RequestDate < '2023-01-01' then '3'
                    when t.customno = '109' then '3.17'
                    when t.CustomNo = '43' then '5'
                    when t.customno = '108'  and r.RequestDate >= '2023-01-01' then '5'
                    else jod.SafeYear
                end as ***REMOVED***veyear,
                r.RequestDate,
                t.tono,
                COALESCE(trtd.ChangeSN, trtd.RealSN) as sn
            FROM
                taskorder t
            INNER JOIN TaskReplyTermDet trtd ON t.tono = trtd.tono
            inner join taskreplyterm tr on t.tono =tr.tono
            INNER JOIN custom c ON c.customno = t.customno
            INNER JOIN material m ON m.mateno = trtd.mateno
            INNER JOIN task tk ON tk.taskno = t.taskno
            INNER JOIN param p ON p.ParamNo = t.MaintNo
            left JOIN RequestSnDet rsd ON rsd.sn = COALESCE(trtd.ChangeSN, trtd.RealSN)
            left JOIN request r ON rsd.RequestNo = r.RequestNo
            left JOIN JobOrderDet jod ON jod.JobOrdNo = r.JobOrdNo AND jod.item = 1
            WHERE
                t.customno IN (%s)
                AND t.CompDate >= ?
                AND t.CompDate < ?
                AND trtd.mateno LIKE '09%%'
                AND m.matename LIKE '%%租賃%%'
                AND p.item = 0
                and ((t.tasktype IN (1, 2, 4) AND tr.rlyno = '00') OR (t.tasktype = 3))
            union
            SELECT
                CONCAT(t.CustomNo, '.', c.NickName) AS bankname,
                t.tasktype,
                CONCAT(t.taskno, '.', tk.taskname) AS taskno,
                CONCAT(t.maintno, '.', p.paramname) AS maint,
                CONCAT(trtd.mateno, ' - ', m.matename) AS edc,
                t.CompDate,
                t.iscomp,
                t.prjno,
                case
                    when t.CustomNo in ('25','28','105') then '3'
                    when t.customno = '108'  and r.RequestDate < '2023-01-01' then '3'
                    when t.customno = '109' then '3.17'
                    when t.CustomNo = '43' then '5'
                    when t.customno = '108'  and r.RequestDate >= '2023-01-01' then '5'
                    else jod.SafeYear
                end as ***REMOVED***veyear,
                r.RequestDate,
                t.tono,
                COALESCE(trtd.ChangeSN, trtd.RealSN) as sn
            FROM
                taskorder t
            INNER JOIN TaskReplyTermDet trtd ON t.tono = trtd.tono
            inner join taskreplyterm tr on t.tono =tr.tono
            INNER JOIN custom c ON c.customno = t.customno
            INNER JOIN material m ON m.mateno = trtd.mateno
            INNER JOIN task tk ON tk.taskno = t.taskno
            INNER JOIN param p ON p.ParamNo = t.MaintNo
            left JOIN RequestSnDet rsd ON rsd.sn = COALESCE(trtd.ChangeSN, trtd.RealSN)
            left JOIN request r ON rsd.RequestNo = r.RequestNo
            left JOIN JobOrderDet jod ON jod.JobOrdNo = r.JobOrdNo AND jod.item = 1
            WHERE
                t.customno IN (%s)
                AND t.CompDate >= ?
                AND t.CompDate < ?
                AND trtd.mateno LIKE '09%%'
                AND p.item = 0
                and ((t.tasktype IN (1, 2, 4) AND tr.rlyno = '00') OR (t.tasktype = 3))
        """, customNo1Params, customNo2Params);

        return jdbcTemplate.query(
                sql,
                new Object[]{startDate, endDate, startDate, endDate},
                (rs, rowNum) -> mapRowToLeaseContractDTO(rs)
        );
    }

    /**
     * 獲取【非租賃】合約資料。
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return DTO 列表
     */
    public List<LeaseContractDTO> getNonLeaseContractData(String startDate, String endDate) {
        String sql = """
            -- 這是您的非租賃查詢
            SELECT
                CONCAT(t.CustomNo, '.', c.NickName) AS bankname,
                t.tasktype,
                CONCAT(t.taskno, '.', tk.taskname) AS taskno,
                CONCAT(t.maintno, '.', p.paramname) AS maint,
                CONCAT(trtd.mateno, ' - ', m.matename) AS edc,
                t.CompDate,
                t.iscomp,
                t.prjno,
                CASE
                    WHEN t.CustomNo IN ('02','03','09','28','77','83','106') THEN '3'
                    WHEN t.customno IN ('11','32','86') THEN '1'
                    WHEN t.customno = '20' THEN '1.5'
                    WHEN t.CustomNo = '29' THEN '2'
                    WHEN t.customno IN ('47','73') THEN '4'
                    WHEN t.customno = '71' THEN '5'
                    WHEN t.CustomNo = '13' THEN '13'
                    ELSE jod.SafeYear
                END AS ***REMOVED***veyear,
                r.RequestDate,
                t.tono,
                COALESCE(trtd.ChangeSN, trtd.RealSN) AS sn
            FROM
                taskorder t
            INNER JOIN TaskReplyTermDet trtd ON t.tono = trtd.tono
            INNER JOIN custom c ON c.customno = t.customno
            INNER JOIN material m ON m.mateno = trtd.mateno
            INNER JOIN task tk ON tk.taskno = t.taskno
            INNER JOIN param p ON p.ParamNo = t.MaintNo
            LEFT JOIN RequestSnDet rsd_change ON rsd_change.sn = trtd.ChangeSN
            LEFT JOIN RequestSnDet rsd_real ON rsd_real.sn = trtd.RealSN AND trtd.ChangeSN IS NULL
            LEFT JOIN request r ON r.RequestNo = COALESCE(rsd_change.RequestNo, rsd_real.RequestNo)
            LEFT JOIN JobOrderDet jod ON jod.JobOrdNo = r.JobOrdNo AND jod.item = 1
            WHERE
                t.CompDate >= ?
                AND t.CompDate < ?
                AND trtd.mateno LIKE '09%%'
                AND p.item = 0
                AND (
                    (t.tasktype IN (1, 2, 4) AND EXISTS (
                        SELECT 1 FROM taskreplyterm tr WHERE tr.tono = t.tono AND tr.rlyno = '00'
                    ))
                    OR (t.tasktype = 3)
                )
                AND t.customno NOT IN ('105', '108', '75', '25','74','109')
                AND m.matename not LIKE '%%租賃%%'
            ORDER BY
                t.TONo
        """;
        
        return jdbcTemplate.query(
                sql,
                new Object[]{startDate, endDate},
                (rs, rowNum) -> mapRowToLeaseContractDTO(rs)
        );
    }
    
    /**
     * 將查詢結果的一行映射到 LeaseContractDTO 物件。
     * @param rs ResultSet
     * @return LeaseContractDTO
     * @throws java.sql.SQLException
     */
    private LeaseContractDTO mapRowToLeaseContractDTO(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new LeaseContractDTO(
            rs.getString("bankname"),
            rs.getString("tasktype"),
            rs.getString("taskno"),
            rs.getString("maint"),
            rs.getString("edc"),
            rs.getString("CompDate"),
            rs.getString("iscomp"),
            rs.getString("prjno"),
            rs.getString("***REMOVED***veyear"),
            rs.getString("RequestDate"),
            rs.getString("tono"),
            rs.getString("sn")
        );
    }
}
