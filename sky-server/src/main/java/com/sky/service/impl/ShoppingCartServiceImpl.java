package com.sky.service.impl;


import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.dishMapper;
import com.sky.mapper.setmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.IShoppingCartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 购物车 服务实现类
 * </p>
 *
 * @author author
 * @since 2023-12-06
 */
@Service
@Slf4j
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements IShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private dishMapper dishMapper;
    @Autowired
    private setmealMapper setmealMapper;
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        //1先判断当前要添加的商品是否已经存在购物车中，且每个用户的购物车是独立的以用户id为条件
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);

        Long id = BaseContext.getCurrentId();//获取当前用户id
        shoppingCart.setUserId(id);


        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //根据用户id，和菜品id，或是加上套餐查询
        //相同的用户id，相同的菜品id，相同的套餐id 这样查只能有一条数据有负数的话就改数量
        //按照上述的条件查询只有两种结果，要么存在一条要么不存在，不会说有多条数据

        //2如果存在则更新数量
        if (list != null && list.size() > 0) {
            ShoppingCart Cart = list.get(0);//所以在这里直接取唯一的第一条数据，不会再有其他的数据

            Cart.setNumber(Cart.getNumber() + 1);//相同的商品则数量加一

            shoppingCartMapper.updateById(Cart);//更新DB数据

        } else {
            //3如果不存在则添加
            //判断本次添加的数据是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) { //如果是菜品
                shoppingCart.setDishId(dishId);
                Dish dish = dishMapper.selectById(dishId);
                //添加缺失的几个字段
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
//                shoppingCart.setNumber(1);//第一次添加默认数量为1
//                shoppingCart.setCreateTime(LocalDateTime.now());


            }else {//如果是套餐
                Long setmealId = shoppingCartDTO.getSetmealId();
                Setmeal setmeal = setmealMapper.selectById(setmealId);
                //添加缺失的几个字段
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());


            }
            shoppingCart.setNumber(1);//第一次添加默认数量为1
            shoppingCart.setCreateTime(LocalDateTime.now());

            shoppingCartMapper.insert(shoppingCart);//插入处理好的数据

        }
    }
}
