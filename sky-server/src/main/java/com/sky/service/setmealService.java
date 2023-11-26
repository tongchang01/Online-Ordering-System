package com.sky.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

public interface setmealService extends IService<Setmeal> {

    PageResult pageQuery(Page<SetmealVO> page, SetmealPageQueryDTO dto, QueryWrapper<SetmealVO> wrapper);

    void add(SetmealDTO dto);

    void updatesetmeal(SetmealDTO dto);
}
