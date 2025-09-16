package org.example.agentback.controller;

import jakarta.annotation.Resource;
import org.example.agentback.pojo.*;
import org.example.agentback.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@CrossOrigin
@RestController
@RequestMapping("/ai")
public class HomeController {

    @Resource
    private HomeService homeService;

    @GetMapping(value = "/stream-chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(
            @RequestParam String message,
            @RequestParam String userId,
            @RequestParam String uuid,
            @RequestParam String historyId) throws InterruptedException {
        return homeService.streamChat(message,userId,historyId,uuid);
    }

    @PostMapping("/history")
    public MyResult history(@RequestBody UserHistory userHistory) throws InterruptedException {
        homeService.history(userHistory);
        return MyResult.success();
    }

    @PostMapping("/NewSession")
    public MyResult NewSession(@RequestBody UserHistory userHistory) throws InterruptedException {
        return MyResult.success(homeService.NewSession(userHistory.getUserId()));
    }

    @PostMapping("/GetHistory")
    public MyResult GetHistory(@RequestBody MyUser user) throws InterruptedException {
        CopyOnWriteArrayList<HistoryTable> historyTables = homeService.GetHistory(user.getId());
        return MyResult.success(historyTables);
    }

    @PostMapping("/DeleteHistory")
    public MyResult DeleteHistory(@RequestBody UserHistory userHistory) throws InterruptedException {
        homeService.DeleteSession(userHistory.getUserId(), userHistory.getHistoryId());
        return MyResult.success();
    }

    @PostMapping("/HistoryLoad")
    public MyResult HistoryLoad(@RequestBody UserHistory userHistory) throws InterruptedException {
        ArrayList<UserHistory> userHistories = homeService.GetUserHistory(userHistory.getUserId(), userHistory.getHistoryId());
        return MyResult.success(userHistories);
    }
}
