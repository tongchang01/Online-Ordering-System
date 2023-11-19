package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.entity.Dish;
import com.sky.mapper.dishMapper;
import com.sky.service.dishService;
import org.springframework.stereotype.Service;


/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/19
 * @time14:48
 **/
@Service
public class dishServiceimpl extends ServiceImpl<dishMapper, Dish> implements dishService {
}
