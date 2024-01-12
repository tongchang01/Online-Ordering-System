package com.sky.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 套餐mapper
 */
@Mapper
public interface setmealMapper extends BaseMapper<Setmeal> {
    Page<SetmealVO> pageQuery(@Param("page") Page<SetmealVO> page,
                              @Param("dto") SetmealPageQueryDTO dto,
                              @Param(Constants.WRAPPER) QueryWrapper<SetmealVO> wrapper);

    /**
     * 根据条件统计套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
