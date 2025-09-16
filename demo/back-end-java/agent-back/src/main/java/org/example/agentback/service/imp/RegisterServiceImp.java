package org.example.agentback.service.imp;

import jakarta.annotation.Resource;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.example.agentback.mapper.RegisterMapper;
import org.example.agentback.pojo.MyUser;
import org.example.agentback.service.BloomFilterService;
import org.example.agentback.service.RegisterService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;

@Service
public class RegisterServiceImp implements RegisterService {

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private RedissonClient redisson;
    
    @Resource
    private RegisterMapper registerMapper;

    @Resource
    private BloomFilterService bloomFilterService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void InsertUser(MyUser user) {
        registerMapper.InsertUser(user);
        Integer i = registerMapper.SelectUserByEmail(user.getUserEmail(), user.getUserName());
        bloomFilterService.addUserId(Integer.toString(i));
    }

    @Override
    public String SendOTP(String userEmail) throws InterruptedException {
        RLock lock = redisson.getLock(userEmail);
        if(lock.tryLock(5,30, TimeUnit.SECONDS)) {
            try {
                String str = "0123456789";
                StringBuilder code = new StringBuilder();
                for (int i = 0; i < 6; i++)
                    code.append(str.charAt((int) (Math.random() * str.length())));
                mailSender.send(getMimeMessagePreparator(userEmail, code));
                return code.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }
        return null;
    }

    private static MimeMessagePreparator getMimeMessagePreparator(String userEmail, StringBuilder code) {
        String otpCode = code.toString();
        return new MimeMessagePreparator() {
            @Override
            public void prepare(MimeMessage mimeMessage) throws Exception {
                mimeMessage.setRecipient(Message.RecipientType.TO,
                        new InternetAddress(userEmail));
                mimeMessage.setFrom(new InternetAddress("3887768494@qq.com"));
                mimeMessage.setText("您的验证码是" + otpCode);
            }
        };
    }

    @Override
    public Integer SelectUserByEmail(String userEmail, String userName) {
        return registerMapper.SelectUserByEmail(userEmail, userName);
    }

}
