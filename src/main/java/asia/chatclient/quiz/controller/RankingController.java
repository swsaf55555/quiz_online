package asia.chatclient.quiz.controller;

import asia.chatclient.quiz.dto.RankingDTO;
import asia.chatclient.quiz.entity.User;
import asia.chatclient.quiz.repository.UserQuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;

import java.util.List;

@Controller
public class RankingController {

    @Autowired
    private UserQuizRepository userQuizRepository;
    @GetMapping("/ranking")
    public String rankingPage(@AuthenticationPrincipal User user, Model model) {

        List<String> adminList = new ArrayList<>();
        try {
            Path path = Paths.get("admin.txt");
            adminList = Files.readAllLines(path);
        } catch (IOException e) {
            System.err.println("读取 admin.txt 失败: " + e.getMessage());
        }

        boolean isAdmin = false;
        for (String u : adminList) {
            if (u.trim().equals(user.getUsername())) {
                isAdmin = true;
                break;
            }
        }
        List<RankingDTO> rankingList = new ArrayList<>();
        if (!isAdmin) {
            rankingList = userQuizRepository.getRanking();
            rankingList.sort(Comparator.comparingInt(RankingDTO::getScore).reversed().thenComparing(RankingDTO::getFirstAnswerTime));
            int rank = 1;
            int sameRank = 1;
            RankingDTO prev = null;
            for (RankingDTO r : rankingList) {
                if (prev != null && r.getScore() == prev.getScore() && r.getFirstAnswerTime().equals(prev.getFirstAnswerTime())) {
                    r.setRank(prev.getRank());
                    sameRank++;
                } else {
                    r.setRank(rank);
                    rank += sameRank;
                    sameRank = 1;
                }
                prev = r;
            }
            model.addAttribute("admin",false);
        } else {
            rankingList = userQuizRepository.getRankingAdmin();
            rankingList.sort(Comparator.comparingInt(RankingDTO::getScore).reversed().thenComparing(RankingDTO::getFirstAnswerTime));
            int rank = 1;
            int sameRank = 1;
            RankingDTO prev = null;
            for (RankingDTO r : rankingList) {
                if (prev != null && r.getScore() == prev.getScore() && r.getFirstAnswerTime().equals(prev.getFirstAnswerTime())) {
                    r.setRank(prev.getRank());
                    sameRank++;
                } else {
                    r.setRank(rank);
                    rank += sameRank;
                    sameRank = 1;
                }
                prev = r;
            }
            model.addAttribute("admin",true);
        }

        model.addAttribute("rankingList", rankingList);
        return "ranking";
    }

}


