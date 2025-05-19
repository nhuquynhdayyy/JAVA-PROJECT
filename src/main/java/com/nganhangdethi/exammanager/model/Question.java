package com.nganhangdethi.exammanager.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Question {
    private int questionID;
    private String questionText;
    private String questionType;
    private String difficultyLevel;
    private String correctAnswerKey; // For FillInBlank, Essay. For MC, it's in options.
    private String answerExplanation;
    private String aiSuggestedAnswer;
    private String audioFilePath;
    private String tags;
    private Date createdAt;
    private Date updatedAt;

    private List<QuestionOption> options; // For MultipleChoice questions

    public Question() {
        this.options = new ArrayList<>();
    }

    // Getters and Setters (bao gồm cả cho options)
    // ... (giữ các getter/setter cũ)
    public String getCorrectAnswerKey() { return correctAnswerKey; }
    public void setCorrectAnswerKey(String correctAnswerKey) { this.correctAnswerKey = correctAnswerKey; }

    public List<QuestionOption> getOptions() { return options; }
    public void setOptions(List<QuestionOption> options) { this.options = options; }
    public void addOption(QuestionOption option) { this.options.add(option); }

    // Phương thức tiện ích để lấy đáp án đúng cho câu trắc nghiệm từ options
    public String getMultipleChoiceCorrectAnswerLetter() {
        if ("MultipleChoice".equalsIgnoreCase(questionType) && options != null) {
            for (QuestionOption opt : options) {
                if (opt.isCorrect()) {
                    return opt.getOptionLetter();
                }
            }
        }
        return null; // Hoặc throw exception nếu không tìm thấy
    }
    
    // Phương thức để lấy text của đáp án đúng dựa trên correctAnswerKey hoặc options
    public String getResolvedCorrectAnswerText() {
        if ("MultipleChoice".equalsIgnoreCase(questionType) && options != null && !options.isEmpty()) {
            for (QuestionOption opt : options) {
                if (opt.isCorrect()) {
                    return opt.getOptionLetter() + ". " + opt.getOptionText();
                }
            }
            return "Error: No correct option marked for MC question.";
        }
        return this.correctAnswerKey; // For FillInBlank, Essay, etc.
    }


    // Existing Getters/Setters
    public int getQuestionID() { return questionID; }
    public void setQuestionID(int questionID) { this.questionID = questionID; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }
    // ... other getters and setters
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public String getAnswerExplanation() { return answerExplanation; }
    public void setAnswerExplanation(String answerExplanation) { this.answerExplanation = answerExplanation; }
    public String getAiSuggestedAnswer() { return aiSuggestedAnswer; }
    public void setAiSuggestedAnswer(String aiSuggestedAnswer) { this.aiSuggestedAnswer = aiSuggestedAnswer; }
    public String getAudioFilePath() { return audioFilePath; }
    public void setAudioFilePath(String audioFilePath) { this.audioFilePath = audioFilePath; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return questionID + ": " + (questionText != null ? questionText.substring(0, Math.min(questionText.length(), 50)) : "N/A") + "...";
    }
}