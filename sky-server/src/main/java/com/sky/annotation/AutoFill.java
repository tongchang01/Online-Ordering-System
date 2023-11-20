package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解 用于自动填充公共字段
 * 创建时间 修改时间 操作用户 等 公共字段
 * 以aop 实现 其实mp也有自动填充功能很方便
 * 但是aop用的少在这里再次练习
 */
@Target(ElementType.METHOD)//指定该注解只能在方法上
@Retention(RetentionPolicy.RUNTIME)//运行时生效
public @interface AutoFill {

    //数据库操作类型 插入，更新
    OperationType value();
    //插入和修改 要填充的字段类型不一样 所以要设置指定本次的操作类型是那种
}
