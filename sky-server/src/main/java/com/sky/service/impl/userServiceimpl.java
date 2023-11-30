package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.JwtProperties;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * @user tyb童以滨
 * @email Tong-yinbin@outlook.com
 * @date2023/11/30
 * @time13:40
 **/

@Service
@Slf4j
public class userServiceimpl extends ServiceImpl<UserMapper,User> implements UserService {

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;


    //微信官方的小程序登录接口
    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";


    @Override
    public User wxlogin(UserLoginDTO userLoginDTO) {
        //调用微信的接口，获取当前登录用户的openid
        HttpClientUtil httpClientUtil = new HttpClientUtil();
        //HttpClient可以实现对外部yrl的访问

        HashMap<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");

        //调用HttpClientUtil的doGet方法，传入url和map，返回json字符串
        String json = httpClientUtil.doGet(WX_LOGIN_URL, map);

        //解析json字符串，获取openid
        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");

        //openid是否为空，如果为空，说明登录失败
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //判断是否为新用户（openid再表里有就不是）
//        userServiceimpl serviceimpl = new userServiceimpl();
//        QueryWrapper<User> wrapper = new QueryWrapper<User>()
//                .eq("openid", openid);
//        User user = serviceimpl.getOne(wrapper);这里为空直接报空指针了改用手写

        User user = userMapper.getone(openid);

        //如果是新用户，保存用户信息 完成自动注册
        if (user == null) {
            user =User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
            //System.out.println(user.getId());

        }//这里其实需要新用户的主键id 不知道mp自动生成的能不能获取到 先试试 试过了 不行 改手写
        //从impl换成mapper就可以了 不清楚为什么
        return user;
    }
}
