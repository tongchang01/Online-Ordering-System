package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderDetailMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.impl.OrdersServiceImpl;
import com.sky.service.impl.ShoppingCartServiceImpl;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.util.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/12/8
 * @time13:45
 **/

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = {"用户订单接口"})
@Slf4j
public class OrderController {

    @Autowired
    private OrdersServiceImpl orderService;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartServiceImpl shoppingCartService;

    @Autowired
    private WebSocketServer webSocketServer;

    @PostMapping("/submit")
    @ApiOperation(value = "提交订单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("提交订单 {}",ordersSubmitDTO);

         OrderSubmitVO vo= orderService.submit(ordersSubmitDTO);

        return Result.success(vo);
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);

        /**
         * 由于跳过了支付接口，所以把下单的来电提醒写在这，本应该写在支付成功的回调接口里
         * 通过websocket发送消息给后台：type,orderId,content
         */
        Map map=new HashMap();
        map.put("type",1);//1表示来电提醒,2表示用户催单

        //传来的dto里面有订单号，用订单号查订单id
        Orders orders = orderService.getOne
                (new QueryWrapper<Orders>().eq("number",
                        ordersPaymentDTO.getOrderNumber()));//当前订单
        map.put("orderId",orders.getId());

        map.put("content","订单号："+orders.getNumber());

        //map转json
        String jsonString = JSON.toJSONString(map);

        //发送消息
        webSocketServer.sendToAllClient(jsonString);



        return Result.success(orderPaymentVO);
    }

    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> page(int page, int pageSize, Integer status) {
        Page<OrderVO> pageResultPage = Page.of(page, pageSize);
        //PageResult 里有条数和数据 数据是vo类型的 所以这里的泛型应是vo类型而非其他

        pageResultPage.addOrder(new OrderItem("order_time", false));

        OrdersPageQueryDTO DTO = new OrdersPageQueryDTO();

        Long id = BaseContext.getCurrentId();
        DTO.setUserId(id);
        DTO.setStatus(status);

        QueryWrapper<OrderVO> wrapper = new QueryWrapper<>();
        wrapper.like(DTO.getNumber()!=null,"number",DTO.getNumber());
        wrapper.like(DTO.getPhone()!=null,"phone",DTO.getPhone());
        wrapper.eq(id!=null,"user_id", BaseContext.getCurrentId());
        wrapper.eq(DTO.getStatus()!=null,"status",DTO.getStatus());
        wrapper.ge(DTO.getBeginTime()!=null,"order_time",DTO.getBeginTime());
        wrapper.le(DTO.getEndTime()!=null,"order_time",DTO.getEndTime());


        PageResult pageResult = orderService.pageQueryUser(pageResultPage,DTO,wrapper);
        return Result.success(pageResult);
    }


    @GetMapping("/orderDetail/{id}")
    @ApiOperation("订单详情")
    public Result<OrderVO> orderDetail(@PathVariable("id") Long id) {
        log.info("订单详情：{}", id);

        Orders byId = orderService.getById(id);

        List<OrderDetail> orderDetailList = orderDetailMapper.selectList
                (new QueryWrapper<OrderDetail>().eq("order_id", id));

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(byId, orderVO);
        orderVO.setOrderDetailList(orderDetailList);


        return Result.success(orderVO);
    }


    @PostMapping("/repetition/{id}")
    @ApiOperation("订单重复购买")
    public Result repetition(@PathVariable("id") Long id) {
        log.info("订单重复购买：{}", id);
        //再来一单即重新下单一样的东西，查询原订单的商品详细，转换成购物车对象，再调用购物车的提交订单接口

        //查订单详细表
        QueryWrapper<OrderDetail> wrapper = new QueryWrapper<OrderDetail>()
                .eq("order_id", id);

        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(wrapper);


        //转换成购物车对象
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        for (OrderDetail x : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());

            shoppingCartList.add(shoppingCart);
        }
        /**
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());
        //首先，orderDetailList.stream()将OrderDetail列表转换为一个流。
        // 然后，map函数对流中的每个元素应用一个函数。
        // 这个函数是一个lambda表达式，它接受一个OrderDetail对象（在lambda表达式中被称为x），
        // 并返回一个新的ShoppingCart对象。  在这个lambda表达式中，首先创建了一个新的ShoppingCart对象。
        // 然后，使用Spring的BeanUtils.copyProperties方法
        // 将OrderDetail对象的属性复制到新的ShoppingCart对象中。
        // 注意，"id"属性被排除在外，这意味着OrderDetail对象的id属性不会被复制到ShoppingCart对象中。
        // 然后，设置ShoppingCart对象的userId和createTime属性。
        // userId被设置为当前用户的ID，createTime被设置为当前时间。
        // 最后，collect方法将流中的所有元素收集到一个列表中。
        // 在这个例子中，它将所有的ShoppingCart对象收集到一个列表中，并返回这个列表。
        // 所以，这段代码的结果是一个新的ShoppingCart对象列表，
        // 这些对象的属性（除了id、userId和createTime）与原始的OrderDetail对象相同。
          */

        //把购物车对象传给购物车的DB
        shoppingCartService.saveBatch(shoppingCartList);

        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable("id") Long id) {
        log.info("取消订单：{}", id);

       //校验订单是否存在
        Orders ordersDB = orderService.getById(id);//原订单

        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (ordersDB.getStatus() > 2) {//已接单
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        //这里new对象 用于后面更新 这个更新操作实际上是在给原订单的部分字段赋值
        //所以用不着拷贝对象

        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {//待接单
            //调用微信支付退款接口   跳过了支付接口，所以这里不用退款
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());

        orderService.updateById(orders);//更新订单

        return Result.success();
    }




}
