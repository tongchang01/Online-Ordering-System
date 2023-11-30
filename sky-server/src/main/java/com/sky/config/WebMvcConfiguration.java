package com.sky.config;

import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")//拦截所有admin下的请求
                .excludePathPatterns("/admin/employee/login");//放行登录请求

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")//拦截所有user下的请求
                .excludePathPatterns("/user/user/login")//放行登录请求
                //和商铺状态请求"/user/user/stayus"
                .excludePathPatterns("/user/shop/status");//放行商铺状态请求


    }


    /**
     * knife4j 常用注解
     * @Api 用在类上 controller 对类说明
     * @ApiModel 用在类上 entity dto vo 对类说明
     * @ApiModelProperty 用在属性上 描述属性
     * @Api 用在方法上
     */
    /**
     * 通过knife4j生成接口文档
     * @return
     */
    @Bean
    public Docket docketAdmin() {
        log.info("准备生成admin接口文档");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("admin端接口")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.admin"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    @Bean
    public Docket docketUser() {
        log.info("准备生成user接口文档");
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .groupName("user端接口")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller.user"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始设置静态资源映射");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * 扩展消息转换器功能
     * 统一转换日期格式
     * @param converters the list of configured converters to extend
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //super.extendMessageConverters(converters);

        log.info("日期格式反序列化为Json转换器启动");

        //新建消息转换器
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        //需要给消息转换器 设置一个对象转换器类 使其可以对java或是json相互序列化
        //这个对象转换器 不用手写 网上很多直接复制JacksonObjectMapper是写好的
        converter.setObjectMapper(new JacksonObjectMapper());

        //将写好的消息转换器交由框架管理
        converters.add(0,converter);//设置index为0以提高优先级 优先使用该转换器
    }
}
