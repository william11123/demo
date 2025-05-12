package com.example.demo.controller // 確認 package

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {

    @GetMapping("/") // 確認映射
    fun home(model: Model): String {
        model.addAttribute("message", "歡迎來到我的網站！")
        return "index" // 確認視圖名稱
    }
}