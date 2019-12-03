package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;
import com.qingcheng.service.order.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时管理
 * @author 李洋
 * @date 2019/10/16 13:03
 */
@Component
public class OrderTask {
    @Reference
    private OrderService orderService;

    @Scheduled(cron = "0 0/2 * * * ?")
    public void orderTimeOutLogic() {
        orderService.orderTimeOutLogic();
    }


    @Reference
    private CategoryReportService categoryReportService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void createData() {
        categoryReportService.createData();
    }
}
