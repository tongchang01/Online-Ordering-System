package com.sky.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.entity.AddressBook;

/**
 * <p>
 * 地址簿 服务类
 * </p>
 *
 * @author author
 * @since 2023-12-07
 */
public interface IAddressBookService extends IService<AddressBook> {


    void setisdDefault(AddressBook addressBook);
}
