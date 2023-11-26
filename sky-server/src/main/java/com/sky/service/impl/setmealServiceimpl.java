package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.setmealMapper;
import com.sky.result.PageResult;
import com.sky.service.setmealService;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/19
 * @time15:23
 **/
@Service
public class setmealServiceimpl extends ServiceImpl<setmealMapper, Setmeal> implements setmealService {

    @Autowired
    private setmealServiceimpl setmealService;
    @Autowired
    private setmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    public PageResult pageQuery(Page<SetmealVO> page, SetmealPageQueryDTO dto,
                                QueryWrapper<SetmealVO> wrapper) {

        Page<SetmealVO> pageQuery = setmealMapper.pageQuery(page, dto, wrapper);


        return new PageResult(pageQuery.getTotal(), pageQuery.getRecords());
    }

    @Override
    @Transactional
    public void add(SetmealDTO dto) {
        //新增套餐 1.新增套餐表数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(dto, setmeal);
        setmeal.setCreateUser(BaseContext.getCurrentId());
        setmeal.setCreateTime(LocalDateTime.now());
        setmeal.setStatus(StatusConstant.DISABLE);//新增的套餐默认是停用状态
        setmealService.save(setmeal);

        // 2.新增套餐与菜品关联表数据
        Long setmealId = setmeal.getId();//获取自动生成的主键值

        List<SetmealDish> setmeal_dish = dto.getSetmealDishes();
        if (!dto.getSetmealDishes().isEmpty()) {
            for (SetmealDish setmealDish : setmeal_dish) {
                setmealDish.setSetmealId(setmealId);//设置套餐id 与菜品id 关联

                setmealDishMapper.insert(setmealDish);
            }
        }
    }

    @Override
    public void updatesetmeal(SetmealDTO dto) {
        //修改套餐 1.修改套餐表数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(dto, setmeal);
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmeal.setUpdateTime(LocalDateTime.now());
        setmealService.updateById(setmeal);

        //删除套餐与菜品关联表数据
        setmealDishMapper.delete(new QueryWrapper<SetmealDish>()
                .eq("setmeal_id", dto.getId()));

        // 2.修改套餐与菜品关联表数据
        Long setmealId = setmeal.getId();//获取自动生成的主键值

        List<SetmealDish> setmeal_dish = dto.getSetmealDishes();
        if (!dto.getSetmealDishes().isEmpty()) {
            for (SetmealDish setmealDish : setmeal_dish) {
                setmealDish.setSetmealId(setmealId);//设置套餐id 与菜品id 关联

                setmealDishMapper.updateById(setmealDish);
            }
        }


    }


}
