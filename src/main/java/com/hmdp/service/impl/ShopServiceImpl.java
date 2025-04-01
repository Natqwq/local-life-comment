package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstant;
import com.hmdp.utils.RedisData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private CacheClient cacheClient;
    /**
     * 根据id查询店铺信息
     * @param id
     * @return Result
     */
    @Override
    public Result queryById(Long id) {
        //缓存null值解决缓存穿透
        Shop shop = cacheClient.queryWithPassThrough(
                RedisConstant.CACHE_SHOP_KEY,
                id,
                Shop.class,
                this::getById,
                RedisConstant.CACHE_SHOP_TTL,
                TimeUnit.MINUTES);

        //互斥锁解决缓存击穿
        //Shop shop = queryWithMutex(id);

        //设置逻辑过期解决缓存击穿
//        Shop shop = cacheClient.queryWithLogicalExpire(
//                RedisConstant.CACHE_SHOP_KEY,
//                id,
//                Shop.class,
//                this::getById,
//                RedisConstant.CACHE_SHOP_TTL,
//                TimeUnit.MINUTES);
        if(Objects.isNull(shop)){
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);
//    private Shop queryWithLogicalExpire(Long id){
//        //先从redis中查询
//        String shopStr = stringRedisTemplate.opsForValue().get(RedisConstant.CACHE_SHOP_KEY + id);
//        // 判断是否存在
//        if(StrUtil.isBlank(shopStr)){
//            return null;
//        }
//
//        RedisData redisData = JSONUtil.toBean(shopStr, RedisData.class);
//        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
//        if(redisData.getExpireTime().isAfter(LocalDateTime.now())){
//            //未过期，直接返回店铺信息
//            return shop;
//        }
//        //已过期，需要重建缓存
//        String lockKey = RedisConstant.LOCK_SHOP_KEY + id;
//        // 尝试获取锁，未获取到锁，则直接返回原始店铺信息
//        if(tryLock(lockKey)){
//            shopStr = stringRedisTemplate.opsForValue().get(RedisConstant.CACHE_SHOP_KEY + id);
//            redisData = JSONUtil.toBean(shopStr, RedisData.class);
//            shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
//            if(redisData.getExpireTime().isAfter(LocalDateTime.now())){
//                //未过期，直接返回店铺信息
//                return shop;
//            }
//            // 获取锁成功，开启独立线程，实现缓存重建
//            CACHE_REBUILD_EXECUTOR.submit(() -> {
//                try {
//                    this.saveShop2Redis(id, RedisConstant.CACHE_SHOP_TTL * 2);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                } finally {
//                    unLock(lockKey);
//                }
//            });
//            return shop;
//        }
//        return shop;
//    }
//    private Shop queryWithMutex(Long id){
//        //先从redis中查询
//        String shopStr = stringRedisTemplate.opsForValue().get(RedisConstant.CACHE_SHOP_KEY + id);
//        // 判断是否存在
//        if(StrUtil.isNotBlank(shopStr)){
//            Shop shop = JSONUtil.toBean(shopStr, Shop.class);
//            return shop;
//        }
//        //判断命中的是否是空值
//        if(shopStr != null){
//            return null;
//        }
//        //redis中不存在，尝试获取锁去重建redis
//        String lockKey = RedisConstant.LOCK_SHOP_KEY + id;
//        Shop shop = null;
//        try {
//            while(!tryLock(lockKey)){
//                Thread.sleep(50);
//                //先从redis中查询
//                shopStr = stringRedisTemplate.opsForValue().get(RedisConstant.CACHE_SHOP_KEY + id);
//                // 判断是否存在
//                if(StrUtil.isNotBlank(shopStr)){
//                    shop = JSONUtil.toBean(shopStr, Shop.class);
//                    return shop;
//                }
//                //判断命中的是否是空值
//                if(shopStr != null){
//                    return null;
//                }
//            }
//            //redis中不存在，则从数据库中查店铺数据，并将数据存入redis
//            shop = getById(id);
//            if(Objects.isNull(shop)){
//                stringRedisTemplate.opsForValue().set(RedisConstant.CACHE_SHOP_KEY + id,
//                        "", RedisConstant.CACHE_NULL_TTL, TimeUnit.MINUTES);
//                return null;
//            }
//            stringRedisTemplate.opsForValue().set(RedisConstant.CACHE_SHOP_KEY + id,
//                    JSONUtil.toJsonStr(shop), RedisConstant.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            //释放锁
//            unLock(lockKey);
//        }
//        return shop;
//    }

//    private Shop queryWithPassThrough(Long id){
//        //先从redis中查询
//        String shopStr = stringRedisTemplate.opsForValue().get(RedisConstant.CACHE_SHOP_KEY + id);
//        // 判断是否存在
//        if(StrUtil.isNotBlank(shopStr)){
//            Shop shop = JSONUtil.toBean(shopStr, Shop.class);
//            return shop;
//        }
//        //判断命中的是否是空值
//        if(shopStr != null){
//            return null;
//        }
//        //redis中不存在，则从数据库中查店铺数据，并将数据存入redis
//        Shop shop = getById(id);
//        if(Objects.isNull(shop)){
//            stringRedisTemplate.opsForValue().set(RedisConstant.CACHE_SHOP_KEY + id,
//                    "", RedisConstant.CACHE_NULL_TTL, TimeUnit.MINUTES);
//            return null;
//        }
//        stringRedisTemplate.opsForValue().set(RedisConstant.CACHE_SHOP_KEY + id,
//                JSONUtil.toJsonStr(shop), RedisConstant.CACHE_SHOP_TTL, TimeUnit.MINUTES);
//        return shop;
//    }
//    public void saveShop2Redis(Long id, Long expireSeconds){
//        Shop shop = getById(id);
//
//        RedisData redisData = new RedisData();
//        redisData.setData(shop);
//        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
//
//        stringRedisTemplate.opsForValue().set(RedisConstant.CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
//    }
//
//    private boolean tryLock(String key){
//        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
//        return BooleanUtil.isTrue(flag);
//    }
//
//    private void unLock(String key){
//        stringRedisTemplate.delete(key);
//    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if(Objects.isNull(id)){
            return Result.fail("店铺id不能为空");
        }
        updateById(shop);
        stringRedisTemplate.delete(RedisConstant.CACHE_SHOP_KEY + id);
        return Result.ok();
    }
}
