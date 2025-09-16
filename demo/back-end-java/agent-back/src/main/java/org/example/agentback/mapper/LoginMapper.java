package org.example.agentback.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.agentback.pojo.MyUser;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

@Mapper
public interface LoginMapper {
    public Integer SelectByNameAndPassword(String userName, String userPassword);
    public Integer SelectByEmail(String userEmail);
    public CopyOnWriteArrayList<Integer> SelectById(Integer userId);
}
