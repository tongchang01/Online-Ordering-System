package com.sky.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/17
 * @time12:54
 **/
@Configuration
public class mppageconfig {

    /**
     * 添加分页插件
     * 这个方法取自mp官网
     * 有什么忘记了直接去看官网很详细
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        //新建插件对象 这个对象托管着mp全部的插件功能

        //指定对象操作的数据库为mysql
        //配置对象具体参数有很多 这里没有别的需求就不写了具体去官网
        PaginationInnerInterceptor innerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);

        interceptor.addInnerInterceptor(innerInterceptor);
        // 添加插件 使其生效

        return interceptor;
    }
}
