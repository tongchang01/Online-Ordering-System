package com.sky.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface dishMapper extends BaseMapper<Dish> {
}
