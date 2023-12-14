package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.impl.OrdersServiceImpl;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/12/11
 * @time19:36
 **/

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Api(tags = {"后台订单接口"})
public class OrderController {

    @Autowired
    private OrdersServiceImpl orderService;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;


    @GetMapping("/conditionSearch")
    @ApiOperation(value = "订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO DTO) {
        log.info("订单搜索,{}", DTO);

        Page<OrderVO> orderVOPage = Page.of(DTO.getPage(),
                DTO.getPageSize());

        orderVOPage.addOrder(new OrderItem("order_time", false));


        Long id = BaseContext.getCurrentId();
        DTO.setUserId(id);

        QueryWrapper<OrderVO> wrapper = new QueryWrapper<>();
        wrapper.like(DTO.getNumber() != null, "number", DTO.getNumber());
        wrapper.like(DTO.getPhone() != null, "phone", DTO.getPhone());
        //wrapper.eq(id != null, "user_id", BaseContext.getCurrentId());
        //这里是后台查应查所有的订单，且两个id对不上一个是用户id，一个是admin的id
        wrapper.eq(DTO.getStatus() != null, "status", DTO.getStatus());
        wrapper.ge(DTO.getBeginTime() != null, "order_time", DTO.getBeginTime());
        wrapper.le(DTO.getEndTime() != null, "order_time", DTO.getEndTime());

        PageResult pageResult = orderService.pageQueryUser(orderVOPage, DTO, wrapper);
        //返回条数与数据

        List<OrderVO> records = pageResult.getRecords();
        long total = pageResult.getTotal();


        //把订单详情的菜品名字和数量拼接成字符串展示
        for (OrderVO record : records) {
            List<OrderDetail> orderDetailList = record.getOrderDetailList();
            for (OrderDetail orderDetail : orderDetailList) {
                String dishes = orderDetail.getName() + "*" + orderDetail.getNumber() + ";";

                record.setOrderDishes(dishes);

            }

        }

        return Result.success(new PageResult(total, records));
    }


    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        log.info("各个状态的订单数量统计");

        //分别查询待接单、待派送、派送中 状态的 订单数量 即 2，3，4
        Integer toBeConfirmed = ordersMapper.selectCount
                (new QueryWrapper<Orders>().eq("status", Orders.TO_BE_CONFIRMED));

        Integer CONFIRMED = ordersMapper.selectCount
                (new QueryWrapper<Orders>().eq("status", Orders.CONFIRMED));

        Integer deliveryInProgress = ordersMapper.selectCount
                (new QueryWrapper<Orders>().eq("status", Orders.DELIVERY_IN_PROGRESS));

        OrderStatisticsVO vo = new OrderStatisticsVO();
        vo.setToBeConfirmed(toBeConfirmed);
        vo.setConfirmed(CONFIRMED);
        vo.setDeliveryInProgress(deliveryInProgress);

        return Result.success(vo);
    }


    @GetMapping("/details/{id}")
    @ApiOperation("订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        log.info("订单详情：{}", id);

        Orders byId = orderService.getById(id);

        List<OrderDetail> orderDetailList = orderDetailMapper.selectList
                (new QueryWrapper<OrderDetail>().eq("order_id", id));

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(byId, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return Result.success(orderVO);
    }


    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersDTO dto) {
        log.info("接单：{}", dto);//只穿来了订单id

        //根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(dto.getId())
                .status(Orders.CONFIRMED)

                .build();

        ordersMapper.updateById(orders);

        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO dto) {
        log.info("拒单：{}", dto);//只穿来了订单id

        Orders ordersDB = ordersMapper.selectById(dto.getId());

        //订单状态为2时（待接单）才能拒单
        if (ordersDB.getStatus() != Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //本应退款 但是跳过支付了 就不必退款了


        //根据订单id更新订单的状态、取消原因，取消时间
        Orders orders = Orders.builder()
                .id(dto.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(dto.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();

        ordersMapper.updateById(orders);

        return Result.success();
    }


    @PutMapping("/cancel")
    @ApiOperation("商家取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO dto) {
        log.info("取消订单：{}", dto);

        Orders ordersDB = ordersMapper.selectById(dto.getId());//原订单


        //这里本应判断订单状态 看是否要退款 但是跳过支付了 就不必退款了


        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //更新订单状态、取消原因、取消时间
        Orders orders = Orders.builder()
                .id(dto.getId())
                .status(Orders.CANCELLED)
                .cancelReason(dto.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();

        ordersMapper.updateById(orders);

        return Result.success();
    }


    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id) {
        log.info("派送订单：{}", id);

        Orders ordersDB = ordersMapper.selectById(id);//原订单

        // 校验订单是否存在，并且状态为3（已接单）才能派送
        if (ordersDB == null || ordersDB.getStatus() != Orders.CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }


        //根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();

        ordersMapper.updateById(orders);

        return Result.success();
    }


    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id) {
        log.info("完成订单：{}", id);

        Orders ordersDB = ordersMapper.selectById(id);//原订单

        // 校验订单是否存在，并且状态为4（派送中）才能完成
        if (ordersDB == null || ordersDB.getStatus() != Orders.DELIVERY_IN_PROGRESS) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();

        ordersMapper.updateById(orders);
        return Result.success();

    }

}
