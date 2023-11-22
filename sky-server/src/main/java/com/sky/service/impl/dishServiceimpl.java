package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
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

    @Override
    @Transactional
    public void add(DishDTO dto) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dto,dish);

        //向dish里插入数据
        dishMapper.add(dish);

        //向dish_f里插入数据
        Long dishId = dish.getId();//获取insert自动生成的主键值

        List<DishFlavor> flavors = dto.getFlavors();
        if (!flavors.isEmpty()){
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            dishFlavorsMapper.insertBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(Page<DishVO> page,DishPageQueryDTO dto) {
        //设置page参数
        //这里是多表联查 而且要二次封装结果 不建议使用mp
        //所以要自定义sql但是依然使用mp提供的分页插件
        //把mapper接口的形参第一个设置为 page 但在sql语句中不会使用到 mp会自动分页

        Page<DishVO> pageQuery = dishMapper.pageQuery(page,dto);
        for (DishVO record : pageQuery.getRecords()) {
            System.out.println(record);
        }
        return new PageResult(pageQuery.getTotal(),pageQuery.getRecords());
    }


}
