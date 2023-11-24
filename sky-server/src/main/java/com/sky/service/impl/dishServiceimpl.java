package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.dishMapper;
import com.sky.result.PageResult;
import com.sky.service.dishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/19
 * @time14:48
 **/
@Service
public class dishServiceimpl extends ServiceImpl<dishMapper, Dish> implements dishService {

    @Autowired
    private dishMapper dishMapper;
    @Autowired
    private com.sky.mapper.dishFlavorsMapper dishFlavorsMapper;
    @Autowired
    private dishServiceimpl dishServiceimpl;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void add(DishDTO dto) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dto, dish);

        //向dish里插入数据
        dishMapper.add(dish);

        //向dish_f里插入数据
        Long dishId = dish.getId();//获取insert自动生成的主键值

        List<DishFlavor> flavors = dto.getFlavors();
        if (!flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorsMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(Page<DishVO> page, DishPageQueryDTO dto, QueryWrapper<DishVO> wrapper) {
        //
        //设置page参数
        //这里是多表联查 而且要二次封装结果 不建议使用mp
        //所以要自定义sql但是依然使用mp提供的分页插件
        //把mapper接口的形参第一个设置为 page 但在sql语句中不会使用到 mp会自动分页

        Page<DishVO> pageQuery = dishMapper.pageQuery(page, dto, wrapper);

        List<DishVO> records = pageQuery.getRecords();
        return new PageResult(pageQuery.getTotal(), pageQuery.getRecords());
    }

    /**
     * //单个或批量删除
     * //起售状态不能删除
     * //被套餐关联的菜品不能删除
     * //删除菜品后 它关联的口味也应一并删除
     * //涉及到3张表 dish dish_flavor setmeal_dish
     *
     * @param ids
     */
    @Override
    @Transactional//涉及到3张表 开启事务
    public void deleteBatch(List<Long> ids) {
        //起售状态不能删除
        List<Dish> dishList = dishServiceimpl.listByIds(ids);//取出全菜品

        //起售状态不能删除
        for (Dish dish : dishList) {
            Integer status = dish.getStatus();
            if (status == StatusConstant.ENABLE) {//起售的
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
                //抛出异常
            }
        }
        //被套餐关联的菜品不能删除 查SetmealDish 表里 dishid的每一列 中setmeal_id不为空
        QueryWrapper<SetmealDish> wrapper = new QueryWrapper<>();
        wrapper.in("dish_id",ids);
        //select setmeal_id form setmeal_dish where id in ids
        //ids 为动态sql <foreach collection="ids" item="dishId" separator="," open="(" close=")">
        //              #{disId}
        //             </foreach>

        List<SetmealDish> setmealDishes = setmealDishMapper.selectList(wrapper);
        if (!setmealDishes.isEmpty()){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        dishServiceimpl.removeByIds(ids);
        //删除菜品表数据

        QueryWrapper<DishFlavor> flavorQueryWrapper = new QueryWrapper<>();
        flavorQueryWrapper.in("dish_id",ids);
        dishFlavorsMapper.delete(flavorQueryWrapper);


    }


}
