package com.nganhangdethi.exammanager.model;

public class QuestionOption implements Cloneable {
    private int optionID;
    private int questionID; // Foreign key
    private String optionLetter; // e.g., "A", "B"
    private String optionText;
    private boolean isCorrect;

    public QuestionOption() {}

    public QuestionOption(int questionID, String optionLetter, String optionText, boolean isCorrect) {
        this.questionID = questionID;
        this.optionLetter = optionLetter;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
    }

    // Getters and Setters
    public int getOptionID() { return optionID; }
    public void setOptionID(int optionID) { this.optionID = optionID; }
    public int getQuestionID() { return questionID; }
    public void setQuestionID(int questionID) { this.questionID = questionID; }
    public String getOptionLetter() { return optionLetter; }
    public void setOptionLetter(String optionLetter) { this.optionLetter = optionLetter; }
    public String getOptionText() { return optionText; }
    public void setOptionText(String optionText) { this.optionText = optionText; }
    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    @Override
    public String toString() { // For display in lists if needed
        return optionLetter + ". " + optionText + (isCorrect ? " (Correct)" : "");
    }
    
 // --- THÊM PHƯƠNG THỨC CLONE ---
    @Override
    public QuestionOption clone() throws CloneNotSupportedException {
        // Vì tất cả các trường đều là kiểu nguyên thủy hoặc String (immutable),
        // shallow copy từ super.clone() là đủ an toàn cho QuestionOption.
        return (QuestionOption) super.clone();
    }
    
}