package com.sky.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface dishService extends IService<Dish> {
    void add(DishDTO dto);


    PageResult pageQuery(Page<DishVO> page, DishPageQueryDTO dto, QueryWrapper<DishVO> wrapper);

    void deleteBatch(List<Long> ids);
}
