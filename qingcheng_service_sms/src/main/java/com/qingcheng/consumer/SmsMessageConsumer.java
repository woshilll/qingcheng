package com.qingcheng.consumer;

import com.alibaba.fastjson.JSON;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author 李洋
 * @date 2019/11/19 17:28
 */
public class SmsMessageConsumer implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;
    public void onMessage(Message message) {
        String json = new String(message.getBody());
        Map<String, String> map = JSON.parseObject(json, Map.class);
        String phone = map.get("phone");
        String code = map.get("code");
        System.out.println("手机号为：" + phone + "   " + "验证码为：" + code);
        smsUtil.sendSms(phone, code);
    }
}
