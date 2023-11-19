package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.impl.categoryServiceimpl;
import com.sky.service.impl.dishServiceimpl;
import com.sky.service.impl.setmealServiceimpl;
import com.sky.service.setmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.ir.BaseNode;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/18
 * @time16:15
 **/
@RestController
@Slf4j
@Api(tags = "套餐分类相关接口")
public class categoryController {
    @Autowired
    private categoryServiceimpl categoryService;

    @Autowired
    private dishServiceimpl dishService;

    @Autowired
    private setmealServiceimpl setmealService;


    /**
     * 套餐分页查询
     *
     * @param queryDTO
     * @return
     */
    @ApiOperation("套餐分页查询")
    @GetMapping("/admin/category/page")
    public Result page(CategoryPageQueryDTO queryDTO) {

        log.info("套餐分页参数{} ", queryDTO);

        Page<Category> page1 = Page.of(queryDTO.getPage(), queryDTO.getPageSize());

        page1.addOrder(new OrderItem("id", true));

        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .like(queryDTO.getName() != null, "name", queryDTO.getName())
                .eq(queryDTO.getType() != null, "type", queryDTO.getType());

        Page<Category> categoryPage = categoryService.page(page1, queryWrapper);

        List<Category> records = categoryPage.getRecords();
        long total = categoryPage.getTotal();

        PageResult pageResult = new PageResult(total, records);


        return Result.success(pageResult);
    }

    @PostMapping("/admin/category")
    @ApiOperation("新增分类")
    public Result add(@RequestBody CategoryDTO dto) {
        log.info("新增分类{}", dto);

        Category category = new Category();
        BeanUtils.copyProperties(dto, category);
        category.setStatus(0);//默认禁用
        category.setCreateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        BaseContext.removeCurrentId();
        categoryService.save(category);

        return Result.success();

    }

    /**
     * 修改分类信息
     * @param dto
     * @return
     */
    @PutMapping("/admin/category")
    @ApiOperation("修改分类信息")
    public Result updata (@RequestBody CategoryDTO dto ){
        log.info("修改分类信息{}",dto);
        Category category = new Category();
        BeanUtils.copyProperties(dto, category);

        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        BaseContext.removeCurrentId();

        categoryService.updateById(category);

        return Result.success();
    }

    /**
     * 分类启用禁用
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/admin/category/status/{status}")
    @ApiOperation("分类启用禁用")
    public Result StartOrStop(@PathVariable Integer status, Long id){

        log.info("启用禁用分类{} {}",status,id);
        Category category = Category.builder()
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .id(id)
                .status(status)
                .build();
        BaseContext.removeCurrentId();

        categoryService.updateById(category);

        return Result.success();
    }

    /**
     * 删除分类
     * @param id
     * @return
     */
    @DeleteMapping("/admin/category")
    @ApiOperation("删除分类")
    public Result del (Integer id){
        log.info("删除分类{}",id);

        //删除分类 要先判断该分类下有无数据(菜品或套餐) 有的话拒绝删除
        QueryWrapper<Dish> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id",id);
        List<Dish> dishList = dishService.list(wrapper);

        QueryWrapper<Setmeal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id",id);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        if (!dishList.isEmpty() || !setmealList.isEmpty()){
            return Result.error("该分类下存在数据,拒绝删除操作");
        }else {
            categoryService.removeById(id);

            return Result.success();
        }

    }
    //todo 这里差一个根据id查询 后面用到了在写

}
