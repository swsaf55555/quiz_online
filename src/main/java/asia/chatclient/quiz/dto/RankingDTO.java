package asia.chatclient.quiz.dto;

import java.time.LocalDateTime;

public class RankingDTO {
    private Long userid;
    private String username;
    private String nickname;
    private String phonenumber;
    private String part;
    private int score;
    private LocalDateTime firstAnswerTime;
    private int rank;

    public RankingDTO(String nickname, int score, LocalDateTime firstAnswerTime) {
        this.username="";
        this.userid= 0L;
        this.nickname = nickname;
        this.score = score;
        this.firstAnswerTime = firstAnswerTime;
    }
    public RankingDTO(Long userid,String nickname, int score, LocalDateTime firstAnswerTime) {
        this.userid=userid;
        this.username="";
        this.nickname = nickname;
        this.score = score;
        this.firstAnswerTime = firstAnswerTime;
    }
    public RankingDTO(Long userid,String phonenumber,String part,String username,String nickname, int score, LocalDateTime firstAnswerTime) {
        this.userid=userid;
        this.phonenumber=phonenumber;
        this.part=part;
        this.username=username;
        this.nickname = nickname;
        this.score = score;
        this.firstAnswerTime = firstAnswerTime;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getFirstAnswerTime() {
        return firstAnswerTime;
    }

    public void setFirstAnswerTime(LocalDateTime firstAnswerTime) {
        this.firstAnswerTime = firstAnswerTime;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getPart() {
        return part;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }
}
