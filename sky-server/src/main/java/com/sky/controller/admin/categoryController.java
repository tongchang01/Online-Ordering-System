package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.impl.categoryServiceimpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
}
