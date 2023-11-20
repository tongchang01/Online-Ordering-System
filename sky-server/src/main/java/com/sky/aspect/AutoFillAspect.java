package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/20
 * @time13:44
 *
 * 自定义切面类 实现公共字段自动填充的处理逻辑
 *以aop 实现 其实mp也有自动填充功能很方便
 *  * 但是aop用的少在这里再次练习
 *
 *  注意这里之前写的empcontroller 之类的两个控制层
 *  我把业务逻辑都写在控制层了 为了方便我自己看
 *  controller的方法形参是dto之类的二次加工类 他里面是没有这些公共字段的
 *  逻辑是用dto接收前端发的数据 再自己新建原实体类对象加工这些公共字段
 *  只有是形参是原实体类，类中有这些公共字段才行
 *
 *  再开发中 mapper里的接口是接收最后加工好的类 也就是原实体类
 *  所以自定义注解应该放在mapper的接口上
 *  我大部分用的mp又把逻辑写再controller里的 所以这个模块功能用不了
 *  只有empcontroller里的新增方法使用原mabaits写的所以可以
 *  经这个方法测试 这个功能是没问题的
 *
 *  约定实体对象形参列表的放在第一位
 **/

@Aspect//标识为切面类
@Component
@Slf4j
public class AutoFillAspect {

//    切入点
//    @Pointcut("@annotation(com.sky.aspect.AutoFill)")//标识为被@AutoFill标识的生效
//    public void autoFillPointcut(){}
    //切入点较多时 用这个标识方便一些  这里就一个就不用了

    @Before("@annotation(com.sky.annotation.AutoFill)")//前置通知
    public void autofill(JoinPoint joinPoint){
        //JoinPoint连接点 即被注解修饰的方法以反射获取到他 通过代理 调用该方法
        log.info("开启进行公共字段填充");

        //开始填充逻辑
        //1 获取当前被拦截到的方法上的@AutoFill里的操作类型时插入或更新
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        //方法签名对象

        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);
        //获取方法上的注解

        OperationType operationType = annotation.value();//获取注解里的value是insert 还是update

        //2获取被拦截的方法的形参--实体对象
        Object[] args = joinPoint.getArgs();
        if (args==null || args.length==0){
            return;
        }
        //约定实体对象放在第一位
        Object entity = args[0];

        //3准备赋值数据
        LocalDateTime now = LocalDateTime.now();
        Long id = BaseContext.getCurrentId();
        BaseContext.removeCurrentId();

        //4根据不同的操作类型，以反射赋值
        if (operationType==OperationType.INSERT){
            try {
                //以反射获取原对象里的set方法
                Method setCreateTime = entity.getClass()
                        .getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass()
                        .getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass()
                        .getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass()
                        .getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //以反射给原对象属性赋值
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,id);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (operationType==OperationType.UPDATE) {
            try {
                //以反射获取原对象里的set方法
                Method setUpdateTime = entity.getClass()
                        .getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass()
                        .getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                //以反射给原对象属性赋值
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }


}
