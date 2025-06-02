// filepath: src/main/java/com/example/demo/SecurityConfig.java
package com.example.demo;

import com.example.demo.model.User_Info; // 匯入 Kotlin 實體
import com.example.demo.repository.UserInfoRepository; // 匯入您的 Repository

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder; // <--- 匯入 NoOpPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;


import java.util.ArrayList; // 用於 authorities
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired // <--- 加入 Autowired 註解
    private UserInfoRepository userInfoRepository; // <--- 宣告並注入 UserInfoRepository

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/public/**", "/login","/api/users/**").permitAll() // 公開路徑和登入頁面允許所有人存取
                    .anyRequest().authenticated() // 其他所有請求都需要驗證
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login") // 指定自訂登入頁面的路徑
                    .defaultSuccessUrl("/welcome", true) // 登入成功後的預設導向頁面
                    .permitAll()
            )
            .logout(logout ->
                logout
                    .permitAll()
            )
            .csrf(csrf -> csrf.di***REMOVED***ble())
            .userDetailsService(userDetailsService()); // <--- 確保這裡使用了您定義的 userDetailsService Bean  
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> { // 'username' 參數對應到您實體中的 'user_id'
            User_Info user = userInfoRepository.findByUserid(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username (user_id): " + username));
            // 注意：這裡的 authorities 可以根據您的 User_Info 模型中的角色欄位來設定 (如果有的話)
            // 目前範例使用空的權限列表
            return new org.springframework.security.core.userdetails.User(
                    user.getUserid(),
                    user.getPassword(),
                    new ArrayList<>() // 您可以根據需要添加權限
            );
        };
    }
  @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 NoOpPasswordEncoder 來處理明文密碼
        // 這告訴 Spring Security 密碼未經加密，不執行任何編碼/解碼操作
        return NoOpPasswordEncoder.getInstance(); // <--- 修改為 NoOpPasswordEncoder
    }
}