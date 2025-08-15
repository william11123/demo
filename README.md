# Demo Backend & Web

一個以 Spring Boot 建構，採 Java + Kotlin 混合開發的企業後端範例專案，涵蓋資料上傳、查詢、報表匯出、打卡紀錄、任務摘要與簡易登入頁面。適合作為多模組 / 多功能後端系統腳手架（Scaffold）參考。

> 若你 fork / 使用此專案，請依實際情況補上授權、部署與環境設定細節。

---

## 目錄
- [功能亮點](#功能亮點)
- [系統架構概觀](#系統架構概觀)
- [技術棧](#技術棧)
- [專案結構](#專案結構)
- [核心模組說明](#核心模組說明)
- [執行前置需求](#執行前置需求)
- [環境設定](#環境設定)
- [建置與啟動](#建置與啟動)
- [範例 API 端點](#範例-api-端點)
- [資料流程範例](#資料流程範例)
- [開發流程建議](#開發流程建議)
- [測試 / 品質](#測試--品質)
- [部署建議](#部署建議)
- [常見問題 (FAQ)](#常見問題-faq)
- [貢獻指南](#貢獻指南)
- [授權](#授權)

---

## 功能亮點
- 銀行資訊管理 (Bank Info)
- ICSS 合同上傳 / 識別與持久化
- 租賃合約資料與 Excel 匯出
- 打卡 (Check-In) 紀錄與查詢
- 地點與目標維護 (Location / LocationTarget)
- 預測檔案上傳 / 更新 (Predict Upload）
- 流程查詢 (Process Query)
- 任務摘要 / 統計 (Task Summary)
- 靜態頁面 + Thymeleaf 模板登入流程
- Excel 匯出（一般 / 串流，降低記憶體壓力）
- Spring Security 簡化授權（可再擴充 JWT / RBAC）

---

## 系統架構概觀
```
+-------------------+        +----------------------+
|     Browser       | <----> | Spring Controllers   |
| (HTML / Template) |        | (REST + View)        |
+-------------------+        +----------+-----------+
                                        |
                                        v
                               +------------------+
                               |  Service Layer   |
                               |(商業邏輯 / 驗證) |
                               +---------+--------+
                                         |
                                         v
                               +------------------+
                               |  Repository(JPA) |
                               +---------+--------+
                                         |
                                         v
                               +------------------+
                               | RDB (e.g. MySQL) |
                               +------------------+
```

---

## 技術棧
- Runtime：Spring Boot
- 語言：Java + Kotlin 混合
- Web：Spring MVC, Thymeleaf (templates), 靜態 HTML
- 安全：Spring Security
- 資料存取：Spring Data JPA
- 匯出：自訂 ExcelExportService / ExcelStreamingService
- 建置：Maven 或 Gradle（請依實際專案根目錄檔案補充）
- 測試：JUnit (Spring Boot Test)

---

## 專案結構
```
src/main
 ├─ java/com/example/demo
 │   ├─ DemoApplication.java        # 啟動入口
 │   ├─ SecurityConfig.java         # 安全性設定
 │   ├─ MvcConfig.java              # MVC / CORS / 靜態資源
 │   ├─ controller/                 # 各功能控制器
 │   ├─ service/                    # 業務邏輯
 │   ├─ repository/                 # JPA Repository (Java)
 │   └─ model/                      # Java 實體 (少量)
 ├─ kotlin/com/example/demo
 │   ├─ model/                      # Kotlin 實體 (JPA Entities)
 │   ├─ dto/                        # DTO / Request
 │   └─ repository/                 # Kotlin Repository
 └─ resources
     ├─ application.properties      # 組態
     ├─ static/                     # 靜態頁面 (HTML)
     └─ templates/                  # Thymeleaf (login, welcome)
```

---

## 核心模組說明
| 模組 | 說明 | 延伸建議 |
|------|------|----------|
| controller | 接收 HTTP 請求，轉交 service | 可加入 Global Exception Handler |
| service | 商業邏輯、驗證、轉換 | 引入 MapStruct 簡化 DTO 轉換 |
| repository | JPA CRUD / 查詢 | 可加入 Specification / QueryDSL |
| model | 實體 | 命名一致性 (Java/Kotlin) |
| dto | 封裝輸入輸出 | 可加 Bean Validation |
| security | Basic/Auth 配置 | 擴充 JWT / OAuth2 |

---

## 執行前置需求
- JDK 17+ (建議)
- 一個關聯式資料庫（MySQL / PostgreSQL / H2）
- Maven 3.9+ 或 Gradle 8+
- Git

---

## 環境設定
application.properties 範例（請依實際 DB 修改）：
```
spring.datasource.url=jdbc:mysql://localhost:3306/demo?useSSL=false&serverTimezone=Asia/Taipei
spring.datasource.username=demo
spring.datasource.password=demo123
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.thymeleaf.cache=false
server.port=8080
```

可改用環境變數：
```
SPRING_DATASOURCE_URL=jdbc:...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
```

---

## 建置與啟動
Maven：
```
mvn clean package
mvn spring-boot:run
```
Gradle (Wrapper)：
```
gradlew clean build
gradlew bootRun
```
執行 Jar：
```
java -jar target/demo-*.jar
# 或 build/libs/demo-*.jar
```

啟動後預設：
- Login: http://localhost:8080/login
- Welcome: http://localhost:8080/welcome

---

## 範例 API 端點
| 功能 | 範例路徑 (示意) | 方法 | 說明 |
|------|-----------------|------|------|
| 銀行資訊 | /bank-info/list | GET | 取得列表 |
| ICSS 合同 | /icss/upload | POST | 上傳合同檔案 |
| 租賃合約 | /lease/export | GET | 匯出 Excel |
| 打卡 | /check-in | POST | 新增打卡紀錄 |
| 地點 | /location/targets | GET | 目標清單 |
| 預測上傳 | /predict/upload | POST | 上傳預測檔 |
| 流程查詢 | /process/query | POST | 條件查詢 |
| 任務摘要 | /task-summary | GET | 統計摘要 |

> 以實際 controller 實作為準。建議後續加入 Spring REST Docs 或 OpenAPI (Swagger) 自動化描述。

---

## 資料流程範例
(以預測檔案上傳為例)
```
Client -> POST /predict/upload (Multipart/JSON)
   -> PredictUploadController
      -> PredictUploadService
         -> PredictUploadRepository.save()
            -> DB
         -> 回傳上傳結果 DTO
```

---

## 開發流程建議
1. 建立/調整 Entity (Kotlin / Java)
2. 建立 Repository (介面化)
3. 新增 DTO (輸入 / 輸出)
4. Service：封裝邏輯 + 交易邊界
5. Controller：驗證輸入 + 呼叫 service
6. 撰寫單元 / 整合測試
7. 若含頁面：於 static/ 或 templates/ 加入 HTML
8. 更新 README / 版本記錄

---

## 測試 / 品質
- 測試命令：
  - Maven: `mvn test`
  - Gradle: `gradlew test`
- 建議補強：
  - Service 層單元測試
  - Controller 層 MockMvc 測試
  - Repository 整合測試 (Testcontainers)
  - Excel 匯出結果格式測試 (以暫存檔比對)
- 建議工具：
  - Spotless / Checkstyle / ktlint
  - SonarQube (品質門檻)

---

## 部署建議
| 環境 | 建議設定 |
|------|----------|
| Dev | H2 / Docker MySQL, `ddl-auto=update` |
| Staging | 外部 DB，關閉 `show-sql` |
| Prod | 明確 schema migration：Flyway / Liquibase |

容器化 (示意 Dockerfile)：
```Dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/demo-*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

---

## 常見問題 (FAQ)
Q: 為什麼有 Java 與 Kotlin 混合？  
A: 逐步遷移或示範共存模式，可視需要統一語言。

Q: 如何新增欄位？  
A: 更新 Entity -> DDL 自動更新 (開發)；正式環境請改用 Migration 工具。

Q: 如何支援 Swagger？  
A: 引入 springdoc-openapi-starter-webmvc-ui，啟動後訪問 /swagger-ui.html。

---

## 貢獻指南
1. Fork & 建立 feature 分支
2. 撰寫/更新測試
3. 透過 PR 說明變更內容
4. 確保 Lint / Build / Test 全數通過
5. 嚴守命名與程式風格一致性

建議 Commit 規範 (可選)：
```
feat: 新增預測上傳批次驗證
fix: 修正租賃合約日期格式解析
refactor: 重構 TaskSummaryService 結構
docs: 更新 README API 區段
test: 增補 ICSSContractRepository 測試
```

---

## 授權
請於此處補上專案授權 (例如 MIT / Apache-2.0 / 商業內部使用)。  
範例 (MIT)：
```
本專案採 MIT License，詳見 LICENSE 檔案。
```

---

## 後續 Roadmap (建議)
- [ ] OpenAPI / Swagger 文件
- [ ] 統一錯誤回應格式 (ErrorResponse)
- [ ] Global Exception Handler
- [ ] 引入分頁 / 過濾通用規格
- [ ] Docker Compose (App + DB)
- [ ] 加入 Flyway Migration
- [ ] 前端改為 SPA / 加入 API Token
- [ ] CI (GitHub Actions) 自動建置 / 測試 / 發佈

---

## 聯絡
(請補：維護者 / Email / Issue Tracker 連結)

---

若需要針對任一模組再產生 API 文件、OpenAPI 規格或補測試樣板，告知即可。祝開發順
