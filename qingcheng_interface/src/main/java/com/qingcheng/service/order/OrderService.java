package com.qingcheng.service.order;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderAndOrderItem;

import java.util.*;

/**
 * order业务逻辑层
 */
public interface OrderService {


    public List<Order> findAll();


    public PageResult<Order> findPage(int page, int size);


    public List<Order> findList(Map<String,Object> searchMap);


    public PageResult<Order> findPage(Map<String,Object> searchMap,int page, int size);


    public Order findById(String id);

    public Map<String, Object> add(Order order);


    public void update(Order order);


    public void delete(String id);

    /**
     * 查询订单的详细信息
     * @param id
     * @return
     */
    OrderAndOrderItem findAllMsg(String id);

    /**
     * 批量发货
     * @param orders
     */
    void batchSend(List<Order> orders);

    /**
     * 订单超时处理
     */
    void orderTimeOutLogic();

    /**
     * 修改订单支付状态
     * @param orderId
     * @param transactionId 交易流水号
     */
    void updatePayStatus(String orderId, String transactionId);
}
