package asia.chatclient.quiz.handler;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final StringRedisTemplate redisTemplate;

    public CustomAuthenticationFailureHandler(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String ip = getIpAddr(request);
        String key = "login:" + ip;

        // 增加失败次数
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES); // 设置 1 分钟有效期
        }

        if (count != null && count > 5) {
            response.sendRedirect("/login?error=busy");
        } else {
            response.sendRedirect("/login?error=retry");
        }
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
