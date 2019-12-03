package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.SkuMapper;
import com.qingcheng.dao.StockBackMapper;
import com.qingcheng.pojo.goods.StockBack;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.StockBackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author 李洋
 * @date 2019/12/2 16:27
 */
@Service(interfaceClass = StockBackService.class)
public class StockBackServiceImpl implements StockBackService {

    @Autowired
    private StockBackMapper stockBackMapper;

    @Transactional(rollbackFor = Exception.class)
    public void addList(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            StockBack stockBack = new StockBack();
            stockBack.setOrderId(orderItem.getOrderId());
            stockBack.setSkuId(orderItem.getSkuId());
            stockBack.setStatus("0");
            stockBack.setCreateTime(new Date());
            stockBack.setNum(orderItem.getNum());

            stockBackMapper.insert(stockBack);
        }
    }

    @Autowired
    private SkuMapper skuMapper;

    @Transactional(rollbackFor = Exception.class)
    public void doBack() {
        //查询回滚表中状态为0的
        StockBack stockBack = new StockBack();
        stockBack.setStatus("0");
        List<StockBack> stockBackList = stockBackMapper.select(stockBack);
        for (StockBack stock : stockBackList) {
            //添加库存
            skuMapper.deductionStock(stock.getSkuId(), -stock.getNum());
            //减去销量
            skuMapper.addSaleNum(stock.getSkuId(), -stock.getNum());

            stock.setStatus("1");
            stockBackMapper.updateByPrimaryKey(stock);

        }
    }
}
