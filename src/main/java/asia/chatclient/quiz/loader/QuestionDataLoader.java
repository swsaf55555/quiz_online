package asia.chatclient.quiz.loader;

import asia.chatclient.quiz.entity.Question;
import asia.chatclient.quiz.repository.QuestionRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class QuestionDataLoader implements CommandLineRunner {

    private final QuestionRepository questionRepository;

    private final List<Question> weixiuquestion = new ArrayList<>();
    private final List<Question> jishuquestion = new ArrayList<>();
    private final List<Question> wangxuanquestion = new ArrayList<>();

    private final Random random = new Random();
    private final int weixiu_num = 10;
    private final int jishu_num = 6;
    private final int wangxuan_num = 4;

    public QuestionDataLoader(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        Map<String, String> fileKeyMap = new LinkedHashMap<>();
        fileKeyMap.put("data/维修.json", "维修题");
        fileKeyMap.put("data/技术.json", "技术题");
        fileKeyMap.put("data/网宣.json", "网宣题");

        for (Map.Entry<String, String> entry : fileKeyMap.entrySet()) {
            importQuestions(entry.getKey(), entry.getValue());
        }

        System.out.println("所有题目导入完成！");
    }

    private void importQuestions(String filename, String jsonKey) {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(filename)), StandardCharsets.UTF_8)) {

            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, List<Question>>>() {
            }.getType();
            Map<String, List<Question>> data = gson.fromJson(reader, mapType);

            List<Question> questions = data.get(jsonKey);

            if (questions != null && !questions.isEmpty()) {
                // 重新排列id
                if (jsonKey.equals("技术题")) {
                    for (Question q : questions) {
                        q.setId(q.getId() + 60);
                    }
                } else if (jsonKey.equals("网宣题")) {
                    for (Question q : questions) {
                        q.setId(q.getId() + 80);
                    }
                }


                List<Question> savedQuestions = questionRepository.saveAll(questions);

                switch (jsonKey) {
                    case "维修题":
                        weixiuquestion.addAll(savedQuestions);
                        break;
                    case "技术题":
                        jishuquestion.addAll(savedQuestions);
                        break;
                    case "网宣题":
                        wangxuanquestion.addAll(savedQuestions);
                        break;
                }

                System.out.printf("成功导入 [%s] 题目数量：%d（文件：%s）%n", jsonKey, savedQuestions.size(), filename);
            } else {
                System.out.printf("未在文件 [%s] 中找到键为 [%s] 的题目数据！%n", filename, jsonKey);
            }

        } catch (Exception e) {
            System.err.printf("导入文件 [%s] 失败：%s%n", filename, e.getMessage());
            e.printStackTrace();
        }
    }

    // 随机抽题
    public List<Question> getRandomQuestions() {
        List<Question> selected = new ArrayList<>();
        selected.addAll(randomPick(weixiuquestion, weixiu_num)); // 诈骗题
        selected.addAll(randomPick(jishuquestion, jishu_num));   // 理论题
        selected.addAll(randomPick(wangxuanquestion, wangxuan_num));   // 拔高题
        //Collections.shuffle(selected); // 打乱顺序
        return selected;
    }

    private List<Question> randomPick(List<Question> source, int count) {
        if (source.isEmpty()) return Collections.emptyList();
        List<Question> copy = new ArrayList<>(source);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(count, copy.size()));
    }

    public Optional<Question> getById(Long id) {
        return questionRepository.findById(id);
    }
}
