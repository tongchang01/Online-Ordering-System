package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.Result;
import com.sky.service.impl.setmealServiceimpl;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/12/1
 * @time15:35
 **/
@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "微信套餐接口")
public class SetmealController {

    @Autowired
    private setmealServiceimpl setmealService;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @GetMapping("/list")
    @ApiOperation(value = "根据分类id查询套餐")
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId")
    //@Cacheable 查询时先从缓存中查询，如果没有再从数据库中查询，并将查询结果缓存
    //key:缓存的key，#代表形参，如果是多个形参，可以用#p0,#p1 也可以用#result.XX 代表返回值的XX属性
    //这里的key最终是setmealCache.categoryId的一个目录
    //cacheNames:缓存的名字，可以指定多个缓存名字，如果指定了多个缓存名字，那么就会将数据缓存到多个缓存中
    public Result<List<Setmeal>> list(Long categoryId) {

        List<Setmeal> list = setmealService.list
                (new QueryWrapper<Setmeal>().eq("category_id", categoryId)
                        .eq("status", StatusConstant.ENABLE));//查询状态为启用的套餐


        return Result.success(list);
    }

    @GetMapping("/dish/{id}")
    @ApiOperation(value = "根据套餐id查询包含的菜品")
    public Result<List<DishItemVO>> dishlist(@PathVariable Long id) {

        List<DishItemVO> dishItemVOList = setmealDishMapper.dishList(id);

        return Result.success(dishItemVOList);
    }
}
