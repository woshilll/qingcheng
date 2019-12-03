package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.LoginLog;
import com.qingcheng.service.system.LoginLogService;
import com.qingcheng.util.WebUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * @author 李洋
 * @date 2019/10/22 16:39
 */
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Reference
    private LoginLogService loginLogService;

    /**
     * 添加登录日志
     * @param httpServletRequest
     * @param httpServletResponse
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        LoginLog loginLog = new LoginLog();
        //获取登录名
        loginLog.setLoginName(authentication.getName());
        //登录ip
        loginLog.setIp(httpServletRequest.getRemoteAddr());
        //浏览器
        loginLog.setBrowserName(WebUtil.getBrowserName(httpServletRequest.getHeader("user-agent")));
        loginLog.setLoginTime(new Date());
        //ip所在地址
        loginLog.setLocation(WebUtil.getCityByIP(httpServletRequest.getRemoteAddr()));

        loginLogService.add(loginLog);
        httpServletRequest.getRequestDispatcher("/main.html").forward(httpServletRequest, httpServletResponse);
    }
}
