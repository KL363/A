package org.example.agentback.controller;

import jakarta.annotation.Resource;
import org.example.agentback.pojo.MyResult;
import org.example.agentback.pojo.MyUser;
import org.example.agentback.service.RegisterService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
public class RegisterController {

    @Resource
    private RegisterService registerService;

    @PostMapping("/register")
    public MyResult register(@RequestBody MyUser user) {
        if(registerService.SelectUserByEmail(user.getUserEmail(),user.getUserName()) != null)
            return MyResult.error("该邮箱或用户名已被注册");
        registerService.InsertUser(user);
        return MyResult.success();
    }

    @PostMapping("/SendOTP")
    public MyResult SendOTP(@RequestBody MyUser user) throws InterruptedException {
        String s = registerService.SendOTP(user.getUserEmail());
        if(s == null) return MyResult.error("发送失败");
        return MyResult.success(s);
    }
}
