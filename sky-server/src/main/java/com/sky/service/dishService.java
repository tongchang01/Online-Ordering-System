package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;

public interface dishService extends IService<Dish> {
    void add(DishDTO dto);
}
