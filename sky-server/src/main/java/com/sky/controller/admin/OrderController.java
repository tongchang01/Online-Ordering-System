package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.OrderDetail;
import com.sky.mapper.OrdersMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.impl.OrdersServiceImpl;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
               String dishes = orderDetail.getName()+"*"+ orderDetail.getNumber()+";";

               record.setOrderDishes(dishes);

            }

        }

        return Result.success(new PageResult(total, records));
    }
}
