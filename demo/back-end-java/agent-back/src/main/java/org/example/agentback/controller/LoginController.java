package org.example.agentback.controller;

import jakarta.annotation.Resource;
import org.example.agentback.pojo.IdWithUUID;
import org.example.agentback.pojo.MyResult;
import org.example.agentback.pojo.MyUser;
import org.example.agentback.service.LoginService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class LoginController {

    @Resource
    private LoginService loginService;

    @PostMapping("/login")
    public MyResult login(@RequestBody MyUser user) throws InterruptedException {
        IdWithUUID idWithUUID = loginService.SelectByNameAndPassword(user.getUserName(), user.getUserPassword());
        if (idWithUUID != null) return MyResult.success(idWithUUID);
        else return MyResult.error("该用户不存在或者密码错误");
    }

    @PostMapping("LoginByEmail")
    public MyResult loginByEmail(@RequestBody MyUser user) throws InterruptedException {
        IdWithUUID idWithUUID = loginService.LoginByEmail(user.getUserEmail());
        if (idWithUUID != null) return MyResult.success(idWithUUID);
        return MyResult.error("该邮箱尚未注册");
    }

}
