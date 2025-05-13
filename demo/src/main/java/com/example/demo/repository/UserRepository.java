// filepath: src/main/java/com/example/demo/repository/UserRepository.java
package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository 已經提供了基本的 CRUD 方法，例如:
    // ***REMOVED***ve(User entity)
    // findById(Long id)
    // findAll()
    // deleteById(Long id)
    // ...等等

    // 您也可以定義自訂的查詢方法，Spring Data JPA 會根據方法名稱自動產生查詢
    Optional<User> findByUsername(String username);

    // 您還可以使用 @Query 註解來編寫 JPQL 或原生 SQL 查詢
    // @Query("SELECT u FROM User u WHERE u.emailAddress = ?1")
    // User findByEmailAddress(String emailAddress);
}