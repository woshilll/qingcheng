package com.qingcheng.service.order;

import java.util.Map;

/**
 * @author 李洋
 * @date 2019/12/5 16:07
 */
public interface WeixinPayService {
    /**
     * 生成微信支付二维码
     * @param orderId 订单号
     * @param money 金额
     * @param notifyUrl 回调地址
     * @return
     */
    public Map createNative(String orderId, Integer money, String notifyUrl);

    /**
     * 支付回调
     * @param xml
     */
    void notifyLogic(String xml);
}
