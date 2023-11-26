package com.sky.controller.admin;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.impl.setmealServiceimpl;

import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.sky.service.impl.dishServiceimpl;
import java.util.List;

/**
 * <p>
 * 套餐 前端控制器
 * </p>
 *
 * @author author
 * @since 2023-11-26
 */
@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private setmealServiceimpl setmealService;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private dishServiceimpl dishServiceimpl;


    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO dto) {
        log.info("套餐分页查询{}", dto);

        Page<SetmealVO> page = Page.of(dto.getPage(), dto.getPageSize());

        page.addOrder(new OrderItem("create_time", false));

        QueryWrapper<SetmealVO> wrapper = new QueryWrapper<>();
        wrapper.like(dto.getName() != null, "name", dto.getName())
                .eq(dto.getCategoryId() != null, "category_id", dto.getCategoryId())
                .eq(dto.getStatus() != null, "status", dto.getStatus());

        PageResult pageResult = setmealService.pageQuery(page,dto,wrapper);

        return Result.success(pageResult);
    }

    @PostMapping
    @ApiOperation("新增套餐")
    public Result add(@RequestBody SetmealDTO dto) {
        log.info("新增套餐{}", dto);

        setmealService.add(dto);

        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> findById(@PathVariable Long id) {
        log.info("根据id查询套餐{}", id);

        Setmeal byId = setmealService.getById(id);//查询套餐表

        List<SetmealDish> setmealDishes =
                setmealDishMapper.selectList(new QueryWrapper<SetmealDish>()
                .eq("setmeal_id", id));//查询套餐与菜品关联表

        SetmealVO SetmealVO = new SetmealVO();
        BeanUtils.copyProperties(byId, SetmealVO);//把套餐表的数据复制到SetmealVO
       SetmealVO.setSetmealDishes(setmealDishes);//把套餐与菜品关联表的数据复制到SetmealVO


        return Result.success(SetmealVO);
    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result update(@RequestBody SetmealDTO dto) {
        log.info("修改套餐{}", dto);

        if (setmealService.getById(dto.getId()).getStatus()== StatusConstant.DISABLE){
            return Result.error("套餐起售中，不能修改");
        }

        setmealService.updatesetmeal(dto);


        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @Transactional
    public Result deleteBatch(@RequestParam List<Long> ids) {

        log.info("批量删除套餐{}", ids);

        //起售中的套餐不能删除
        for (Long id : ids) {
            if (setmealService.getById(id).getStatus()== StatusConstant.ENABLE){
                return Result.error("套餐起售中，不能删除");
            }
        }
        setmealDishMapper.delete(new QueryWrapper<SetmealDish>()
                .in("setmeal_id",ids));//删除套餐与菜品关联表数据

        setmealService.removeByIds(ids);//删除套餐表数据

        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("修改停售起售状态")
    public Result updateStatus(@PathVariable Integer status,Long id) {
        log.info("修改停售起售状态{},{}", status,id);
          if (status== StatusConstant.ENABLE){

              List<SetmealDish> dishIdList = setmealDishMapper.selectList(
                      new QueryWrapper<SetmealDish>().eq("setmeal_id", id));
              //当前套餐下的菜品id集合


              QueryWrapper<Dish> wrapper = new QueryWrapper<Dish>()
                      .in("id", dishIdList)
                      .eq("status", StatusConstant.DISABLE);
              List<Dish> dishList = dishServiceimpl.list(wrapper);
              //查询当前套餐下的菜品状态为停售的菜品

              //当前套餐下的菜品都是起售状态才能起售
               if (!dishList.isEmpty())return Result.error("当前套餐下的菜品都是起售状态才能起售");

          }

        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();

        setmealService.updateById(setmeal);

        return Result.success();
    }
}
