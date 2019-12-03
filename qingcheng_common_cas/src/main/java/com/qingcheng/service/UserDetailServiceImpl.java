package com.qingcheng.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 李洋
 * @date 2019/11/23 09:45
 */
public class UserDetailServiceImpl implements UserDetailsService {
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //这个类没有校验用户密码的功能 ,给当前用户赋予权限
        List<GrantedAuthority> grantedAuthorityList = new ArrayList();
        grantedAuthorityList.add(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(username, "", grantedAuthorityList);
    }
}
