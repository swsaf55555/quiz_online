package asia.chatclient.quiz.controller;

import asia.chatclient.quiz.entity.User;
import asia.chatclient.quiz.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private  static String passcode;

    public LoginController(UserRepository userRepository, PasswordEncoder passwordEncoder, StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.redisTemplate = redisTemplate;
        passcode="passcode";
        try {
            Path path = Paths.get("passcode.txt");
            passcode=Files.readString(path).trim();
            System.out.println(passcode);
        } catch (IOException e) {
            System.err.println("读取 passcode.txt 失败: " + e.getMessage());
        }
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String part,
                           @RequestParam String phonenumber,
                           @RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String nickname,
                           @RequestParam String passcode,
                           HttpServletRequest request) {
        String ip = getIpAddr(request);
        String key = "register:" + ip;

        // 检查 Redis 是否已有注册记录
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return "redirect:/register?error=busy";
        }
        System.out.println(passcode);
        System.out.println(LoginController.passcode);
        if(!LoginController.passcode.equals(passcode)){
            return "redirect:/register?error=passcode";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/register?error=exists";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setPhonenumber(phonenumber);
        user.setPart(part);
        userRepository.save(user);

        // 设置 5 分钟冷却时间
        redisTemplate.opsForValue().set(key, "1", 5, TimeUnit.MINUTES);

        return "redirect:/login?registered";
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.split("\\.")[0].trim();
    }
}
