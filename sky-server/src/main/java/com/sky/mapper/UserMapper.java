package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("select * from user where openid=#{openid}")
    User getone(String openid);

    //void add(User user);

    Integer countbyMap(Map map);
}
