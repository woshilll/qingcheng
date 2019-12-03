package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.pojo.goods.Category;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.PreferentialService;
import com.qingcheng.utils.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 李洋
 * @date 2019/11/27 14:47
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Reference
    private SkuService skuService;


    @Override
    public List<Map<String, Object>> findCartList(String username) {
        List<Map<String, Object>> cartList = (List<Map<String, Object>>) redisTemplate.boundHashOps(CacheKey.CART_LIST).get(username);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void addItem(String username, String skuId, Integer num) {
        //获取购物车
        List<Map<String, Object>> cartList = findCartList(username);
        boolean flag = false;
        for (Map<String, Object> map : cartList) {
            OrderItem orderItem = (OrderItem) map.get("item");
            //购物车中存在该商品
            if (orderItem.getSkuId().equals(skuId)) {
                if (orderItem.getNum() <= 0) {
                    cartList.remove(map);
                    flag = true;
                    break;
                }

                int weight = orderItem.getWeight() / orderItem.getNum();
                //设置数量
                orderItem.setNum(orderItem.getNum() + num);
                //设置总价
                orderItem.setMoney(orderItem.getPrice() * orderItem.getNum());
                //设置重量
                orderItem.setWeight(weight * orderItem.getNum());


                if (orderItem.getNum() <= 0) {
                    flag = true;
                    cartList.remove(map);
                    break;
                }

                flag = true;
                break;
            }
        }
        //当未在缓存中找到该商品
        if (!flag) {
            Sku sku = skuService.findById(skuId);
            if (sku == null) {
                throw new RuntimeException("未找到该商品");
            }
            if (!"1".equals(sku.getStatus())) {
                throw new RuntimeException("商品状态不合法");
            }
            if (num <= 0) {
                throw new RuntimeException("商品数量不合法");
            }
            //设置商品item
            OrderItem orderItem = new OrderItem();
            orderItem.setSkuId(skuId);
            orderItem.setSpuId(sku.getSpuId());

            orderItem.setCategoryId3(sku.getCategoryId());
            Category category3 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(sku.getCategoryId());
            orderItem.setCategoryId2(category3.getParentId());
            Category category2 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(category3.getParentId());
            orderItem.setCategoryId2(category2.getParentId());
            orderItem.setImage(sku.getImage());
            orderItem.setNum(num);
            orderItem.setPrice(sku.getPrice());
            orderItem.setMoney(sku.getPrice() * num);
            orderItem.setName(sku.getName());
            if (sku.getWeight() == null) {
                sku.setWeight(0);
            }
            orderItem.setWeight(sku.getWeight() * num);
            Map<String, Object> map = new HashMap<String, Object>(2);
            map.put("item", orderItem);
            //默认为选中状态
            map.put("checked", true);
            cartList.add(map);
        }
        //更新缓存
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username, cartList);
    }

    @Override
    public Boolean updateChecked(String username, String skuId, Boolean checked) {
        List<Map<String, Object>> cartList = findCartList(username);
        Boolean isOk = false;
        for (Map<String, Object> map : cartList) {
            OrderItem orderItem = (OrderItem) map.get("item");
            if (orderItem.getSkuId().equals(skuId)) {
                map.put("checked", checked);
                isOk = true;
                break;
            }
        }
        if (isOk) {
            redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username, cartList);
        }
        return isOk;
    }

    @Override
    public void deleteCheckedCart(String username) {
        List<Map<String, Object>> cartList = findCartList(username).stream().filter(cart ->
                !(Boolean) cart.get("checked")
        ).collect(Collectors.toList());
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username, cartList);
    }

    @Autowired
    private PreferentialService preferentialService;

    @Override
    public int preferential(String username) {
        //得到所有被选中的OrderItem集合
        List<OrderItem> orderItems = findCartList(username).stream().filter(cart ->
                (Boolean) cart.get("checked")
        ).map(cart -> (OrderItem) cart.get("item"))
                .collect(Collectors.toList());
        //以categoryId3分组
        Map<Integer, IntSummaryStatistics> cartMap = orderItems.stream().collect(Collectors.groupingBy(OrderItem::getCategoryId3, Collectors.summarizingInt(OrderItem::getMoney)));
        //优惠金额
        int preferential = 0;
        for (Integer categoryId : cartMap.keySet()) {
            //得到该分类下的金钱总和
            int money = (int) cartMap.get(categoryId).getSum();
            int preferentialMoney = preferentialService.findPreMoneyByCategoryId(categoryId, money);
            preferential += preferentialMoney;
        }
        return preferential;
    }

    /**
     * 更新购物车中的金额
     * @param username
     * @return
     */
    @Override
    public List<Map<String, Object>> findNewOrderItemList(String username) {
        List<Map<String, Object>> cartList = findCartList(username);
        for (Map<String, Object> cart : cartList) {
            OrderItem orderItem = (OrderItem) cart.get("item");
            Sku sku = skuService.findById(orderItem.getSkuId());
            //更新金额
            orderItem.setPrice(sku.getPrice());
            orderItem.setMoney(sku.getPrice() * orderItem.getNum());
        }
        //重新放到缓存
        redisTemplate.boundHashOps(CacheKey.CART_LIST).put(username, cartList);
        return cartList;
    }
}
