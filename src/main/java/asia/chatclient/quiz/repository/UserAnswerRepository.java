package asia.chatclient.quiz.repository;

import asia.chatclient.quiz.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    Optional<UserAnswer> findByUserIdAndQuestionId(Long userId, Long questionId);
}
