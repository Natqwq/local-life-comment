package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisConstant;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class HmDianPingApplicationTests {

    @Autowired
    private CacheClient cacheClient;
    @Autowired
    private ShopServiceImpl shopService;
    @Autowired
    private RedisIdWorker redisIdWorker;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private ExecutorService executorService = Executors.newFixedThreadPool(500);

    @Test
    void testSave2Redis() {
        Shop shop = shopService.getById(1L);
        cacheClient.setWithLogicalExpire(RedisConstant.CACHE_SHOP_KEY + 1L,  shop,
                RedisConstant.CACHE_SHOP_TTL, TimeUnit.MINUTES);
    }
    @Test
    void testIdWorker() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(300);
        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            countDownLatch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            executorService.submit(task);
        }
        long end = System.currentTimeMillis();
        countDownLatch.await();
        System.out.println("time = " + (end - begin));
    }

    @Test
    void SaveSeckillRedis(){
        stringRedisTemplate.opsForValue().set("seckill:stock:12", "100");
    }

}
