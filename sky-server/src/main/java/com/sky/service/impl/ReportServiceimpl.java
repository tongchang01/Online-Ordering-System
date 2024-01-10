package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Struct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @user TYB童以滨
 * @email Tong-yinbin@outlook.com
 * @date2024/1/9
 * @time13:58
 **/

@Service
@Slf4j
public class ReportServiceimpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     *
     * @param begin
     * @param end
     * @return
     * 日期处理方法
     */
    public ArrayList<LocalDate> dateList(LocalDate begin, LocalDate end){

        //大前提，begin<end
        if (begin.isAfter(end)){
            throw new RuntimeException("开始时间不能大于结束时间");
        }

        ArrayList<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)){//计算日期
            begin=begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {

        ArrayList<LocalDate> dateList = dateList(begin, end);//日期处理方法

        ArrayList<Double> turnoverlist = new ArrayList<>();//存放每天的营业额
        for (LocalDate date : dateList) {
            //查询每天的营业额,需是已完成的订单。
            //select sum(amount) from orders where status=5 and order_time >beginDateTime
            // and order_time<endDateTime;

            //开始时间为当天的0点，结束时间为当天的23点59分59秒
            LocalDateTime beginDateTime = LocalDateTime.of(date, LocalDateTime.MIN.toLocalTime());
            LocalDateTime endDateTime = LocalDateTime.of(date, LocalDateTime.MAX.toLocalTime());

            //设置查询条件
            Map map = new HashMap<>();
            map.put("status",Orders.COMPLETED);
            map.put("begin",beginDateTime);
            map.put("end",endDateTime);

            //查询营业额
            Double turnover = ordersMapper.sumtodayTurnover(map);//这里查不到会返回null不合理
            if (turnover==null){
                turnover=0.0;
            }

            turnoverlist.add(turnover);

        }
        //将日期列表和营业额列表转换为字符串，按前端要求用，分割
        String string = StringUtils.join(dateList, ",");//日期列表
        String string1 = StringUtils.join(turnoverlist, ",");//营业额列表

        return TurnoverReportVO.builder()
                .dateList(string)
                .turnoverList(string1)
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = dateList(begin, end);//日期处理方法

        //每天新增的用户数量
        //select count(*) from user where create_time>beginDateTime and create_time<endDateTime;
        ArrayList<Integer> newUserList = new ArrayList<>();
        //每天用户总量
        //select count(*) from user where create_time<endDateTime;
        ArrayList<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //开始时间为当天的0点，结束时间为当天的23点59分59秒
            LocalDateTime beginDateTime = LocalDateTime.of(date, LocalDateTime.MIN.toLocalTime());
            LocalDateTime endDateTime = LocalDateTime.of(date, LocalDateTime.MAX.toLocalTime());

            //设置查询条件
            Map map = new HashMap<>();
            map.put("end",end);

            //查询每天用户总量
            Integer totalUser = userMapper.countbyMap(map);

            //查询每天新增的用户数量
            map.put("begin",begin);
            Integer newUser = userMapper.countbyMap(map);
            if (newUser==null){
                newUser=0;
            }

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }


        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }
}
