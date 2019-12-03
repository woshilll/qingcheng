package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.service.system.AdminService;
import com.qingcheng.util.BCrypt;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 李洋
 * @date 2019/10/22 16:19
 */
@RestController
@RequestMapping("/login")
public class LoginController {
    @Reference
    private AdminService adminService;


    @GetMapping("/name")
    public Map getName() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("name", name);
        return map;
    }

    @GetMapping("/checkPwd")
    public Boolean checkPwd(String password) {
        boolean checkpw = BCrypt.checkpw(password, getAdmin().getPassword());
        return checkpw;
    }

    @PostMapping("/update")
    public Result update( String newPassword) {
        Admin admin = getAdmin();
        String gensalt = BCrypt.gensalt();
        String hashpw = BCrypt.hashpw(newPassword, gensalt);
        admin.setPassword(hashpw);
        boolean checkpw = BCrypt.checkpw(newPassword, hashpw);
        System.out.println(checkpw);
        adminService.updatePwd(admin);
        return new Result();
    }

    private Admin getAdmin() {
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> map = new HashMap<>();
        map.put("loginName", loginName);
        List<Admin> list = adminService.findList(map);
        return list.get(0);
    }

    public static void main(String[] args) {
        String gensalt = BCrypt.gensalt();
        String hashpw = BCrypt.hashpw("654321", gensalt);
        System.out.println(hashpw);
        boolean checkpw = BCrypt.checkpw("654321", hashpw);
        System.out.println(checkpw);

    }

}
