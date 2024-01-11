package com.sky.mapper;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2023-12-08
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    Page<OrderVO> page(@Param("pageResultPage") Page<OrderVO> pageResultPage,
                       @Param("dto") OrdersPageQueryDTO dto,
                       @Param(Constants.WRAPPER)QueryWrapper<OrderVO> wrapper);

    Double sumtodayTurnover(Map map);

    Integer countbyMap(Map map);

    List<GoodsSalesDTO> getGoodsSales(LocalDateTime begin, LocalDateTime end);
}
