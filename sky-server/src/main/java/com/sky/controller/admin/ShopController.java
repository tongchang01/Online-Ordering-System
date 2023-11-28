package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;


/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/28
 * @time14:38
 **/

@Slf4j
@RestController("adminShopController")//这里的名字要改，Bean的名字不能重复
@RequestMapping("/admin/shop")
@Api(tags = "后台商铺状态管理接口")
public class ShopController {

    public static final String KEY = "shop_status";
    //提出常量，避免硬编码

    @Autowired
    private RedisTemplate redisTemplate;

    //这里都没有用那个判断key是否存在的redis语句 所以会一直覆盖

    @PutMapping("/{status}")
    @ApiOperation(value = "修改商铺状态")
    public Result setStatus(@PathVariable Integer status) {
       log.info("修改商铺状态为{}",status==1?"营业中":"打烊中");

       redisTemplate.opsForValue().set(KEY,status);

        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation(value = "管理端获取商铺状态")
    public Result<Integer> getStatus() {


        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(KEY);

        log.info("获取商铺状态{}",shopStatus==1?"营业中":"打烊中");
        return Result.success(shopStatus);
    }
}
