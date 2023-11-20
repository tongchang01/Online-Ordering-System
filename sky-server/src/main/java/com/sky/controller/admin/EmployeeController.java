package com.sky.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.TSFBuilder;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
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
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
    public Result save(@RequestBody EmployeeLoginDTO employeeLoginDTO) {

        log.info("新增员工:{}", employeeLoginDTO);
        //dto里的数据还不够 还得从实体类里加 比如密码
        Employee employee = new Employee();
        //对象拷贝
        BeanUtils.copyProperties(employeeLoginDTO, employee);
        //添加前端没有提供的数据
        //添加账号状态 为启用
        employee.setStatus(StatusConstant.ENABLE);
        //添加默认密码 加密
        employee.setPassword(
                DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //添加创建时间和修改时间
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //添加当前记录创建人 的信息 （当前登录用户）
        // to do 等待完善 需获取当前登录id 用jwt已经封装id
        //通过线程隔离的特性获取jwt中的id
        //因为每一次请求都是一个新的线程，这次请求里的每个操作其实都会被拦截器拦截来校验令牌
        //于是在拦截器中提前把id封装到线程空间中 实现在当前类中获取到
        //用完ThreadLocal后记得要删除线程释放资源 不然会一直留在内存里
        //方法封装完了 在sky-common/src/main/java/com/sky/context/BaseContext.java
        //employee.setCreateUser(BaseContext.getCurrentId());
        //employee.setCreateUser(BaseContext.getCurrentId());
        //释放线程
        //BaseContext.removeCurrentId();

        //数据无误写入
        //employeeService.save(employee);
        //有大问题 mp写入 id自增的默认策略是雪花算法 生成巨长一串id
        //再通过id执行其他sql时 id就会匹配不上
        //那个属性上重写策略为普通自增 @TableId(value = "id", type = IdType.AUTO)
        //可以改成普通自增 但是在这不知道为什么不行
        //发现id过长时在加注释就没用了 要把表删掉重建才生效所以要涨记性 以后用mp先检查一下
        employeeService.add(employee);


        return Result.success();
    }

    /**
     * "员工分页查询"
     *
     * @param empDTO 是封装前端传来的参数
     *               PageResult是封装要传回前端的数据格式
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO empDTO) {
        log.info("员工分页查询 参数：{}", empDTO);
        //empDTO.setPage(1);//默认给个 初始显示第几页
        //empDTO.setPageSize(10);//默认给个 每页显示的数据条数
        //画蛇添足了 前端有默认值

        //创建分页对象
        Page<Employee> page1 = Page.of(empDTO.getPage(), empDTO.getPageSize());

        //设置排序条件
        page1.addOrder(new OrderItem("create_time", false));//按id升序排序

        //添加条件
        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        wrapper.like(empDTO.getName() != null && empDTO.getName() != "",
                "name", empDTO.getName());

        //查询
        Page<Employee> employeePage = employeeService.page(page1, wrapper);

        //解析
        long total = employeePage.getTotal();//条数
        List<Employee> records = employeePage.getRecords();//数据

        //封装新对象用于返回 （要总记录数和emp集合）
        PageResult pageResult = new PageResult(total, records);


        //到这里发现 前端 的操作时间这一栏的数据格式有问题 要改一下 对日期进行格式化
        //方式一 在当前方法下 设置日期格式
        //在emp实体类上给时间相关属性加上@JsonFormat(patten="yyyy-MM-dd HH:mm:ss")
        //方式二 在拦截其中统一进行转换 class WebMvcConfiguration 中添加消息转换器
        return Result.success(pageResult);
    }


    /**
     * 启用禁用员工账号
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工账号")
    public Result StartOrStop(@PathVariable Integer status, Long id) {

        log.info("启用禁用员工账号 {}, {}", status, id);


        //根据id修改状态 反选 状态
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        BaseContext.removeCurrentId();
        //通过builder new了一个新对象   .build();是结束的意思没有就报错还不好改
        employeeService.updateById(employee);

        return Result.success();
        //为了练习 在xml里也编写一个通用的根据id改emp所有字段的代码段


    }


    /**
     * 根据id查询
     * @param id
     * @return
     */
    @ApiOperation("根据id查询 用于修改的回显")
    @GetMapping("/{id}")
    public Result<Employee> selById(@PathVariable long id) {

        Employee employee = employeeService.getById(id);
        employee.setPassword("******");//隐藏密码


        return Result.success(employee);
    }

    /**
     * 修改员工信息
     * @param dto
     * @return
     */

    @ApiOperation("修改员工信息")
    @PutMapping
    public Result updatabyid(@RequestBody EmployeeDTO dto){
         log.info("修改员工信息 {}",dto);

        Employee employee = new Employee();

        BeanUtils.copyProperties(dto,employee);

        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        BaseContext.removeCurrentId();

        employeeService.updateById(employee);


        return Result.success();
    }


}
