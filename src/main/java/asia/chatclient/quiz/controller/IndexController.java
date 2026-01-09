package asia.chatclient.quiz.controller;

import asia.chatclient.quiz.entity.User;
import asia.chatclient.quiz.entity.UserQuiz;
import asia.chatclient.quiz.repository.UserQuizRepository;
import asia.chatclient.quiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @Autowired
    private UserQuizRepository userQuizRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String index(@AuthenticationPrincipal User user, Model model) {
        System.out.println("进入 HomeController.index");

        if (user == null) {
            model.addAttribute("login", false);
        } else {
            UserQuiz quiz = userQuizRepository.findByUserId(user.getId()).orElse(null);
            String username = userRepository.findById(user.getId()).map(User::getNickname).orElse("未知用户");
            model.addAttribute("username", username);
            if (quiz != null && quiz.isCompleted()) {
                model.addAttribute("login", true);
                model.addAttribute("complete", true);
            } else {
                model.addAttribute("login", true);
                model.addAttribute("complete", false);
            }
        }

        return "index";
    }
}
