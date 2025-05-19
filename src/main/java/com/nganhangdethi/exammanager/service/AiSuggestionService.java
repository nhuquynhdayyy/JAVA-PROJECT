package com.nganhangdethi.exammanager.service; // Hoặc package của bạn

public class AiSuggestionService {

    public AiSuggestionService() {
        // Constructor (nếu cần khởi tạo API client hoặc model sau này)
    }

    /**
     * Cung cấp gợi ý đáp án dựa trên nội dung câu hỏi và loại câu hỏi.
     * Đây là phiên bản giả lập đơn giản.
     * @param questionText Nội dung câu hỏi.
     * @param questionType Loại câu hỏi (e.g., "MultipleChoice", "FillInBlank").
     * @return Một chuỗi gợi ý.
     */
    public String getAnswerSuggestion(String questionText, String questionType) {
        if (questionText == null || questionText.trim().isEmpty()) {
            return "Không có gợi ý: Nội dung câu hỏi trống.";
        }

        // Chuyển sang chữ thường để so sánh dễ hơn
        String lowerQuestionText = questionText.toLowerCase();

        if ("MultipleChoice".equalsIgnoreCase(questionType)) {
            if (lowerQuestionText.contains("読み方") || lowerQuestionText.contains("よみかた") || lowerQuestionText.contains("発音")) {
                return "AI Gợi ý: Đây là câu hỏi về cách đọc/phát âm. Hãy chú ý các lựa chọn liên quan đến âm thanh.";
            } else if (lowerQuestionText.contains("意味") || lowerQuestionText.contains("いみ")) {
                return "AI Gợi ý: Câu hỏi về ý nghĩa. Hãy chọn từ/cụm từ đồng nghĩa hoặc giải thích phù hợp.";
            } else if (lowerQuestionText.contains("正しい形") || lowerQuestionText.contains("正しいのはどれ")) {
                return "AI Gợi ý: Tìm dạng đúng của từ hoặc ngữ pháp trong các lựa chọn.";
            }
            return "AI Gợi ý: Xem xét kỹ các lựa chọn A, B, C, D và chọn đáp án phù hợp nhất với ngữ cảnh câu hỏi.";
        } else if ("FillInBlank".equalsIgnoreCase(questionType)) {
            if (lowerQuestionText.contains("これは＿＿＿ですか")) {
                return "AI Gợi ý: Điền một danh từ thông dụng (ví dụ: 本, ペン, 猫).";
            } else if (lowerQuestionText.contains("に＿＿＿があります") || lowerQuestionText.contains("へ＿＿＿をします")) {
                return "AI Gợi ý: Điền một động từ hoặc danh từ phù hợp với giới từ.";
            }
            return "AI Gợi ý: Điền từ hoặc cụm từ còn thiếu vào chỗ trống sao cho câu có nghĩa và đúng ngữ pháp.";
        } else if ("Listening".equalsIgnoreCase(questionType)) {
            return "AI Gợi ý: Nghe kỹ đoạn hội thoại/thông báo và chọn/viết câu trả lời dựa trên những gì bạn nghe được.";
        } else if ("Reading".equalsIgnoreCase(questionType)) {
            return "AI Gợi ý: Đọc kỹ đoạn văn và trả lời câu hỏi dựa trên thông tin trong bài đọc.";
        } else if ("Essay".equalsIgnoreCase(questionType)) {
            return "AI Gợi ý: Viết một đoạn văn mạch lạc, đúng ngữ pháp và trả lời đầy đủ yêu cầu của đề bài.";
        }

        return "AI Gợi ý: Chưa có gợi ý cụ thể cho loại câu hỏi hoặc nội dung này.";
    }
}