package com.sky.service.impl;


import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.IAddressBookService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 地址簿 服务实现类
 * </p>
 *
 * @author author
 * @since 2023-12-07
 */
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements IAddressBookService {


    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    @Transactional
    public void setisdDefault(AddressBook addressBook) {
        //先把所有的地址簿都设置为非默认
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updatebydefault(addressBook);
        //条件是当前用户的id

        //再把当前的地址簿设置为默认
        AddressBook book = addressBookMapper.selectById(addressBook.getId());
        book.setIsDefault(1);
        addressBookMapper.updateById(book);
        //条件是主键id

    }
}
