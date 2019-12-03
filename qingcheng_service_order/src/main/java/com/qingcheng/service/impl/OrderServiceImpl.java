package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.OrderConfigMapper;
import com.qingcheng.dao.OrderItemMapper;
import com.qingcheng.dao.OrderLogMapper;
import com.qingcheng.dao.OrderMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.*;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.OrderService;
import com.qingcheng.utils.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 返回全部记录
     *
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 分页查询
     *
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    @Override
    public PageResult<Order> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        Page<Order> orders = (Page<Order>) orderMapper.selectAll();
        return new PageResult<Order>(orders.getTotal(), orders.getResult());
    }

    /**
     * 条件查询
     *
     * @param searchMap 查询条件
     * @return
     */
    @Override
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        if (searchMap.get("ids") != null) {
            example.createCriteria().andIn("id", Arrays.asList((Integer[]) searchMap.get("ids")));
        }
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     *
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        Page<Order> orders = (Page<Order>) orderMapper.selectByExample(example);
        return new PageResult<Order>(orders.getTotal(), orders.getResult());
    }

    /**
     * 根据Id查询
     *
     * @param id
     * @return
     */
    @Override
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    @Autowired
    private CartService cartService;
    @Reference
    private SkuService skuService;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 新增
     *
     * @param order
     */
    @Override
    public Map<String, Object> add(Order order) {
        //1.获取选中的购物车
        List<OrderItem> orderItemList = cartService.findNewOrderItemList(order.getUsername()).stream().filter(
                cart -> (boolean) cart.get("checked")
        ).map(cart -> (OrderItem) cart.get("item")).collect(Collectors.toList());

        //2.扣减库存
        if (!skuService.deductionStock(orderItemList)) {
            throw new RuntimeException("库存不足");
        }
        try {
            //3.保存订单主表
            order.setId(idWorker.nextId() + "");

            IntStream moneyStream = orderItemList.stream().mapToInt(OrderItem::getMoney);
            IntStream numStream = orderItemList.stream().mapToInt(OrderItem::getNum);
            int preMoney = cartService.preferential(order.getUsername());
            int totalMoney = moneyStream.sum();
            int totalNum = numStream.sum();

            //总金额
            order.setTotalMoney(totalMoney);
            //总数量
            order.setTotalNum(totalNum);
            //优惠金额
            order.setPreMoney(preMoney);
            //支付金额
            order.setPayMoney(totalMoney - preMoney);
            //创建时间
            order.setCreateTime(new Date());
            //订单状态
            order.setOrderStatus("0");
            //支付状态
            order.setPayStatus("0");
            //发货状态
            order.setConsignStatus("0");

            orderMapper.insert(order);

            //4.保存订单明细
            //打折比例
            double proportion = (double)(order.getPayMoney() / order.getTotalMoney());
            for (OrderItem orderItem : orderItemList) {
                orderItem.setId(idWorker.nextId() + "");
                orderItem.setOrderId(order.getId());
                orderItem.setPayMoney((int) (proportion * orderItem.getMoney()));
                orderItemMapper.insert(orderItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //当订单发生异常,发送消息队列进行回滚操作
            rabbitTemplate.convertAndSend("", "queue.skuback", JSON.toJSONString(orderItemList));
            throw new RuntimeException("订单失败");
        }
        //5.清除购物车
        cartService.deleteCheckedCart(order.getUsername());

        Map<String, Object> map = new HashMap<>();
        map.put("ordersn", order.getId());
        map.put("money", order.getPayMoney());
        return map;
    }

    /**
     * 修改
     *
     * @param order
     */
    @Override
    public void update(Order order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 通过订单id查询订单信息和详细信息
     *
     * @param id
     * @return
     */
    @Override
    public OrderAndOrderItem findAllMsg(String id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        Example example = new Example(OrderItem.class);
        example.createCriteria().andEqualTo("orderId", id);
        List<OrderItem> orderItems = orderItemMapper.selectByExample(example);
        OrderAndOrderItem orderAndOrderItem = new OrderAndOrderItem();
        orderAndOrderItem.setOrder(order);
        orderAndOrderItem.setOrderItems(orderItems);
        return orderAndOrderItem;
    }

    @Autowired
    private OrderLogMapper orderLogMapper;

    /**
     * 批量发货
     *
     * @param orders
     */
    @Override
    public void batchSend(List<Order> orders) {
        //判断运单号和物流公司是否为空
        for (Order order :
                orders) {
            if (order.getShippingCode() == null || order.getShippingName() == null) {
                throw new RuntimeException("请填写快递公司和快递单号");
            }
        }
        //循环订单
        for (Order order :
                orders) {
            //订单状态
            order.setOrderStatus("3");
            //发货状态
            order.setConsignStatus("2");
            //发货时间
            order.setConsignTime(new Date());
            //更新
            orderMapper.updateByPrimaryKeySelective(order);

            //记录日志信息
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId() + "");
            orderLog.setConsignStatus(order.getConsignStatus());
            orderLog.setOperateTime(new Date());
            orderLog.setOrderId(order.getId());
            orderLog.setOrderStatus(order.getOrderStatus());
            orderLog.setPayStatus(order.getPayStatus());

            //TODO 支付流水号


            orderLogMapper.insert(orderLog);

        }

    }

    @Autowired
    private OrderConfigMapper orderConfigMapper;
    @Override
    public void orderTimeOutLogic() {
        //订单超时未付款自动关闭
        //查询超时时间
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey(1);
        //超时时间  分  60
        Integer orderTimeout = orderConfig.getOrderTimeout();
        //得到超时的时间点
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(orderTimeout);

        Example example = new Example(Order.class);
        //未付款，未删除
        example.createCriteria().andLessThan("createTime", localDateTime)
                .andEqualTo("orderStatus","0")
                .andEqualTo("isDelete","0");
        List<Order> orders = orderMapper.selectByExample(example);
        for (Order order : orders) {
            //记录订单变动日志
            OrderLog orderLog = new OrderLog();
            orderLog.setOperater("system");
            orderLog.setOperateTime(new Date());
            orderLog.setOrderStatus("4");
            orderLog.setPayStatus(order.getPayStatus());
            orderLog.setConsignStatus(order.getConsignStatus());
            orderLog.setRemarks("超时订单，系统自动关闭");
            orderLog.setOrderId(order.getId());
            orderLogMapper.insert(orderLog);

            //更改订单状态
            order.setOrderStatus("4");
            order.setCloseTime(new Date());
            orderMapper.updateByPrimaryKeySelective(order);
        }

    }

    /**
     * 构建查询条件
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 订单id
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andLike("id", "%" + searchMap.get("id") + "%");
            }
            // 支付类型，1、在线支付、0 货到付款
            if (searchMap.get("payType") != null && !"".equals(searchMap.get("payType"))) {
                criteria.andLike("payType", "%" + searchMap.get("payType") + "%");
            }
            // 物流名称
            if (searchMap.get("shippingName") != null && !"".equals(searchMap.get("shippingName"))) {
                criteria.andLike("shippingName", "%" + searchMap.get("shippingName") + "%");
            }
            // 物流单号
            if (searchMap.get("shippingCode") != null && !"".equals(searchMap.get("shippingCode"))) {
                criteria.andLike("shippingCode", "%" + searchMap.get("shippingCode") + "%");
            }
            // 用户名称
            if (searchMap.get("username") != null && !"".equals(searchMap.get("username"))) {
                criteria.andLike("username", "%" + searchMap.get("username") + "%");
            }
            // 买家留言
            if (searchMap.get("buyerMessage") != null && !"".equals(searchMap.get("buyerMessage"))) {
                criteria.andLike("buyerMessage", "%" + searchMap.get("buyerMessage") + "%");
            }
            // 是否评价
            if (searchMap.get("buyerRate") != null && !"".equals(searchMap.get("buyerRate"))) {
                criteria.andLike("buyerRate", "%" + searchMap.get("buyerRate") + "%");
            }
            // 收货人
            if (searchMap.get("receiverContact") != null && !"".equals(searchMap.get("receiverContact"))) {
                criteria.andLike("receiverContact", "%" + searchMap.get("receiverContact") + "%");
            }
            // 收货人手机
            if (searchMap.get("receiverMobile") != null && !"".equals(searchMap.get("receiverMobile"))) {
                criteria.andLike("receiverMobile", "%" + searchMap.get("receiverMobile") + "%");
            }
            // 收货人地址
            if (searchMap.get("receiverAddress") != null && !"".equals(searchMap.get("receiverAddress"))) {
                criteria.andLike("receiverAddress", "%" + searchMap.get("receiverAddress") + "%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if (searchMap.get("sourceType") != null && !"".equals(searchMap.get("sourceType"))) {
                criteria.andLike("sourceType", "%" + searchMap.get("sourceType") + "%");
            }
            // 交易流水号
            if (searchMap.get("transactionId") != null && !"".equals(searchMap.get("transactionId"))) {
                criteria.andLike("transactionId", "%" + searchMap.get("transactionId") + "%");
            }
            // 订单状态
            if (searchMap.get("orderStatus") != null && !"".equals(searchMap.get("orderStatus"))) {
                criteria.andLike("orderStatus", "%" + searchMap.get("orderStatus") + "%");
            }
            // 支付状态
            if (searchMap.get("payStatus") != null && !"".equals(searchMap.get("payStatus"))) {
                criteria.andLike("payStatus", "%" + searchMap.get("payStatus") + "%");
            }
            // 发货状态
            if (searchMap.get("consignStatus") != null && !"".equals(searchMap.get("consignStatus"))) {
                criteria.andLike("consignStatus", "%" + searchMap.get("consignStatus") + "%");
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andLike("isDelete", "%" + searchMap.get("isDelete") + "%");
            }

            // 数量合计
            if (searchMap.get("totalNum") != null) {
                criteria.andEqualTo("totalNum", searchMap.get("totalNum"));
            }
            // 金额合计
            if (searchMap.get("totalMoney") != null) {
                criteria.andEqualTo("totalMoney", searchMap.get("totalMoney"));
            }
            // 优惠金额
            if (searchMap.get("preMoney") != null) {
                criteria.andEqualTo("preMoney", searchMap.get("preMoney"));
            }
            // 邮费
            if (searchMap.get("postFee") != null) {
                criteria.andEqualTo("postFee", searchMap.get("postFee"));
            }
            // 实付金额
            if (searchMap.get("payMoney") != null) {
                criteria.andEqualTo("payMoney", searchMap.get("payMoney"));
            }

        }
        return example;
    }

}
