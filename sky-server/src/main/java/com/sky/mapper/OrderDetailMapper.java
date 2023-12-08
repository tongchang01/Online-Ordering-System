package com.sky.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 订单明细表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2023-12-08
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {

    void insertBatch(List<OrderDetail> orderDetailList);
}
