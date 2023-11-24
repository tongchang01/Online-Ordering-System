package com.sky.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface dishFlavorsMapper extends BaseMapper<DishFlavor> {
    void insertBatch(List<DishFlavor> flavors);
}
