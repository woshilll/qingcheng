package com.qingcheng.pojo.order;

import java.io.Serializable;
import java.util.List;

/**
 * @author 李洋
 * @date 2019/10/16 11:13
 */
public class OrderAndOrderItem implements Serializable {
    private Order order;
    private List<OrderItem> orderItems;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}
