package com.sky.task;

/**
 * @user TYB童以滨
 * @email Tong-yinbin@outlook.com
 * @date2024/1/6
 * @time13:16
 **/

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 开启spring task 注解后  自定义定时任务类
 */
@Slf4j
@Component//交给spring容器管理
public class MyTask {

    @Autowired
    private OrdersMapper ordersMapper;
    ;

    /**
     * 类里写具体的定时任务方法
     */
    //测试定时任务

    //cron表达式,和正则表达式类似写起来比较麻烦，网上有生成器建议用那个
    /**
     @Scheduled(cron = "0/5 * * * * ?")//每隔5秒执行一次
     public void testTask() {
     log.info("执行定时任务:{}",new Date());
     }
     测试成功
     */

    /**
     * 该类要实现本项目的定时任务定时处理主要的需求如下：
     * 1.下单后，用户一直未支付，导致订单处于待支付状态下，超过15分钟，订单自动取消
     * 2.用户收到货之后，管理端为点击完成按钮，导致订单处于派送中状态下，超过7天，订单自动完成
     * <p>
     * 实现思路：
     * 1.每分钟查询一次订单表，查询出所有处于待支付状态下的订单，判断订单创建时间和当前时间的差值是否大于15分钟，
     * 如果大于15分钟，修改订单状态为已取消
     * 2.每天凌晨4点查询一次订单表，查询出所有处于派送中状态下的订单，把他的状态设置为已完成
     * 其实该功能应该优化在打样模块里 等之后有时间再优化，先实现功能
     */

    /**
     * @Scheduled
     * 注意 @Scheduled 注解只能用于无参方法
     * 这里本来应该把 status，orderTime 两个参数写成形参
     * 再去mapper里调用，为了后续回顾 方便一眼直接看完写在一起了
     *
     * 这种写法并不规范
     */

    //处理超时订单
    @Scheduled(cron = "0 * * * * ? *")//每分钟执行一次
    //@Scheduled(cron = "0/5 * * * * ?")//每隔5秒执行一次 用于测试
    public void processOverTimeOrder() {
        log.info("处理超时订单:{}", LocalDateTime.now());

        Integer status = Orders.PENDING_PAYMENT;//待支付状态

        LocalDateTime orderTime = LocalDateTime.now().minusMinutes(15);//订单创建时间减去15分钟

        //查询超时订单
        //select * from orders where  order_status = 1 and create_time < now() - interval 15 minute
        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.eq("status", status);
        wrapper.lt("order_time", orderTime);

        List<Orders> list = ordersMapper.selectList(wrapper);

        if (list.size() > 0 && list != null) {
            //修改订单状态为已取消
            for (Orders orders : list) {
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("超时未支付，系统自动取消");//取消原因
                orders.setCancelTime(LocalDateTime.now());//取消时间
                ordersMapper.updateById(orders);
            }
        }

    }

    /**
     * 处理长时间处于派送中的订单
     */
    @Scheduled(cron = "0 0 4 * * ?")//每天凌晨4点执行一次
    //@Scheduled(cron = "0/10 * * * * ?")//每隔5秒执行一次 用于测试
    public void processUnfinishedOrder() {
        log.info("处理时间处于派送中的订单:{}", LocalDateTime.now());

        Integer status = Orders.DELIVERY_IN_PROGRESS;//派送中状态

        //订单创建时间减去4小时，处理上一个工作日的订单
        LocalDateTime orderTime = LocalDateTime.now().minusHours(4);

        QueryWrapper<Orders> wrapper = new QueryWrapper<>();
        wrapper.eq("status", status);
        wrapper.lt("order_time", orderTime);

        List<Orders> list = ordersMapper.selectList(wrapper);

        if (list.size() > 0 && list != null) {
            //修改订单状态为已完成
            for (Orders orders : list) {
                orders.setStatus(Orders.COMPLETED);
                ordersMapper.updateById(orders);
            }
        }

    }

}
