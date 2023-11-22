package com.sky.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface dishMapper extends BaseMapper<Dish> {
    @Insert("")
    @AutoFill(OperationType.INSERT)
    void add(Dish dish);
}
