package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.entity.DishFlavor;
import com.sky.mapper.dishFlavorsMapper;
import com.sky.result.Result;
import com.sky.service.impl.dishServiceimpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/22
 * @time15:10
 **/
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class dishController {
    @Autowired
    private dishServiceimpl dishService;


    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dto){
       log.info("新增菜品{}",dto);

       //这个新增操作涉及两张表 dish和dish_flavor 也就有事务开启
        //所以就不用mp了
        dishService.add(dto);

        return Result.success();
    }
}
