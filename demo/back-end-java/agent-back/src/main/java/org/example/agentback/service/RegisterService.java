package org.example.agentback.service;

import org.example.agentback.pojo.MyUser;

public interface RegisterService {
    public void InsertUser(MyUser user);
    public String SendOTP(String userEmail) throws InterruptedException;
    public Integer SelectUserByEmail(String userEmail, String userName);
}
