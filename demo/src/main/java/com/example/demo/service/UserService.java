// filepath: src/main/java/com/example/demo/service/UserService.java
package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder; // 假設您有密碼加密器
import org.springframework.web.multipart.MultipartFile; // 新增：處理檔案上傳
import org.apache.poi.ss.usermodel.*; // 新增：Apache POI 核心介面 (Workbook, Sheet, Row, Cell, CellType, DataFormatter)
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // 新增：處理 .xlsx 格式
import org.apache.poi.hssf.usermodel.HSSFWorkbook; // 新增：處理 .xls 格式

import java.io.IOException; // 新增：處理 IO 例外
import java.io.InputStream; // 新增：讀取檔案輸入流
import java.util.ArrayList; // 新增：用於儲存訊息列表
import java.util.Iterator; // 新增：用於迭代 Excel 行
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 假設注入密碼加密器

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String username, String rawPassword) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword)); // 儲存前加密密碼
        return userRepository.***REMOVED***ve(newUser); // 使用 ***REMOVED***ve 方法儲存使用者到資料庫
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username); // 使用自訂查詢方法
    }

    public List<User> getAllUsers() {
        return userRepository.findAll(); // 取得所有使用者
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id); // 根據 ID 查詢使用者
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id); // 根據 ID 刪除使用者
    }
    /**
     * 更新使用者資訊 (PUT)。
     * 如果使用者存在，則使用提供的資料完整更新使用者。
     * 如果提供了新的密碼，則會進行加密。
     * @param id 使用者 ID
     * @param userDataForUpdate 包含更新後使用者資料的 User 物件
     * @return 更新後的使用者物件，如果使用者不存在則為 Optional.empty()
     */
    public Optional<User> updateUser(Long id, User userDataForUpdate) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setUsername(userDataForUpdate.getUsername());

                    // 如果在 userDataForUpdate 中提供了密碼，則更新並加密
                    if (userDataForUpdate.getPassword() != null && !userDataForUpdate.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(userDataForUpdate.getPassword()));
                    }
                    // 假設 User 模型還有其他欄位，例如 email, roles 等，可以在這裡一併更新
                    // existingUser.setEmail(userDataForUpdate.getEmail());
                    // existingUser.setRoles(userDataForUpdate.getRoles());
                    // ... 其他欄位的完整更新

                    return userRepository.***REMOVED***ve(existingUser);
                });
    }

    /**
     * 部分更新使用者資訊 (PATCH)。
     * 如果使用者存在，則僅更新 userDataForPatch 中非 null 的欄位。
     * 如果提供了新的密碼，則會進行加密。
     * @param id 使用者 ID
     * @param userDataForPatch 包含要更新的部分使用者資料的 User 物件
     * @return 更新後的使用者物件，如果使用者不存在則為 Optional.empty()
     */
    public Optional<User> patchUser(Long id, User userDataForPatch) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    // 檢查 username 是否需要更新
                    if (userDataForPatch.getUsername() != null) {
                        existingUser.setUsername(userDataForPatch.getUsername());
                    }

                    // 檢查 password 是否需要更新
                    if (userDataForPatch.getPassword() != null && !userDataForPatch.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(userDataForPatch.getPassword()));
                    }

                    // 假設 User 模型還有其他欄位，例如 email, roles 等
                    // if (userDataForPatch.getEmail() != null) {
                    //     existingUser.setEmail(userDataForPatch.getEmail());
                    // }
                    // if (userDataForPatch.getRoles() != null) { // 假設 roles 是集合或複雜物件，需要更仔細的合併邏輯
                    //     existingUser.setRoles(userDataForPatch.getRoles());
                    // }
                    // ... 其他欄位的選擇性更新

                    return userRepository.***REMOVED***ve(existingUser);
                });
    }
/**
     * 從 Excel 檔案匯入使用者。
     * 假設 Excel 檔案的第一欄是 username，第二欄是 password。
     * @param file 上傳的 Excel 檔案 (MultipartFile)
     * @return 包含匯入結果訊息的列表
     * @throws IOException 如果讀取檔案時發生 IO 錯誤
     */
    public List<String> importUsersFromExcel(MultipartFile file) throws IOException {
        List<String> mes***REMOVED***ges = new ArrayList<>();
        Workbook workbook = null;
        InputStream inputStream = file.getInputStream();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.toLowerCase().endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(inputStream); // 處理 .xlsx
        } else if (originalFilename != null && originalFilename.toLowerCase().endsWith(".xls")) {
            workbook = new HSSFWorkbook(inputStream); // 處理 .xls
        } else {
            mes***REMOVED***ges.add("錯誤：不支援的檔案格式。請上傳 .xls 或 .xlsx 檔案。");
            if (inputStream != null) {
                inputStream.close();
            }
            return mes***REMOVED***ges;
        }

        Sheet sheet = workbook.getSheetAt(0); // 假設資料在第一個工作表
        Iterator<Row> rowIterator = sheet.iterator();

        int rowNumber = 0;
        // 嘗試跳過標頭列 (如果 Excel 檔案有標頭)
        if (rowIterator.hasNext()) {
            rowIterator.next(); // 讀取並忽略第一行 (標頭)
            rowNumber++;
        }

        DataFormatter formatter = new DataFormatter(); // 用於將儲存格內容安全地格式化為字串

        while (rowIterator.hasNext()) {
            Row currentRow = rowIterator.next();
            rowNumber++;

            // 獲取儲存格，如果儲存格不存在或為空，則返回 null
            Cell usernameCell = currentRow.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL); // 第一欄 (索引 0)
            Cell passwordCell = currentRow.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL); // 第二欄 (索引 1)

            String username = null;
            String rawPassword = null;

            if (usernameCell != null) {
                username = formatter.formatCellValue(usernameCell).trim();
            }

            if (passwordCell != null) {
                rawPassword = formatter.formatCellValue(passwordCell).trim();
            }

            if (username == null || username.isEmpty() || rawPassword == null || rawPassword.isEmpty()) {
                mes***REMOVED***ges.add("警告：第 " + rowNumber + " 行資料不完整 (使用者名稱或密碼為空)，已跳過。");
                continue; // 跳過此行，處理下一行
            }

            try {
                // 檢查使用者是否已存在 (選擇性，但建議)
                if (userRepository.findByUsername(username).isPresent()) {
                    mes***REMOVED***ges.add("警告：第 " + rowNumber + " 行的使用者 '" + username + "' 已存在，已跳過。");
                    continue;
                }
                // 使用現有的 createUser 方法建立並儲存使用者
                createUser(username, rawPassword);
                mes***REMOVED***ges.add("成功：第 " + rowNumber + " 行的使用者 '" + username + "' 已匯入。");
            } catch (Exception e) {
                // 在實際應用中，您可能想使用日誌框架記錄錯誤
                // logger.error("Error importing user {} at row {}: {}", username, rowNumber, e.getMes***REMOVED***ge());
                mes***REMOVED***ges.add("錯誤：第 " + rowNumber + " 行的使用者 '" + username + "' 匯入失敗：" + e.getMes***REMOVED***ge());
            }
        }

        workbook.close(); // 關閉工作簿以釋放資源
        inputStream.close(); // 關閉輸入流

        return mes***REMOVED***ges;
    }
}