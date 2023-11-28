package com.sky.controller.user;

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
@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags = "后台商铺状态管理接口")
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    public static final String KEY = "shop_status";
    //提出常量，避免硬编码


//    @PutMapping("/{status}")
//    @ApiOperation(value = "修改商铺状态")
//    public Result setStatus(@PathVariable Integer status) {
//       log.info("修改商铺状态为{}",status==1?"营业中":"打烊中");
//
//       redisTemplate.opsForValue().set("shop_status",status);
//
//        return null;
//    }
    //不能让用户端修改

    @GetMapping("/status")
    @ApiOperation(value = "用户端获取商铺状态")
    public Result<Integer> getStatus() {


        Integer shopStatus = (Integer) redisTemplate.opsForValue().get(KEY);

        log.info("获取商铺状态{}",shopStatus==1?"营业中":"打烊中");
        return Result.success(shopStatus);
    }
}
