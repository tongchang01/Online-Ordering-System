package com.sky.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderSubmitVO;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author author
 * @since 2023-12-08
 */
public interface IOrdersService extends IService<Orders> {

    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);
}
