package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.user.Address;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.user.AddressService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 李洋
 * @date 2019/11/27 14:51
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @GetMapping("/findCartList")
    public List<Map<String, Object>> findCartList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
       return cartService.findCartList(username);
    }

    @GetMapping("/addItem")
    public Result addItem(String skuId, Integer num) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.addItem(username, skuId, num);
        return new Result();
    }

    @GetMapping("/buy")
    public void buy(HttpServletResponse response, String skuId, Integer num) throws IOException {
        addItem(skuId, num);
//        response.sendRedirect("http://localhost:9102/cart.html");
        response.sendRedirect("http://qingchengwoshilll.easy.echosite.cn/cart.html");
    }

    @GetMapping("/updateChecked")
    public Result updateChecked(String skuId, Boolean checked) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.updateChecked(username, skuId, checked);
        return new Result();
    }

    @GetMapping("/deleteCheckedCart")
    public Result deleteCheckedCart() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.deleteCheckedCart(username);
        return new Result();
    }

    /**
     * 计算当前购物车的优惠金额
     * @return
     */
    @GetMapping("/preferential")
    public Map preferential() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        int preferential = cartService.preferential(username);
        Map map = new HashMap();
        map.put("preferential", preferential);
        return map;
    }

    @GetMapping("/findNewOrderItems")
    public List<Map<String, Object>> findNewOrderItems() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return cartService.findNewOrderItemList(username);
    }

    @Reference
    private AddressService addressService;

    /**
     * 通过用户名查找地址
     * @return
     */
    @GetMapping("/findAddressList")
    public List<Address> findAddressList() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return addressService.findAddressList(username);
    }

    @Reference
    private OrderService orderService;

    @PostMapping("/saveOrder")
    public Map<String, Object> saveOrder(@RequestBody Order order) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setUsername(username);
        return orderService.add(order);
    }
}
