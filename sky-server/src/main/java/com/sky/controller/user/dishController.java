package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    @Autowired
    private RedisTemplate redisTemplate;


    @GetMapping("/list")
    @ApiOperation(value = "根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId){
        log.info("根据分类id查询菜品{}",categoryId);
        //23/12/4 15:10对该方法进行优化，将查询结果放入redis中
        //动态构造key
        String key = "dish_" + categoryId;

        //先从redis中查询
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        //1这里调试的时候被误导了，说反序列化失败，其实并没有，还是调试不熟练



        if (list != null && list.size()>0){
            //如果redis中有，就直接返回无需再去访问DB
            return Result.success(list);
        }

        //如果redis中没有，就从数据库中查询
        List<Dish> dishList = dishService.list
                (new QueryWrapper<Dish>().eq("category_id", categoryId)
                        .eq("status", StatusConstant.ENABLE));
        //根据分类id查询菜品

        List<DishVO> dishVOList = new ArrayList<>();
        //2再就是这里是有必要重开一个对象的跟上面，共用一个list变量会导致空指针 但是原因不明
        //说白了就是调试不熟练没玩明白


        for (Dish dish : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish,dishVO);//将dish的属性拷贝到dishVO中

            dishVO.setFlavors(dishFlavorsMapper.selectList
                    (new QueryWrapper<DishFlavor>().eq("dish_id", dish.getId())));
            //根据菜品id查询口味


           dishVOList.add(dishVO);
           //3不重开一个对象比如 list.add(dishVO)会导致空指针


        }
        //将查询结果放入redis中

        redisTemplate.opsForValue().set(key,dishVOList);
        //到这里基本的缓存逻辑完成，但是要保证缓存数据跟DB数据的一致还要加上缓存更新的逻辑
        //再管理端更新逻辑

        return Result.success(list);
    }
}
