package org.example.agentback.service.imp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.example.agentback.mapper.HomeMapper;
import org.example.agentback.pojo.HistoryTable;
import org.example.agentback.pojo.QueryBody;
import org.example.agentback.pojo.UserHistory;
import org.example.agentback.redis.HistoryRedis;
import org.example.agentback.service.HomeService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Service
public class HomeServiceImp implements HomeService {

    @Resource
    private HistoryRedis historyRedis;

    @Resource
    private HomeMapper homeMapper;

    @Resource
    private RedissonClient redisson;

    @Resource
    private RedisTemplate<Integer,String> redisTemplate;

    @Override
    public void history(UserHistory userHistory) throws InterruptedException {
        historyRedis.history(userHistory);
    }

    @Override
    public Integer NewSession(Integer userId) throws InterruptedException {
        return historyRedis.NewSession(userId);
    }

    @Override
    public synchronized CopyOnWriteArrayList<HistoryTable> GetHistory(Integer userId) {
        CopyOnWriteArrayList<UserHistory> userHistories = homeMapper.SelectByUserId(userId);
        CopyOnWriteArrayList<HistoryTable> historyTables = new CopyOnWriteArrayList<>();
        if(!userHistories.isEmpty()) {
            int historyId = userHistories.getFirst().getHistoryId();
            for(UserHistory userHistory : userHistories) {
                if(userHistory.getHistoryId() == historyId) {
                    String content = userHistory.getContent();
                    String substring;
                    if(content.length() > 8) substring = content.substring(0, 6) + "...";
                    else substring = content;
                    HistoryTable historyTable = new HistoryTable(historyId,substring,userHistory.getAskTime());
                    historyTables.add(historyTable);
                    historyId--;
                }
            }
            return historyTables;
        }
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void DeleteSession(Integer userId, Integer historyId) throws InterruptedException {
        homeMapper.DeleteById(userId, historyId);
    }

    @Override
    public ArrayList<UserHistory> GetUserHistory(Integer userId, Integer historyId) throws InterruptedException {
        return homeMapper.selectByUserIdAndHistoryId(userId, historyId);
    }

    @Override
    public Flux<String> streamChat(String message, String userId, String historyId, String uuid) throws InterruptedException {
        if(!Objects
                .equals(Objects.requireNonNull(redisTemplate.
                        opsForValue().
                        get(Integer.parseInt(userId)))
                        .split(":")[0], uuid)) return null;

        RLock lock = redisson.getLock(uuid);
        lock.tryLock(5, 30, TimeUnit.SECONDS);
        if(lock.isLocked()) {
            try {
                // 3. 配置WebClient（使用POST方法）
                WebClient client = WebClient.create("http://localhost:8000");

                String requestBody = String.format("{\"query\":\"%s\",\"stream\":%b,\"session_id\":%d}", message, true, Integer.parseInt(historyId));

                return client.post()
                        .uri("/query_with_intent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToFlux(String.class)
                        .onErrorResume(e -> Flux.just("Error: " + e.getMessage()));
            } catch (NumberFormatException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
        return Flux.just("Error: " + message);
    }

}
