package org.example.agentback.redis.Imp;

import jakarta.annotation.Resource;
import org.example.agentback.mapper.HomeMapper;
import org.example.agentback.pojo.UserHistory;
import org.example.agentback.redis.HistoryRedis;
import org.example.agentback.service.BloomFilterService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class HistoryRedisImp implements HistoryRedis {

    @Resource
    private RedisTemplate<Integer, String> redisTemplate;

    @Resource
    private RedissonClient redisson;

    @Resource
    private HomeMapper homeMapper;

    @Resource
    private BloomFilterService bloomFilterService;

    @Override
    public void history(UserHistory userHistory) throws InterruptedException {
        RLock lock = redisson.getLock("history");
        boolean b = lock.tryLock(5, 30, TimeUnit.SECONDS);
        if(b) {
            try {
                if (!bloomFilterService.mightContain(Integer.toString(userHistory.getUserId()))) return;
                String s = redisTemplate.opsForValue().get(userHistory.getUserId());
                String s2 = s.split(":")[1];
                int i = Integer.parseInt(s2);
                userHistory.setHistoryId(i);
                homeMapper.InsertHistory(userHistory);
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public Integer NewSession(Integer userId) throws InterruptedException {
        RLock lock = redisson.getLock("NewSession");
        boolean b = lock.tryLock(5, 30, TimeUnit.SECONDS);
        if(b) {
            try {
                if (!bloomFilterService.mightContain(Integer.toString(userId))) return null;
                CopyOnWriteArrayList<Integer> integers = homeMapper.SelectById(userId);
                int i = 0;
                String s = redisTemplate.opsForValue().get(userId);
                String s2 = s.split(":")[0];
                if(!integers.isEmpty()) {
                    i = integers.get(0);
                    i++;
                    redisTemplate.opsForValue().set(userId, s2 + ":" + Integer.toString(i), 10, TimeUnit.DAYS);
                } else redisTemplate.opsForValue().set(userId, s2 + ":0", 10, TimeUnit.DAYS);
                return i;
            } finally {
                lock.unlock();
            }
        }
        return null;
    }
}
