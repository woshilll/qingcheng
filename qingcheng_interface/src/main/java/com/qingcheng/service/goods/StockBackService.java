package com.qingcheng.service.goods;

import com.qingcheng.pojo.order.OrderItem;

import java.util.List;

/**
 * @author 李洋
 * @date 2019/12/2 16:18
 */
public interface StockBackService {
    /**
     * 添加需要回滚的信息
     * @param orderItemList
     */
    void addList(List<OrderItem> orderItemList);

    /**
     * 回滚操作
     */
    void doBack();
}
