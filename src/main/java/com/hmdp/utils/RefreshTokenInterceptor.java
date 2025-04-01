package com.hmdp.utils;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Slf4j
public class RefreshTokenInterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringRedisTemplate;
    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.获取请求头中的token
        String token = request.getHeader("authorization");
        log.info("auth token:{}",token);
        // 2.判断是否存在
        if (StrUtil.isBlank(token)) {
            return true;
        }
        //基于token获取Redis中的用户
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(RedisConstant.LOGIN_USER_KEY + token);
        //
        //判断用户是否存在
        if (userMap.isEmpty()) {
            //不存在，直接放行
            return true;
        }
        //将用户信息转为User对象
        UserDTO user = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        //将用户信息保存到ThreadLocal中
        UserHolder.saveUser(user);
        //刷新token有效期
        stringRedisTemplate.expire(RedisConstant.LOGIN_USER_KEY + token, RedisConstant.LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 4.判断用户是否存在
        return true;
    }
}
