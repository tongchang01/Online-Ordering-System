package com.sky.controller.user;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.impl.AddressBookServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 地址簿 前端控制器
 * </p>
 *
 * @author author
 * @since 2023-12-07
 */
@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "地址簿")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookServiceImpl addressBookService;


    @GetMapping("/list")
    @ApiOperation(value = "查询地址簿列表")
    public Result<List<AddressBook>> list() {

        Long id = BaseContext.getCurrentId();//获取当前用户id
        log.info("查询地址簿列表 id={}", id);

        QueryWrapper<AddressBook> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", id);

        List<AddressBook> list = addressBookService.list(wrapper);


        return Result.success(list);
    }

    @PostMapping
    @ApiOperation(value = "新增地址簿")
    public Result add(@RequestBody AddressBook addressBook) {
        log.info("新增地址簿 addressBook={}", addressBook);
        Long id = BaseContext.getCurrentId();//获取当前用户id

        if (addressBook.getDetail() == null) {
            return Result.error("详细地址不能为空");
        }
        if (addressBook.getSex() == null) {
            return Result.error("性别不能为空");
        }
        if (addressBook.getPhone() == null) {
            return Result.error("手机号不能为空");
        }

        addressBook.setUserId(id);
        addressBook.setIsDefault(0);//新增的地址簿默认不是默认地址
        addressBookService.save(addressBook);

        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id查询地址簿详情")
    public Result<AddressBook> detail(@PathVariable Long id) {
        log.info("根据id查询地址簿详情 id={}", id);
        //一般用于修改时查询详情
        AddressBook addressBook = addressBookService.getById(id);

        return Result.success(addressBook);
    }

    @GetMapping("/default")
    @ApiOperation(value = "查询默认地址簿")
    public Result<AddressBook> defaultAddressBook() {
        log.info("查询默认地址簿");
        Long id = BaseContext.getCurrentId();//获取当前用户id

        QueryWrapper<AddressBook> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", id);
        wrapper.eq("is_default", 1);

        AddressBook addressBook = addressBookService.getOne(wrapper);

        if (addressBook == null) {
            return Result.error("没查到默认地址");
        }

        return Result.success(addressBook);
    }


    @PutMapping
    @ApiOperation(value = "修改地址簿")
    public Result update(@RequestBody AddressBook addressBook) {
        log.info("修改地址簿 addressBook={}", addressBook);
        Long id = BaseContext.getCurrentId();//获取当前用户id

        if (addressBook.getDetail() == null) {
            return Result.error("详细地址不能为空");
        }
        if (addressBook.getSex() == null) {
            return Result.error("性别不能为空");
        }
        if (addressBook.getPhone() == null) {
            return Result.error("手机号不能为空");
        }

        addressBook.setUserId(id);
        addressBookService.updateById(addressBook);

        return Result.success();
    }

    @DeleteMapping
    @ApiOperation(value = "删除地址簿")
    public Result delete(@RequestParam Long id) {
        log.info("删除地址簿 id={}", id);

        addressBookService.removeById(id);

        return Result.success();
    }


    @PutMapping("/default")
    @ApiOperation(value = "设置默认地址簿")
    public Result defaultAddressBook(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址簿 addressBook={}", addressBook);


        addressBookService.setisdDefault(addressBook);


        return Result.success();
    }

}
