package asia.chatclient.quiz.repository;

import asia.chatclient.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

}
