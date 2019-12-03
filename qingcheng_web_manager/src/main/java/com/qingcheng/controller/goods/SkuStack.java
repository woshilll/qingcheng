package com.qingcheng.controller.goods;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.goods.StockBackService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单异常回滚
 * 每一个小时回滚一次
 * @author 李洋
 * @date 2019/12/2 16:50
 */
@Component
public class SkuStack {

    @Reference
    private StockBackService stockBackService;

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void skuStockBack() {
        System.out.println("回滚开始");
        stockBackService.doBack();
        System.out.println("回滚结束");
    }
}
