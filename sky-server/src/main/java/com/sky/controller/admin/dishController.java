package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.dishFlavorsMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.impl.dishServiceimpl;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

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


    /**
     *
     * @param dto
     * @return
     * //这里是多表联查 而且要二次封装结果 不建议使用mp
     * //所以要自定义sql但是依然使用mp提供的分页插件
     * //把mapper接口的形参第一个设置为 page 但在sql语句中不会使用到  mp会自动分页
     * //mapper里的方法有两个及以上参数时必须加@Param
     * //且再xml里要用注解的标识的名字点属性 即 dto.name ...
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dto){
      log.info("菜品分页查询{}",dto);
        Page<DishVO> page = Page.of(dto.getPage(), dto.getPageSize());

        page.addOrder(new OrderItem("create_time",false));

        QueryWrapper<DishVO> wrapper = new QueryWrapper<>();
        wrapper.like(dto.getName()!=null,"name",dto.getName())
                .eq(dto.getCategoryId()!=null,"category_id",dto.getCategoryId())
                .eq(dto.getStatus()!=null,"status",dto.getStatus());
        /**
         * 遗留问题 用wrapper构造条件 拼接进sql里只有category_id 生效 其他不生效
         * 实在没办法了 索性直接全部写xml
         */

        PageResult pageResult =dishService.pageQuery(page,dto,wrapper);

        return Result.success(pageResult);

    }

    //单个或批量删除
    //起售状态不能删除
    //被套餐关联的菜品不能删除
    //删除菜品后 它关联的口味也应一并删除
    //涉及到3张表 dish dish_flavor setmeal_dish
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    //@RequestParam：将请求参数绑定到你控制器的方法参数上（是springmvc中接收普通参数的注解）
    public Result delids(@RequestParam List<Long> ids){
        log.info("批量删除菜品{}",ids);

        dishService.deleteBatch(ids);

        return Result.success();
    }
}
