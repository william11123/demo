// filepath: src/main/java/com/example/demo/SecurityConfig.java
package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
            .csrf(csrf -> csrf.di***REMOVED***ble());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // 這裡使用 InMemoryUserDetailsManager 作為範例，實際應用中通常會從資料庫讀取使用者資訊
        // 使用者名稱 "user", 密碼 "password" (會被 PasswordEncoder 加密)
        UserDetails user = User.builder()
            .username("user")
            .password(passwordEncoder().encode("123")) // 密碼必須被加密
            .roles("USER")
            .build();

        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("adminpass"))
            .roles("ADMIN", "USER")
            .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 BCrypt 進行密碼加密
        return new BCryptPasswordEncoder();
    }
}