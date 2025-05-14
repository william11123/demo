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
}