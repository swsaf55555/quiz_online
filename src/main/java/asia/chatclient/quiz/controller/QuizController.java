package asia.chatclient.quiz.controller;

import asia.chatclient.quiz.entity.*;
import asia.chatclient.quiz.loader.QuestionDataLoader;
import asia.chatclient.quiz.repository.QuestionRepository;
import asia.chatclient.quiz.repository.UserQuizRepository;
import asia.chatclient.quiz.repository.UserAnswerRepository;
import asia.chatclient.quiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    private static final int TIME_LIMIT_SECONDS = 30; // 每道题超时时间
    private static final int TOTAL_QUESTIONS = 20;    // 总题数

    @Autowired
    private QuestionDataLoader questionDataLoader;
    @Autowired
    private UserQuizRepository userQuizRepository;
    @Autowired
    private UserAnswerRepository userAnswerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private QuestionRepository questionRepository;

    private boolean isQuizEnabled() {
        String flag = redisTemplate.opsForValue().get("quiz_menzhen:enabled");
        return "true".equalsIgnoreCase(flag);
    }

    // 答题须知或成绩
    @GetMapping
    public String quizNotice(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        Optional<UserQuiz> quizOpt = userQuizRepository.findByUserId(user.getId());
        String nickname = userRepository.findById(user.getId()).map(User::getNickname).orElse("未知用户");
        model.addAttribute("username", nickname);
        model.addAttribute("userid",user.getId());
        if (quizOpt.isPresent() && quizOpt.get().isCompleted()) {
            model.addAttribute("score", quizOpt.get().getScore());
            return "score";
        }
        //检查是否开放
        if (!isQuizEnabled()) {
            return "unopen";
        }

        return "quiznote";
    }

    // 开始答题
    @PostMapping("/start")
    @Transactional
    public String startQuiz(@AuthenticationPrincipal User user) {
        UserQuiz quiz = userQuizRepository.findByUserId(user.getId()).orElse(null);
        // 如果已完成，禁止再次开始
        if (quiz != null && quiz.isCompleted()) {
            return "redirect:/quiz";
        }
        // 如果存在未完成记录，直接跳到当前题
        if (quiz != null && !quiz.isCompleted()) {
            Long currentQuestionId = quiz.getQuestionIds().get(quiz.getCurrentQuestionIndex());
            return "redirect:/quiz/question/" + currentQuestionId;
        }

        // 抽题
        List<Question> selected = questionDataLoader.getRandomQuestions();
        if (selected.size() < TOTAL_QUESTIONS) {
            throw new RuntimeException("题库不足，请补充题库！");
        }

        List<Long> questionIds = selected.stream().map(Question::getId).collect(Collectors.toList());

        // 初始化用户测验
        quiz = new UserQuiz();
        quiz.setUserId(user.getId());
        quiz.setScore(0);
        quiz.setCompleted(false);
        quiz.setQuestionIds(questionIds);
        quiz.setCurrentQuestionIndex(0);
        userQuizRepository.save(quiz);

        return "redirect:/quiz/question/" + questionIds.get(0);
    }

    // 显示题目
    @GetMapping("/question/{id}")
    public String showQuestion(@AuthenticationPrincipal User user, @PathVariable Long id, Model model) {

        UserQuiz quiz = userQuizRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("未开始测验"));

        if (quiz.isCompleted()) return "redirect:/quiz";
        if (!quiz.getQuestionIds().contains(id)) throw new RuntimeException("无效题目");

        Question question = questionDataLoader.getById(id).orElseThrow(() -> new RuntimeException("题目不存在"));

        // 获取已有答题记录
        UserAnswer userAnswer = userAnswerRepository.findByUserIdAndQuestionId(user.getId(), id).orElseGet(() -> {
            UserAnswer ua = new UserAnswer();
            ua.setUserId(user.getId());
            ua.setQuestionId(id);
            ua.setStartTime(LocalDateTime.now());
            return ua;
        });

        // 始终生成新的 token
        userAnswer.setToken(UUID.randomUUID().toString());
        if (userAnswer.getStartTime() == null) {
            userAnswer.setStartTime(LocalDateTime.now());
        }
        userAnswerRepository.save(userAnswer);

        String username = userRepository.findById(user.getId()).map(User::getUsername).orElse("未知用户");
        model.addAttribute("username", username);
        model.addAttribute("question", question);
        model.addAttribute("token", userAnswer.getToken());
        return "question";
    }


    // 提交答案
    @PostMapping("/answer/{id}")
    @Transactional
    public String submitAnswer(@AuthenticationPrincipal User user, @PathVariable Long id, @RequestParam(required = false) String answer, @RequestParam String token) {

        System.out.println("1");
        UserQuiz quiz = userQuizRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("未开始测验"));
        System.out.println("2");
        if (quiz.isCompleted()) {
            return "redirect:/quiz";
        }
        System.out.println("3");
        UserAnswer userAnswer = userAnswerRepository.findByUserIdAndQuestionId(user.getId(), id).orElseThrow(() -> new RuntimeException("未开始此题"));
        // token 不匹配或已使用，直接拒绝
        if (!token.equals(userAnswer.getToken())) {
            System.out.println(userAnswer.getToken());
            System.out.println(token);
            System.out.println("此题已提交或 token 无效");
            throw new RuntimeException("此题已提交或 token 无效");
        }
        // 已答过题或超时，不重复加分
        if (userAnswer.isCorrect() || userAnswer.isTimeout()) {
            System.out.println("此题已提交或超时");
            throw new RuntimeException("此题已提交或超时");
        }

        long secondsUsed = Duration.between(userAnswer.getStartTime(), LocalDateTime.now()).getSeconds();
        boolean isTimeout = secondsUsed > TIME_LIMIT_SECONDS;

        Question question = questionDataLoader.getById(id).orElseThrow(() -> new RuntimeException("题目不存在"));

        boolean isCorrect = !isTimeout && question.getCorrect().equalsIgnoreCase(answer);

        if (isCorrect) {
            quiz.setScore(quiz.getScore() + 1);
        }

        // 保存答题记录，同时将 token 标记为 null，防止再次提交
        userAnswer.setUserAnswer(answer);
        userAnswer.setCorrect(isCorrect);
        userAnswer.setTimeout(isTimeout);
        userAnswer.setToken(null);
        userAnswerRepository.save(userAnswer);

        // 更新当前题索引
        int currentIndex = quiz.getCurrentQuestionIndex();
        if (currentIndex + 1 >= TOTAL_QUESTIONS) {
            quiz.setCompleted(true);
        } else {
            quiz.setCurrentQuestionIndex(currentIndex + 1);
        }
        userQuizRepository.save(quiz);

        if (quiz.isCompleted()) {
            return "redirect:/quiz";
        } else {
            Long nextQuestionId = quiz.getQuestionIds().get(quiz.getCurrentQuestionIndex());
            return "redirect:/quiz/question/" + nextQuestionId;
        }
    }

    @GetMapping("/details")
    public String detailPage(@AuthenticationPrincipal User user,Model model,@RequestParam Long userid){
        System.out.println("1111");
        List<String> adminList = new ArrayList<>();
        try {
            Path path = Paths.get("admin.txt");
            adminList = Files.readAllLines(path);
        } catch (IOException e) {
            System.err.println("读取 admin.txt 失败: " + e.getMessage());
        }

        // 遍历判断
        boolean isAdmin = false;
        for (String u : adminList) {
            if (u.trim().equals(user.getUsername())) {
                isAdmin = true;
                break;
            }
        }
        if(!isAdmin){
            Optional<UserQuiz> quizOpt = userQuizRepository.findByUserId(user.getId());
            if (quizOpt.isPresent() && quizOpt.get().isCompleted()) {
                model.addAttribute("open_details",true);
            }else{
                model.addAttribute("open_details",false);
            }
        }else{
            model.addAttribute("open_details",true);
        }
        model.addAttribute("admin",isAdmin);
        Optional<User> userOpt = userRepository.findById(userid);
        String nickname = userRepository.findById(userid).map(User::getNickname).orElse("未知用户");
        String username = userRepository.findById(userid).map(User::getUsername).orElse("未知用户");
        String phonenumber = userRepository.findById(userid).map(User::getPhonenumber).orElse("null");
        String part = userRepository.findById(userid).map(User::getPart).orElse("null");
        model.addAttribute("nickname", nickname);
        if(isAdmin){
            model.addAttribute("phonenumber",phonenumber);
            model.addAttribute("part",part);
            model.addAttribute("username",username);
        }
        User user1=userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("用户不存在"));
        UserQuiz userQuiz=userQuizRepository.findByUserId(user1.getId()).orElseThrow(()->new RuntimeException("用户不存在"));
        UserAnswer userAnswer;
        Question question;
        List<Detail> details=new ArrayList<>();
        int index=0;
        for(Long i:userQuiz.getQuestionIds()){
            Detail detail=new Detail();
            int finalIndex = index;
            userAnswer=userAnswerRepository.findByUserIdAndQuestionId(user1.getId(), userQuiz.getQuestionIds().get(index)).orElseThrow(()->new RuntimeException("第"+ finalIndex +"次答题记录不存在"));
            question =  questionRepository.findById(userAnswer.getQuestionId()).orElseThrow(()->new RuntimeException("没有第"+i+"题"));
            detail.setQuestion(question.getQuestion());
            detail.setOptions(question.getOptions());
            detail.setCorrect(userAnswer.isCorrect());
            detail.setQuestionId(question.getId());
            detail.setTimeout(userAnswer.isTimeout());
            detail.setUserAnswer(userAnswer.getUserAnswer());
            detail.setExplanation(question.getExplanation());
            details.add(detail);
            index++;
        }
        model.addAttribute("detailList",details);
        model.addAttribute("score",userQuiz.getScore());
        return "details";
    }
}
