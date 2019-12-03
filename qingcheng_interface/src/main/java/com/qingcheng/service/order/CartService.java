package com.qingcheng.service.order;

import java.util.List;
import java.util.Map;

/**
 * 购物车
 * @author 李洋
 * @date 2019/11/27 14:45
 */
public interface CartService {

    /**
     * 通过用户名从缓存中来获取当前用户的购物车详情
     * @param username
     * @return
     */
    List<Map<String, Object>> findCartList(String username);

    /**
     * 添加商品
     * @param username
     * @param skuId
     * @param num
     */
    void addItem(String username, String skuId, Integer num);

    /**
     * 更新勾选状态
     * @param username
     * @param skuId
     * @param checked
     * @return
     */
    Boolean updateChecked(String username, String skuId, Boolean checked);

    /**
     * 删除选中的购物车
     * @param username
     */
    void deleteCheckedCart(String username);

    /**
     * 计算优惠金额
     * @param username
     * @return
     */
    int preferential(String username);

    /**
     * 重新查找订单,保证金额最新
     * @param username
     * @return
     */
    List<Map<String, Object>> findNewOrderItemList(String username);
}
