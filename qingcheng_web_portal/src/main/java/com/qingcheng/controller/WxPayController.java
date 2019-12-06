package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.order.WeixinPayService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author 李洋
 * @date 2019/12/5 16:35
 */
@RestController
@RequestMapping("/wxpay")
public class WxPayController {

    @Reference
    private OrderService orderService;

    @Reference
    private WeixinPayService weixinPayService;

    @GetMapping("/createNative")
    public Map createNative(String orderId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderService.findById(orderId);
        if (order != null) {
            if ("0".equals(order.getPayStatus()) && "0".equals(order.getOrderStatus()) && username.equals(order.getUsername())) {
                return weixinPayService.createNative(orderId, order.getPayMoney(), "http://qingchengwoshilll.easy.echosite.cn/wxpay/notify.do");
            }
        }
        return null;
    }

    @PostMapping("/notify")
    public void notifyLogic(HttpServletRequest request) {
        System.out.println("支付成功回调");
        ServletInputStream inputStream = null;
        try {
            inputStream = request.getInputStream();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            outputStream.close();
            inputStream.close();
            String result = new String(outputStream.toByteArray(), "utf-8");
            weixinPayService.notifyLogic(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
