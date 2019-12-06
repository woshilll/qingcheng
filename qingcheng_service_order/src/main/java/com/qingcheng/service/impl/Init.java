package com.qingcheng.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 李洋
 * @date 2019/12/5 14:54
 */
@Component
public class Init implements InitializingBean {
//    @Autowired
//    private RabbitTemplate rabbitTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        //rabbitTemplate.convertAndSend("","queue.skuback","");

    }
}
