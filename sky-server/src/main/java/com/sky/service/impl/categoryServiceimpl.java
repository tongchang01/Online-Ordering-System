package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.entity.Category;
import com.sky.mapper.categoryMapper;
import org.springframework.stereotype.Service;
import com.sky.service.categoryService;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/18
 * @time16:10
 **/
@Service
public class categoryServiceimpl extends ServiceImpl<categoryMapper, Category> implements categoryService {
}
