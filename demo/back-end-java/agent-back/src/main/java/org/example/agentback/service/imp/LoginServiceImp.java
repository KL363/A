package org.example.agentback.service.imp;

import jakarta.annotation.Resource;
import org.example.agentback.mapper.LoginMapper;
import org.example.agentback.pojo.IdWithUUID;
import org.example.agentback.service.LoginService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImp implements LoginService {

    @Resource
    private LoginMapper loginMapper;

    @Resource
    private RedissonClient redisson;

    @Resource
    private RedisTemplate<Integer,String> redisTemplate;

    @Resource
    private JavaMailSender mailSender;

    @Override
    public IdWithUUID SelectByNameAndPassword(String userName, String userPassword) throws InterruptedException {
        RLock lock = redisson.getLock(userName);
        if(lock.tryLock(5,30, TimeUnit.SECONDS)) {
            try {
                Integer i = loginMapper.SelectByNameAndPassword(userName, userPassword);
                IdWithUUID uuid = getIdWithUUID(i);
                if (uuid != null) return uuid;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
        return null;
    }

    @Override
    public IdWithUUID LoginByEmail(String userEmail) throws InterruptedException {
        RLock lock = redisson.getLock(userEmail);
        if(lock.tryLock(5,30, TimeUnit.SECONDS)) {
            try {
                Integer i = loginMapper.SelectByEmail(userEmail);
                IdWithUUID uuid = getIdWithUUID(i);
                if (uuid != null) return uuid;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
        return null;
    }

    private IdWithUUID getIdWithUUID(Integer i) {
        if (i != null) {
            CopyOnWriteArrayList<Integer> integers = loginMapper.SelectById(i);
            String uuid = UUID.randomUUID().toString();
            if (!integers.isEmpty())
                redisTemplate.opsForValue().set(i, uuid + ":" + integers.getFirst().toString(), 10, TimeUnit.DAYS);
            else
                redisTemplate.opsForValue().set(i, uuid + ":0", 10, TimeUnit.DAYS);
            return new IdWithUUID(i, uuid);
        }
        return null;
    }

}
