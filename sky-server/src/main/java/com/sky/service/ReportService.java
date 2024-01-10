package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

/**
 * @user TYB童以滨
 * @email Tong-yinbin@outlook.com
 * @date2024/1/9
 * @time13:55
 **/
public interface ReportService {


    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);

    UserReportVO userStatistics(LocalDate begin, LocalDate end);


}
