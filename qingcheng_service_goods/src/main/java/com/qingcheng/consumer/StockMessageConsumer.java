package com.qingcheng.consumer;

import com.alibaba.fastjson.JSON;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.StockBackService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author 李洋
 * @date 2019/12/2 16:31
 */
public class StockMessageConsumer implements MessageListener {
    @Autowired
    private StockBackService stockBackService;
    public void onMessage(Message message) {
        try {
            String json = new String(message.getBody());
            List<OrderItem> orderItemList = JSON.parseArray(json, OrderItem.class);
            //异常订单回滚处理
            stockBackService.addList(orderItemList);
        } catch (Exception e) {
            e.printStackTrace();
            //人工处理, 记录日志
        }
    }
}
