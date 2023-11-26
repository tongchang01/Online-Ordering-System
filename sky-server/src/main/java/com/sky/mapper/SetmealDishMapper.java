package com.sky.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 套餐菜品中间表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2023-11-24
 */
@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {


    void insertBatch(List<SetmealDish> setmealDish);
}
