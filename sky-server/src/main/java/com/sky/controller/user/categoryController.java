package com.sky.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.impl.categoryServiceimpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/12/1
 * @time14:47
 **/

@Slf4j
@Api(tags = "微信端分类接口")
@RestController("userCategoryController")
@RequestMapping("/user/category")
public class categoryController {
    @Autowired
    private categoryServiceimpl categoryService;


    @GetMapping("/list")
    @ApiOperation("用户分类查询")
    public Result<List<Category>> list(Integer type){

        log.info("用户分类查询参数{}",type);

        QueryWrapper<Category> wrapper = new QueryWrapper<Category>()
                .eq(type!=null,"type", type);
        List<Category> list = categoryService.list(wrapper);

        return Result.success(list);

    }


}
