package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.impl.OrdersServiceImpl;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        wrapper.like(DTO.getNumber()!=null,"number",DTO.getNumber());
        wrapper.like(DTO.getPhone()!=null,"phone",DTO.getPhone());
        wrapper.eq(id!=null,"user_id", BaseContext.getCurrentId());
        wrapper.eq(DTO.getStatus()!=null,"status",DTO.getStatus());
        wrapper.ge(DTO.getBeginTime()!=null,"order_time",DTO.getBeginTime());
        wrapper.le(DTO.getEndTime()!=null,"order_time",DTO.getEndTime());


        PageResult pageResult = orderService.pageQueryUser(pageResultPage,DTO,wrapper);
        return Result.success(pageResult);
    }
}
