// filepath: src/main/java/com/example/demo/MvcConfig.java
package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login"); // 將 /login 路徑對應到 login.html
        // 可以加入其他簡單的視圖控制器
        registry.addViewController("/welcome").setViewName("welcome"); // 假設您有一個 welcome.html
    }
}