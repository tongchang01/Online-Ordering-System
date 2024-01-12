package com.sky.mapper;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface dishMapper extends BaseMapper<Dish> {
    @Insert("")
    @AutoFill(OperationType.INSERT)
    void add(Dish dish);


    /**
     *
     * @param page
     * @param dto
     * @return
     * mapper里的方法有两个及以上参数时必须加@Param
     * 且再xml里要用注解的标识的名字点属性 即 dto.name ...
     */
    Page<DishVO> pageQuery(@Param("page") Page<DishVO> page, @Param("dto")DishPageQueryDTO dto,
                           @Param(Constants.WRAPPER) QueryWrapper<DishVO> wrapper);

    //这里是多表联查 而且要二次封装结果 不建议使用mp
    //所以要自定义sql但是依然使用mp提供的分页插件
    //把mapper接口的形参第一个设置为 page 但在sql语句中不会使用到 mp会自动分页

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
