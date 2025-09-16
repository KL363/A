package org.example.agentback.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.agentback.pojo.UserHistory;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

@Mapper
public interface HomeMapper {
    public void InsertHistory(UserHistory userHistory);
    public CopyOnWriteArrayList<Integer> SelectById(Integer userId);
    public CopyOnWriteArrayList<UserHistory> SelectByUserId(Integer userId);
    public void DeleteById(Integer userId, Integer historyId);
    public ArrayList<UserHistory> selectByUserIdAndHistoryId(Integer userId, Integer historyId);
}
