package com.sky.controller.admin;

import com.fasterxml.jackson.core.TSFBuilder;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.service.impl.EmployeeServiceImpl;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;


    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("登出")
    public Result<String> logout() {
        return Result.success();
    }


    @ApiOperation(value = "新增员工")
    @PostMapping
    public Result save(@RequestBody EmployeeLoginDTO employeeLoginDTO){

        log.info("新增员工:{}",employeeLoginDTO);
        //dto里的数据还不够 还得从实体类里加 比如密码
        Employee employee = new Employee();
        //对象拷贝
        BeanUtils.copyProperties(employeeLoginDTO,employee);
        //添加前端没有提供的数据
        //添加账号状态 为启用
        employee.setStatus(StatusConstant.ENABLE);
        //添加默认密码 加密
        employee.setPassword(
                DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //添加创建时间和修改时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //添加当前记录创建人 的信息 （当前登录用户）
        // to do 等待完善 需获取当前登录id 用jwt已经封装id
        //通过线程隔离的特性获取jwt中的id
        //因为每一次请求都是一个新的线程，这次请求里的每个操作其实都会被拦截器拦截来校验令牌
        //于是在拦截器中提前把id封装到线程空间中 实现在当前类中获取到
        //用完ThreadLocal后记得要删除线程释放资源 不然会一直留在内存里
        //方法封装完了 在sky-common/src/main/java/com/sky/context/BaseContext.java
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setCreateUser(BaseContext.getCurrentId());
        //释放线程
        BaseContext.removeCurrentId();

        //数据无误写入
        employeeService.save(employee);

        return Result.success();
    }

}
