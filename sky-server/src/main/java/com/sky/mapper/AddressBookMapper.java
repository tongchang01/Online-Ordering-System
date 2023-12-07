package com.sky.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 地址簿 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2023-12-07
 */
@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {

    @Update("update address_book set is_default=#{isDefault} where user_id=#{userId}")
    void updatebydefault(AddressBook addressBook);
    //is_default=#{addressBook.isDefault} where user_id=#{addressBook.userId}
    //这个写法不对要直接写属性名
    //在MyBatis中，你应该直接使用对象的属性名，而不是通过对象来引用

    //要用对象引用这种写法要在形参前加上@Param("addressBook")这样
    //当出现多个参数时也要用@Param("addressBook") 标识
}
