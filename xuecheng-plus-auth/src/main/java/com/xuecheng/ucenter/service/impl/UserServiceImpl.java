package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现用户认证
 */
@Slf4j
@Component
public class UserServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired//注入Spring容器
    ApplicationContext applicationContext;

    @Autowired
    XcMenuMapper xcMenuMapper;
    /**
     * 实现统一入口功能
     * 传入的请求认证参数就是AuthParamsDto
     *
     * **/
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //首先，将传入的json转成AuthParamsDto对象
        AuthParamsDto authParamsDto = null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证参数不符合要求");
        }
        //拿到认证的类型,有password、wx等等
        String authType = authParamsDto.getAuthType();
        //根据认证的类型，从容器中取出指定的Bean
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        //调用统一的execute方法完成认证
        XcUserExt xcUserExt = authService.execute(authParamsDto);
        //封装xcUserExt用户信息为UserDetails
        /** 最终根据 UserDetails对象生成令牌 **/
        UserDetails userPrincipal = getUserPrincipal(xcUserExt);

        return userPrincipal;
    }

    /**
     * @description 查询用户信息
     * @param xcUser  用户id，主键
     * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
     * @author Mr.M
     * @date 2022/9/29 12:19
     */
    public UserDetails getUserPrincipal(XcUserExt xcUser){
        String password = xcUser.getPassword();
        /** 权限：根据用户的id查询用户的权限 **/
        String[] authorities=  {"test"};
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(xcUser.getId());
        if(xcMenus.size() > 0){
            List<String> permissions = new ArrayList<>();
            xcMenus.forEach(m -> {
                permissions.add(m.getCode());//拿到了用户拥有的权限标识符
            });
            authorities = permissions.toArray(new String[0]);
        }
        xcUser.setPassword(null);
        //将用户信息转json
        String userJson = JSON.toJSONString(xcUser);
        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();
        return  userDetails;
    }
}
