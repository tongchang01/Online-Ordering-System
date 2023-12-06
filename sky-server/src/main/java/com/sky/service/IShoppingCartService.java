package com.sky.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

/**
 * <p>
 * 购物车 服务类
 * </p>
 *
 * @author author
 * @since 2023-12-06
 */
public interface IShoppingCartService extends IService<ShoppingCart> {

    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

}
