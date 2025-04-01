package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询所有店铺类型
     * @return Result
     */
    @Override
    public Result queryTypeList() {
        // 先从redis中查询
        String listJson = stringRedisTemplate.opsForValue().get(RedisConstant.CACHE_SHOP_TYPE_KEY);
        // 判断是否存在
        if(StrUtil.isNotBlank(listJson)){
            return Result.ok(JSONUtil.toList(listJson, ShopType.class));
        }
        // 不存在，查询数据库
        List<ShopType> list = this.query().orderByAsc("sort").list();
        // 判断数据库是否为空
        if(list == null){
            return Result.fail("店铺类型不存在");
        }
        // 数据库不为空，存入redis
        stringRedisTemplate.opsForValue().set(RedisConstant.CACHE_SHOP_TYPE_KEY, JSONUtil.toJsonStr(list));
        return Result.ok(list);
    }
}
