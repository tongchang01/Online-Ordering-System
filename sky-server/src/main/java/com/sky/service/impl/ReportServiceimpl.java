package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Struct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * @param begin
     * @param end
     * @return 日期处理方法
     */
    public ArrayList<LocalDate> dateList(LocalDate begin, LocalDate end) {

        //大前提，begin<end
        if (begin.isAfter(end)) {
            throw new RuntimeException("开始时间不能大于结束时间");
        }

        ArrayList<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)) {//计算日期
            begin = begin.plusDays(1);
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
            map.put("status", Orders.COMPLETED);
            map.put("begin", beginDateTime);
            map.put("end", endDateTime);

            //查询营业额
            Double turnover = ordersMapper.sumtodayTurnover(map);//这里查不到会返回null不合理
            if (turnover == null) {
                turnover = 0.0;
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
            map.put("end", end);

            //查询每天用户总量
            Integer totalUser = userMapper.countbyMap(map);

            //查询每天新增的用户数量
            map.put("begin", begin);
            Integer newUser = userMapper.countbyMap(map);
            if (newUser == null) {
                newUser = 0;
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

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateList = dateList(begin, end);//日期处理方法


        List<Integer> ordersCountList = new ArrayList<>();//存放每天的总订单数
        List<Integer> valiordersCountList = new ArrayList<>();//存放每天的有效订单数
        for (LocalDate date : dateList) {
            //查询每天的总订单数
            LocalDateTime begintime = LocalDateTime.of(date, LocalDateTime.MIN.toLocalTime());
            LocalDateTime endtime = LocalDateTime.of(date, LocalDateTime.MAX.toLocalTime());
            Integer ordersCount = getorderscount(begintime, endtime, null);

            //查询每天的有效订单数（状态为已完成的订单）
            Integer valiordersCount = getorderscount(begintime, endtime, Orders.COMPLETED);

            ordersCountList.add(ordersCount);
            valiordersCountList.add(valiordersCount);

        }

        //计算时间区间内的总订单数量
        Integer totalorderscount = ordersCountList.stream().reduce(Integer::sum).get();
        //计算时间区间内的有效订单总数量
        Integer valiorderscount = valiordersCountList.stream().reduce(Integer::sum).get();

        //订单完成率
        Double orderCompletionRate = 0.0;
        if (totalorderscount != 0) {
            orderCompletionRate =
                    valiorderscount.doubleValue() / totalorderscount.doubleValue();
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))//日期列表
                .orderCountList(StringUtils.join(ordersCountList, ","))//总订单数列表
                .validOrderCountList(StringUtils.join(valiordersCountList, ","))//有效订单数列表
                .totalOrderCount(totalorderscount)//总订单数
                .validOrderCount(valiorderscount)//有效订单数
                .orderCompletionRate(orderCompletionRate)//订单完成率
                .build();
    }

    private Integer getorderscount(LocalDateTime begin, LocalDateTime end, Integer status) {

        //设置查询条件
        Map map = new HashMap<>();
        map.put("status", status);
        map.put("begin", begin);
        map.put("end", end);

        return ordersMapper.countbyMap(map);
    }


    @Override
    public SalesTop10ReportVO gettop10(LocalDate begin, LocalDate end) {
        LocalDateTime begintime = LocalDateTime.of(begin, LocalDateTime.MIN.toLocalTime());
        LocalDateTime endtime = LocalDateTime.of(end, LocalDateTime.MAX.toLocalTime());

        //要查两张表，orders和order_detail
        //条件是订单状态为已完成，订单时间在begin和end之间
        List<GoodsSalesDTO> SalesTop = ordersMapper.getGoodsSales(begintime, endtime);

        //更改格式
        List<String> names =
                SalesTop.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String namelist = StringUtils.join(names, ",");

        List<Integer> nums =
                SalesTop.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numlist = StringUtils.join(nums, ",");

        return SalesTop10ReportVO.builder()
                .nameList(namelist)
                .numberList(numlist)
                .build();
    }

    @Override
    public void exportData(HttpServletResponse response) {
        //1.查询数据

        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        //查询概览数据
        BusinessDataVO businessData =
                workspaceService.getBusinessData(
                        LocalDateTime.of(begin, LocalTime.MAX),
                        LocalDateTime.of(end, LocalTime.MAX));
        //2.以poi写入数据到excel

        //反射获取模板文件
        InputStream inputStream = this.getClass().getClassLoader()
                .getResourceAsStream("template/businessData.xlsx");

        try {
            //基于模板创建新的excel文件
            XSSFWorkbook sheets = new XSSFWorkbook(inputStream);

            XSSFSheet sheet1 = sheets.getSheet("Sheet1");//获取标签页

            //填充数据--时间
            XSSFCell cell = sheet1.getRow(1).getCell(1);//获取单元格
            cell.setCellValue("时间："+begin+"至"+end);//设置单元格的值

            //第4行
            XSSFRow row = sheet1.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());//营业额
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());//订单完成率
            row.getCell(6).setCellValue(businessData.getNewUsers());//新增用户数

            //第5行
            row = sheet1.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());//有效订单数
            row.getCell(4).setCellValue(businessData.getUnitPrice());//平均客单价

            //填充明细数据
            for (int i = 0; i <30 ; i++) {
                LocalDate date = begin.plusDays(i);
                //查询每天的营业数据
                BusinessDataVO businessData1 = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));
                //填充数据
                row = sheet1.getRow(7+i);//从第8行开始
                row.getCell(1).setCellValue(date.toString());//日期
                row.getCell(2).setCellValue(businessData1.getTurnover());//营业额
                row.getCell(3).setCellValue(businessData1.getValidOrderCount());//有效订单数
                row.getCell(4).setCellValue(businessData1.getOrderCompletionRate());//订单完成率
                row.getCell(5).setCellValue(businessData1.getUnitPrice());//平均客单价
                row.getCell(6).setCellValue(businessData1.getNewUsers());//新增用户数

            }

            //3.以输出流下载excel
            ServletOutputStream outputStream = response.getOutputStream();
            sheets.write(outputStream);

            //4.关闭流
            outputStream.close();
            sheets.close();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
