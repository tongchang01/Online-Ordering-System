package com.sky.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.SetmealDish;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    @Select("select sd.name,sd.copies,d.image,d.description " +
            "from setmeal_dish sd left join dish d  on sd.dish_id = d.id " +
            "where setmeal_id=#{id}")
    List<DishItemVO> dishList(Long id);
}
