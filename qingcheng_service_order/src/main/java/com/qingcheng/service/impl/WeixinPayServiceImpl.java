package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayRequest;
import com.github.wxpay.sdk.WXPayUtil;
import com.github.wxpay.sdk.WXPayXmlUtil;
import com.qingcheng.config.MyWXPayConfig;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.service.order.WeixinPayService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 李洋
 * @date 2019/12/5 16:11
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    @Autowired
    private MyWXPayConfig wxPayConfig;
    @Override
    public Map createNative(String orderId, Integer money, String notifyUrl) {
        try {
            //1.封装请求参数
            Map<String, String> requestMap = new HashMap<String, String>();
            requestMap.put("appid", wxPayConfig.getAppID());
            requestMap.put("mch_id", wxPayConfig.getMchID());
            requestMap.put("nonce_str", WXPayUtil.generateNonceStr());
            requestMap.put("sign", WXPayUtil.generateNonceStr());
            requestMap.put("body", "青橙");
            requestMap.put("out_trade_no", orderId);
            requestMap.put("total_fee", money + "");
            requestMap.put("spbill_create_ip", "127.0.0.1");
            requestMap.put("notify_url", notifyUrl);
            requestMap.put("trade_type", "NATIVE");
            String signedXml = WXPayUtil.generateSignedXml(requestMap, wxPayConfig.getKey());
            //发送请求
            WXPayRequest wxPayRequest = new WXPayRequest(wxPayConfig);
            String xmlResult = wxPayRequest.requestWithCert("/pay/unifiedorder", null, signedXml, false);
            //解析返回结果
            Map<String, String> result = WXPayUtil.xmlToMap(xmlResult);

            System.out.println(xmlResult);
            Map map = new HashMap();
            map.put("code_url", result.get("code_url"));
            map.put("total_fee", money + "");
            map.put("out_trade_no", orderId);
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }

    }
    @Autowired
    private OrderService orderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void notifyLogic(String xml) {
        try {
            Map<String, String> map = WXPayUtil.xmlToMap(xml);
            boolean signatureValid = WXPayUtil.isSignatureValid(map, wxPayConfig.getKey());

            if (signatureValid) {
                if ("SUCCESS".equals(map.get("result_code"))) {
                    rabbitTemplate.convertAndSend("paynotify","",map.get("out_trade_no"));
                    orderService.updatePayStatus(map.get("out_trade_no"), map.get("transaction_id"));

                }else {
                    //记录日志
                }
            }else {
                //记录日志
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
