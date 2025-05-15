// filepath: src/main/java/com/example/demo/controller/UserController.java
package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.example.demo.dto.UserCreateRequest; // <--- 新增的 import 語句 (如果放在 dto 套件)
// 如果 UserCreateRequest.java 和 UserController.java 在同一個套件，則不需要這個 import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 確保這個也已匯入 (您之前的程式碼應該有)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users") // 所有此控制器的端點都會以 /api/users 開頭
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class); // 加入 Logger
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 建立新使用者 (例如，透過 POST 請求到 /api/users)
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserCreateRequest request) {
        User newUser = userService.createUser(request.getUsername(), request.getPassword());
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    // 根據使用者名稱獲取使用者 (例如，透過 GET 請求到 /api/users/john.doe)
    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> userOptional = userService.getUserByUsername(username);
        return userOptional.map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                           .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 獲取所有使用者 (例如，透過 GET 請求到 /api/users)
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // 根據 ID 獲取使用者 (例如，透過 GET 請求到 /api/users/id/1)
    @GetMapping("/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userService.getUserById(id);
        return userOptional.map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                           .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 根據 ID 刪除使用者 (例如，透過 DELETE 請求到 /api/users/1)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // 您可能想先檢查使用者是否存在
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    // 更新使用者資訊 (PUT - 完整更新)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> updatedUserOptional = userService.updateUser(id, userDetails);
        return updatedUserOptional
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 部分更新使用者資訊 (PATCH - 部分更新)
    @PatchMapping("/{id}")
    public ResponseEntity<User> patchUser(@PathVariable Long id, @RequestBody User userDetails) {
        Optional<User> patchedUserOptional = userService.patchUser(id, userDetails);
        return patchedUserOptional
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    @PostMapping("/upload-excel")
    public ResponseEntity<List<String>> uploadUsersFromExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of("請選擇一個檔案上傳。"));
        }

        // 檢查檔案類型是否為 Excel
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/vnd.ms-excel") && !contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of("檔案格式不正確，請上傳 Excel 檔案 (.xls 或 .xlsx)。"));
        }

        try {
            List<String> importMessages = userService.importUsersFromExcel(file);
            if (importMessages.stream().anyMatch(msg -> msg.startsWith("錯誤"))) {
                 return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(importMessages);
            }
            return ResponseEntity.ok(importMessages);
        } catch (IOException e) {
            logger.error("處理檔案時發生 IO 錯誤: {}", file.getOriginalFilename(), e); // 記錄例外
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of("處理檔案時發生錯誤：" + e.getMessage()));
        } catch (Exception e) {
            logger.error("匯入過程中發生未知錯誤: {}", file.getOriginalFilename(), e); // 記錄例外
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of("匯入過程中發生未知錯誤：" + e.getMessage()));
        }
    }
}