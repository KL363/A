package org.example.agentback.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.agentback.pojo.MyUser;

@Mapper
public interface RegisterMapper {
    public void InsertUser(MyUser user);
    public Integer SelectUserByEmail(String userEmail, String userName);
}
