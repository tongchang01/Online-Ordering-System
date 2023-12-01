package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.dishFlavorsMapper;
import com.sky.result.Result;
import com.sky.service.impl.dishServiceimpl;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/12/1
 * @time15:03
 **/

@RestController("userdishController")
@Slf4j
@Api(tags = "微信菜品接口")
@RequestMapping("/user/dish")
public class dishController {

    @Autowired
    private dishServiceimpl dishService;

    @Autowired
    private dishFlavorsMapper dishFlavorsMapper;

    @GetMapping("/list")
    @ApiOperation(value = "根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId){

        log.info("根据分类id查询菜品{}",categoryId);

        List<Dish> dishList = dishService.list
                (new QueryWrapper<Dish>().eq("category_id", categoryId)
                        .eq("status", StatusConstant.ENABLE));
        //根据分类id查询菜品

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish dish : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish,dishVO);//将dish的属性拷贝到dishVO中

            dishVO.setFlavors(dishFlavorsMapper.selectList
                    (new QueryWrapper<DishFlavor>().eq("dish_id", dish.getId())));
            //根据菜品id查询口味
           dishVOList.add(dishVO);
        }
        return Result.success(dishVOList);
    }
}
