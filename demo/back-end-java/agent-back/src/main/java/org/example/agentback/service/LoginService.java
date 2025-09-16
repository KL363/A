package org.example.agentback.service;

import org.example.agentback.pojo.IdWithUUID;
import org.example.agentback.pojo.MyUser;

import java.util.concurrent.ConcurrentHashMap;

public interface LoginService {
    public IdWithUUID SelectByNameAndPassword(String userName, String userPassword) throws InterruptedException;
    public IdWithUUID LoginByEmail(String userEmail) throws InterruptedException;
}
