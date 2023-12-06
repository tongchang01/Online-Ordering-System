package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.Result;
import com.sky.service.impl.ShoppingCartServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/12/6
 * @time13:23
 **/

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartServiceImpl shoppingCartService;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 添加购物车
     * @param DTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation(value = "添加购物车")
    public Result add(@RequestBody ShoppingCartDTO DTO) {
        log.info("添加购物车:{}",DTO);

        //涉及的表不少改用mb
         shoppingCartService.addShoppingCart(DTO);

        return Result.success();
    }


    @GetMapping("/list")
    @ApiOperation(value = "购物车列表")
    public Result<List<ShoppingCart>> list(){

        Long id = BaseContext.getCurrentId();//获取当前用户id

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(id)
                .build();

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);


        return Result.success(list);
    }
}
