// filepath: src/main/java/com/example/demo/SecurityConfig.java
package com.example.demo;

import com.example.demo.model.User_Info; // 匯入 Kotlin 實體
import com.example.demo.repository.UserInfoRepository; // 匯入您的 Repository

import java.util.Arrays; // <-- 【修正點】新增這個 import
import java.util.List;   // <-- 【修正點】新增這個 import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority; // <-- 新增 import
import org.springframework.security.core.authority.SimpleGrantedAuthority; // <-- 新增 import
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
                    // 只有 ADMIN 角色的使用者可以訪問 /api/users/upload-excel
                    .requestMatchers("/api/predict-uploads/upload-excel").hasRole("A") 
                    // 假設其他 /api/users/** 路徑需要 ADMIN 或 USER 角色
                    .requestMatchers("/api/users/**").hasAuthority("ACCESS_API")
                    .requestMatchers("/public/**", "/login").permitAll() // 公開路徑和登入頁面允許所有人存取
                    .requestMatchers("/api/location/**").permitAll()
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
            .csrf(csrf -> csrf.disable())
            .userDetailsService(userDetailsService()); // <--- 確保這裡使用了您定義的 userDetailsService Bean  
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> { // 'username' 參數對應到您實體中的 'user_id'
            User_Info user = userInfoRepository.findByUserid(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username (user_id): " + username));
            // 1. 建立使用者自己的角色 (例如 "ROLE_A", "ROLE_B" 等)
            GrantedAuthority userRole = new SimpleGrantedAuthority("ROLE_" + user.getSeclevel().toUpperCase());
            // 2. 建立一個所有 a,b,c,d 等級使用者都共有的通用權限
            GrantedAuthority accessApiAuthority = new SimpleGrantedAuthority("ACCESS_API");

            // 3. 將角色和權限都放入一個列表中
            List<GrantedAuthority> authorities = Arrays.asList(userRole, accessApiAuthority);

            return new org.springframework.security.core.userdetails.User(
                    user.getUserid(),
                    user.getPassword(),
                    authorities         
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