package com.example.demo.repository;

import com.example.demo.model.User_Info; // 確保 User_Info 實體類別可以被正確引用
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<User_Info, String> {
    // Spring Data JPA 會根據方法名稱自動產生查詢
    // 這個方法會根據 user_id 查詢 User_Info
    // 注意：User_Info 實體中的欄位名稱是 user_id，方法名稱 findByUserId 會對應到 user_id 欄位
    Optional<User_Info> findByUserid(String userId);
}