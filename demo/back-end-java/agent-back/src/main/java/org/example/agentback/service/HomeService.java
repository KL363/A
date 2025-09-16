package org.example.agentback.service;

import org.example.agentback.pojo.HistoryTable;
import org.example.agentback.pojo.UserHistory;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public interface HomeService {
    public void history(UserHistory userHistory) throws InterruptedException;
    public Integer NewSession(Integer userId) throws InterruptedException;
    public CopyOnWriteArrayList<HistoryTable> GetHistory(Integer userId);
    public void DeleteSession(Integer userId, Integer historyId) throws InterruptedException;
    public ArrayList<UserHistory> GetUserHistory(Integer userId, Integer historyId) throws InterruptedException;
    public Flux<String> streamChat(String message, String userId, String historyId, String uuid) throws InterruptedException;
}
