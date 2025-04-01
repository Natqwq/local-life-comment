-- 比较当前线程标识和传入的标识是否一致，一致则删除锁
if(redis.call('get', KEYS[1]) == ARGV[1]) then
    return redis.call('del', KEYS[1])
end
return 0