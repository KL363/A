package org.example.agentback.interceptor;


import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.agentback.pojo.MyResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private RedisTemplate<Integer,String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uuid = request.getHeader("uuid");
        String IdString = request.getHeader("id");
        if (IdString != null) {
            Integer id = Integer.parseInt(IdString);
            String s = redisTemplate.opsForValue().get(id);
            if (s != null && uuid != null && redisTemplate.opsForValue().get(id) != null
                    && uuid.equals(s.split(":")[0])) return true;
        }
        response.getWriter().write("Not Login");
        return false;
    }
}
