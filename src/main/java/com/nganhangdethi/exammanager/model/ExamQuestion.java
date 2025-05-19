package com.nganhangdethi.exammanager.model;

public class ExamQuestion {
    private int examQuestionID; // Hoặc không cần nếu dùng PK phức hợp
    private int examID;
    private int questionID;
    private int questionOrderInExam;
    // private double score; // Optional

    // Optional: To hold the full Question object if needed after joining
    private Question question; 

    public ExamQuestion() {}

    public ExamQuestion(int examID, int questionID, int questionOrderInExam) {
        this.examID = examID;
        this.questionID = questionID;
        this.questionOrderInExam = questionOrderInExam;
    }

    // Getters and Setters
    public int getExamQuestionID() { return examQuestionID; }
    public void setExamQuestionID(int examQuestionID) { this.examQuestionID = examQuestionID; }
    public int getExamID() { return examID; }
    public void setExamID(int examID) { this.examID = examID; }
    public int getQuestionID() { return questionID; }
    public void setQuestionID(int questionID) { this.questionID = questionID; }
    public int getQuestionOrderInExam() { return questionOrderInExam; }
    public void setQuestionOrderInExam(int questionOrderInExam) { this.questionOrderInExam = questionOrderInExam; }
    // public double getScore() { return score; }
    // public void setScore(double score) { this.score = score; }
    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
}