package org.example.agentback;

import jakarta.annotation.Resource;
import org.example.agentback.mapper.LoginMapper;
import org.example.agentback.pojo.MyUser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class AgentBackApplicationTests {

    @Resource
    private LoginMapper loginMapper;

    @Resource
    private RedisTemplate<Integer,String> redisTemplate;

    @Test
    void contextLoads() {
    }

}
