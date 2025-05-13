// filepath: src/main/java/com/example/demo/service/UserService.java
package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder; // 假設您有密碼加密器
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
        return userRepository.save(newUser); // 使用 save 方法儲存使用者到資料庫
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
}