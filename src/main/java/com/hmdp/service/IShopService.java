package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;


public interface IShopService extends IService<Shop> {
    /**
     * 根据id查询店铺
     * @param id 店铺id
     * @return 店铺信息
     */
    Result queryById(Long id);
    /**
     * 更新店铺信息
     * @param shop 店铺信息
     * @return 是否成功
     */
    Result update(Shop shop);
}
