package com.sky.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.ShoppingCart;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 购物车 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2023-12-06
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {

    List<ShoppingCart> list(ShoppingCart shoppingCart);

}
