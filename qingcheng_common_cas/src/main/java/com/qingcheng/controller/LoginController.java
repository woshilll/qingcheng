package com.qingcheng.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 李洋
 * @date 2019/11/23 10:18
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    @GetMapping("/username")
    public Map<String, String> getUsername() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, String> map = new HashMap<String, String>();
        if ("anonymousUser".equals(username)) {
            //未登录
            username = "";
        }
        map.put("username", username);
        return map;
    }
}
