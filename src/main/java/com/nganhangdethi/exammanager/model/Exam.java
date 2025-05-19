package com.nganhangdethi.exammanager.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Exam {
    private int examID;
    private String examName;
    private String description;
    private boolean shuffleEnabled;
    private int totalQuestions; // Can be derived or stored
    private Date createdAt;
    // private int createdByUserID; // Optional

    // This list will hold Question objects when an exam is fully loaded
    private List<Question> questionsInExam; 
    // Or, if you only need IDs and order from ExamQuestions initially:
    // private List<ExamQuestion> examQuestionEntries; 

    public Exam() {
        this.questionsInExam = new ArrayList<>();
        // this.examQuestionEntries = new ArrayList<>();
    }

    // Getters and Setters
    public int getExamID() { return examID; }
    public void setExamID(int examID) { this.examID = examID; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isShuffleEnabled() { return shuffleEnabled; }
    public void setShuffleEnabled(boolean shuffleEnabled) { this.shuffleEnabled = shuffleEnabled; }
    public int getTotalQuestions() {
         // If questionsInExam is populated, derive from it
        if (questionsInExam != null && !questionsInExam.isEmpty()) {
            return questionsInExam.size();
        }
        return totalQuestions; // Otherwise, use stored value
    }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public List<Question> getQuestionsInExam() { return questionsInExam; }
    public void setQuestionsInExam(List<Question> questionsInExam) { this.questionsInExam = questionsInExam; }
    // public List<ExamQuestion> getExamQuestionEntries() { return examQuestionEntries; }
    // public void setExamQuestionEntries(List<ExamQuestion> examQuestionEntries) { this.examQuestionEntries = examQuestionEntries; }

    @Override
    public String toString() { // For display in JList or JComboBox
        return examID + ": " + examName + (createdAt != null ? " (" + createdAt.toInstant().toString().substring(0,10) + ")" : "");
    }
}