package com.sky.controller.user;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.impl.OrdersServiceImpl;
import com.sky.vo.OrderSubmitVO;
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
}
