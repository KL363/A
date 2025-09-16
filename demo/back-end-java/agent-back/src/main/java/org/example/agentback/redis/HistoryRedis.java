package org.example.agentback.redis;

import org.example.agentback.pojo.UserHistory;

public interface HistoryRedis {
    public void history(UserHistory userHistory) throws InterruptedException;
    public Integer NewSession(Integer userId) throws InterruptedException;
}
