package asia.chatclient.quiz.repository;

import asia.chatclient.quiz.dto.RankingDTO;
import asia.chatclient.quiz.entity.UserQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserQuizRepository extends JpaRepository<UserQuiz, Long> {
    Optional<UserQuiz> findByUserId(Long userId);

    @Query("""
                SELECT new asia.chatclient.quiz.dto.RankingDTO(u.id,u.nickname, q.score, MIN(a.startTime))
                FROM User u
                JOIN UserQuiz q ON u.id = q.userId
                JOIN UserAnswer a ON u.id = a.userId
                WHERE q.completed = true
                GROUP BY u.id, u.username,u.nickname, q.score
                ORDER BY q.score DESC, MIN(a.startTime) ASC
            """)
    List<RankingDTO> getRanking();

    @Query("""
                SELECT new asia.chatclient.quiz.dto.RankingDTO(u.id,u.phonenumber,u.part,u.username,u.nickname, q.score, MIN(a.startTime))
                FROM User u
                JOIN UserQuiz q ON u.id = q.userId
                JOIN UserAnswer a ON u.id = a.userId
                WHERE q.completed = true
                GROUP BY u.id, u.username,u.nickname,u.phonenumber,u.part, q.score
                ORDER BY q.score DESC, MIN(a.startTime) ASC
            """)
    List<RankingDTO> getRankingAdmin();
}
