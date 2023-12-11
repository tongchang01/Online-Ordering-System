package com.sky.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;

import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;

import com.sky.service.IOrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.sky.utils.WeChatPayUtil;


/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author author
 * @since 2023-12-08
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {

        //提前处理业务异常（地址簿信息不全，购物车为空）
        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            //抛出异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        Long id = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setId(id);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null) {
            //抛出异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //前端校验过用户的数据，避免有人用其他方式提交请求比如postman这里再次校验


        //1向订单表中插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()) + id);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(id);
        orders.setAddress(addressBook.getDetail());

        ordersMapper.insert(orders);
        Long ordersId = orders.getId();


        //2向订单详情表中插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();

        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(ordersId);//设置订单id 逻辑外键

            orderDetailList.add(orderDetail);
        }


        orderDetailMapper.insertBatch(orderDetailList);


        //3清空购物车
        QueryWrapper<ShoppingCart> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", id);
        shoppingCartMapper.delete(wrapper);

        //4封装返回数据
        OrderSubmitVO submitVO = OrderSubmitVO.builder()
                .id(ordersId)
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();


        return submitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.selectById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));

        // 模拟支付成功 跳过微信支付
        //个人测试号没法申请微信的支付接口，所以这里模拟支付成功
        OrderPaymentVO vo = new OrderPaymentVO();
        vo.setNonceStr("666");
        vo.setPaySign("hhh");
        vo.setPackageStr("prepay_id=wx");
        vo.setSignType("RSA");
        vo.setTimeStamp("1670380960");

        return vo;
    }

    @Override
    public void paySuccess(String outTradeNo) {
        // 根据订单号查询订单
        Long id = BaseContext.getCurrentId();
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.eq("number", outTradeNo)
                .eq("user_id", id);
        Orders ordersDB = ordersMapper.selectOne(wrapper);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        ordersMapper.updateById(orders);
    }

    @Override
    public PageResult pageQueryUser(Page<OrderVO> pageResultPage, OrdersPageQueryDTO dto, QueryWrapper<OrderVO> wrapper) {

        Page<OrderVO> pageQuery = ordersMapper.page(pageResultPage, dto, wrapper);

        List<OrderVO> records = pageQuery.getRecords();
        long total = pageQuery.getTotal();

        if (records != null) {
            for (OrderVO record : records) {
                Long id = record.getId();
                QueryWrapper<OrderDetail> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("order_id", id);
                List<OrderDetail> detailList = orderDetailMapper.selectList(queryWrapper);
                record.setOrderDetailList(detailList);


            }
        }

        return new PageResult(total, records);
    }


    /**
     * 订单搜索
     * @param orderVOPage
     * @param dto
     * @param wrapper
     */



}
