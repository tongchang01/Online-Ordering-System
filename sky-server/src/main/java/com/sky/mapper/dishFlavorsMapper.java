package com.sky.mapper;


import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface dishFlavorsMapper {
    void insertBatch(List<DishFlavor> flavors);
}
