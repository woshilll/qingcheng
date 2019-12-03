package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.Admin;
import com.qingcheng.service.system.AdminService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 李洋
 * @date 2019/10/21 16:45
 */
public class UserDetailsServiceImpl implements UserDetailsService {


    @Reference
    private AdminService adminService;
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //构建角色集合 ，项目中此处应该是根据用户名查询用户的角色列表
        List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Map<String, Object> map = new HashMap<String, Object>(2);
        map.put("loginName",s);
        map.put("status","1");
        List<Admin> admins = adminService.findList(map);
        if (admins == null || admins.size() == 0) {
            return null;
        }
        return new User(s,admins.get(0).getPassword(),grantedAuths);
    }
}
