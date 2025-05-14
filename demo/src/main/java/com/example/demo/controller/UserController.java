// filepath: src/main/java/com/example/demo/controller/UserController.java
package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users") // 所有此控制器的端點都會以 /api/users 開頭
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 建立新使用者 (例如，透過 POST 請求到 /api/users)
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserCreateRequest request) {
        // 實際應用中，您可能想為請求建立一個 DTO (Data Transfer Object)
        // 而不是直接使用 User 物件，特別是對於密碼等敏感資訊
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
}

// 一個簡單的請求物件範例，用於建立使用者
// 您可以將這個類別放在一個單獨的檔案中，例如 UserCreateRequest.java
class UserCreateRequest {
    private String username;
    private String password;

    // Getters and Setters
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}