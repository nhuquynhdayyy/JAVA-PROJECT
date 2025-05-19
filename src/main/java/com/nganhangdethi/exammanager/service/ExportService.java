package com.nganhangdethi.exammanager.service; // Hoặc package của bạn

import com.nganhangdethi.exammanager.model.Question;
import com.nganhangdethi.exammanager.model.QuestionOption;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
// import java.io.FileInputStream; // Không cần nếu load từ resource
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExportService {
    private String audioStorageBasePath; // Đường dẫn cơ sở đến thư mục audio, dùng để tham khảo

    public ExportService(String audioStorageBasePath) {
        this.audioStorageBasePath = audioStorageBasePath;
        // Không load font ở constructor nữa, sẽ load cho từng document
    }

    // --- PDF Export ---
    public void exportToPdf(List<Question> questions, String examFilePath, String answerKeyFilePath) throws IOException {
        // Xuất Đề Thi
        try (PDDocument examDoc = new PDDocument()) {
            PDType0Font fontForExamDoc = loadJapaneseFont(examDoc);
            if (fontForExamDoc == null) {
                throw new IOException("Không thể load font tiếng Nhật cho đề thi PDF. Kiểm tra file font trong resources/fonts.");
            }
            exportContentToPdf(examDoc, fontForExamDoc, questions, "ĐỀ THI TIẾNG NHẬT", false);
            examDoc.save(examFilePath);
            System.out.println("Đã xuất đề thi PDF: " + examFilePath);
        }

        // Xuất Đáp Án
        try (PDDocument answerDoc = new PDDocument()) {
            PDType0Font fontForAnswerDoc = loadJapaneseFont(answerDoc);
            if (fontForAnswerDoc == null) {
                throw new IOException("Không thể load font tiếng Nhật cho đáp án PDF. Kiểm tra file font trong resources/fonts.");
            }
            exportContentToPdf(answerDoc, fontForAnswerDoc, questions, "ĐÁP ÁN CHI TIẾT", true);
            answerDoc.save(answerKeyFilePath);
            System.out.println("Đã xuất đáp án PDF: " + answerKeyFilePath);
        }
    }

    private PDType0Font loadJapaneseFont(PDDocument document) throws IOException {
        // Đảm bảo file NotoSansJP-Regular.ttf nằm trong src/main/resources/fonts/
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/NotoSansJP-Regular.ttf")) {
            if (fontStream == null) {
                System.err.println("LỖI NGHIÊM TRỌNG: Không tìm thấy file font '/fonts/NotoSansJP-Regular.ttf' trong classpath (resources).");
                System.err.println("Hãy đảm bảo file font tồn tại trong thư mục 'src/main/resources/fonts/' của project.");
                return null;
            }
            // Tham số thứ 3 `false` nghĩa là không subset font, nhúng toàn bộ font vào PDF.
            // Điều này làm tăng kích thước file PDF nhưng đảm bảo hiển thị trên mọi máy.
            // Nếu muốn giảm kích thước, có thể đặt là `true` nhưng cần test kỹ.
            return PDType0Font.load(document, fontStream, false);
        } catch (IOException e) {
            System.err.println("Lỗi IO khi load font tiếng Nhật cho PDF: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void exportContentToPdf(PDDocument document, PDType0Font japaneseFont, List<Question> questions, String title, boolean isAnswerKey) throws IOException {
        PDPage currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);
        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

        float margin = 50;
        float yStart = currentPage.getMediaBox().getHeight() - margin;
        float yPosition = yStart;
        float usableWidth = currentPage.getMediaBox().getWidth() - 2 * margin;
        float defaultLeading = 18f; // Khoảng cách dòng mặc định
        float questionFontSize = 12f;
        float optionFontSize = 11f;
        float titleFontSize = 16f;
        float explanationFontSize = 10f;
        float audioNoteFontSize = 9f;

        // Title
        contentStream.beginText();
        contentStream.setFont(japaneseFont, titleFontSize);
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(title);
        yPosition -= defaultLeading * 1.5f;
        contentStream.endText();

        int qNum = 1;
        for (Question q : questions) {
            contentStream.beginText(); // Bắt đầu khối text mới cho mỗi câu
            contentStream.setFont(japaneseFont, questionFontSize);
            contentStream.newLineAtOffset(margin, yPosition); // Reset vị trí cho mỗi câu hỏi

            String questionDisplay;
            if (isAnswerKey) {
                questionDisplay = qNum + ". Đáp án: " + q.getResolvedCorrectAnswerText();
            } else {
                questionDisplay = qNum + ". " + q.getQuestionText();
            }

            List<String> wrappedLines = wrapText(questionDisplay, usableWidth, japaneseFont, questionFontSize);
            for (String line : wrappedLines) {
                if (yPosition - defaultLeading < margin) { // Nếu sắp hết trang
                    contentStream.endText();
                    contentStream.close();
                    currentPage = new PDPage(PDRectangle.A4);
                    document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage);
                    yPosition = yStart;
                    contentStream.beginText();
                    contentStream.setFont(japaneseFont, questionFontSize);
                    contentStream.newLineAtOffset(margin, yPosition);
                }
                contentStream.showText(line);
                yPosition -= defaultLeading;
                if (!line.equals(wrappedLines.get(wrappedLines.size()-1))) { // Nếu không phải dòng cuối của wrapped text
                    contentStream.newLineAtOffset(0, -defaultLeading); // Xuống dòng trong cùng khối text
                }
            }
            contentStream.endText(); // Kết thúc khối text của phần câu hỏi/đáp án chính
            // yPosition đã được cập nhật bởi vòng lặp trên


            // In các lựa chọn (nếu không phải là đáp án và là MCQ)
            if (!isAnswerKey && "MultipleChoice".equalsIgnoreCase(q.getQuestionType()) && q.getOptions() != null) {
                contentStream.beginText();
                contentStream.setFont(japaneseFont, optionFontSize);
                contentStream.newLineAtOffset(margin + 20, yPosition); // Thụt lề cho options

                for (QuestionOption opt : q.getOptions()) {
                    String optionLine = opt.getOptionLetter() + ". " + opt.getOptionText();
                    List<String> wrappedOptionLines = wrapText(optionLine, usableWidth - 20, japaneseFont, optionFontSize);
                    for (String line : wrappedOptionLines) {
                         if (yPosition - defaultLeading * 0.9f < margin) {
                            contentStream.endText(); contentStream.close(); currentPage = new PDPage(PDRectangle.A4); document.addPage(currentPage);
                            contentStream = new PDPageContentStream(document, currentPage); yPosition = yStart;
                            contentStream.beginText(); contentStream.setFont(japaneseFont, optionFontSize); contentStream.newLineAtOffset(margin + 20, yPosition);
                        }
                        contentStream.showText(line);
                        yPosition -= defaultLeading * 0.9f;
                        if (!line.equals(wrappedOptionLines.get(wrappedOptionLines.size()-1))) {
                           contentStream.newLineAtOffset(0, -defaultLeading * 0.9f);
                        }
                    }
                }
                contentStream.endText();
            }

            // In giải thích (nếu là đáp án và có giải thích)
            if (isAnswerKey && q.getAnswerExplanation() != null && !q.getAnswerExplanation().isEmpty()) {
                contentStream.beginText();
                contentStream.setFont(japaneseFont, explanationFontSize);
                contentStream.newLineAtOffset(margin + 10, yPosition); // Thụt lề ít hơn

                String explanationFull = "Giải thích: " + q.getAnswerExplanation();
                List<String> wrappedExplanationLines = wrapText(explanationFull, usableWidth - 10, japaneseFont, explanationFontSize);
                for(String line : wrappedExplanationLines){
                     if (yPosition - defaultLeading * 0.8f < margin) {
                        contentStream.endText(); contentStream.close(); currentPage = new PDPage(PDRectangle.A4); document.addPage(currentPage);
                        contentStream = new PDPageContentStream(document, currentPage); yPosition = yStart;
                        contentStream.beginText(); contentStream.setFont(japaneseFont, explanationFontSize); contentStream.newLineAtOffset(margin + 10, yPosition);
                    }
                    contentStream.showText(line);
                    yPosition -= defaultLeading * 0.8f;
                    if (!line.equals(wrappedExplanationLines.get(wrappedExplanationLines.size()-1))) {
                         contentStream.newLineAtOffset(0, -defaultLeading * 0.8f);
                    }
                }
                contentStream.endText();
            }

            // Thông tin file âm thanh
            if (!isAnswerKey && q.getAudioFilePath() != null && !q.getAudioFilePath().isEmpty()) {
                contentStream.beginText();
                contentStream.setFont(japaneseFont, audioNoteFontSize);
                contentStream.newLineAtOffset(margin + 20, yPosition);

                String audioNote = "(File âm thanh: " + q.getAudioFilePath() + ")";
                if (yPosition - defaultLeading * 0.7f < margin) {
                    contentStream.endText(); contentStream.close(); currentPage = new PDPage(PDRectangle.A4); document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage); yPosition = yStart;
                    contentStream.beginText(); contentStream.setFont(japaneseFont, audioNoteFontSize); contentStream.newLineAtOffset(margin + 20, yPosition);
                }
                contentStream.showText(audioNote);
                yPosition -= defaultLeading * 0.7f;
                contentStream.endText();
            }
            yPosition -= defaultLeading * 0.5f; // Khoảng cách giữa các câu hỏi
            qNum++;
        }
        contentStream.close(); // Đóng content stream của trang cuối cùng
    }

    // Helper để wrap text cho PDF
    private List<String> wrapText(String text, float maxWidth, PDType0Font font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add(" "); // Trả về một khoảng trắng để tránh lỗi khi showText("")
            return lines;
        }

        String[] paragraphs = text.split("\n"); // Tách theo dấu xuống dòng có sẵn

        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) { // Xử lý dòng trống do người dùng nhập \n\n
                lines.add(" ");
                continue;
            }
            String remaining = paragraph;
            while (remaining.length() > 0) {
                int breakPoint = remaining.length(); // Mặc định lấy cả dòng nếu vừa
                float currentLineWidth = font.getStringWidth(remaining) / 1000 * fontSize;

                if (currentLineWidth > maxWidth) { // Nếu dòng hiện tại quá dài, cần ngắt
                    // Lùi lại từng ký tự để tìm điểm ngắt
                    for (int i = remaining.length() - 1; i > 0; i--) {
                        String sub = remaining.substring(0, i);
                        if (font.getStringWidth(sub) / 1000 * fontSize <= maxWidth) {
                            // Tìm điểm ngắt tự nhiên (khoảng trắng, dấu câu) gần điểm i này
                            int tempBreakPoint = i;
                            for (int j = i; j > 0; j--) {
                                char c = remaining.charAt(j - 1);
                                if (c == ' ' || c == '　' || c == '。' || c == '、' || c == '.' || c == ',' || c == '?' || c == '!') {
                                    tempBreakPoint = j; // Ngắt sau dấu câu/khoảng trắng
                                    break;
                                }
                            }
                             // Nếu không tìm thấy điểm ngắt tự nhiên hoặc điểm ngắt tự nhiên quá xa, ngắt cứng
                            if (i - tempBreakPoint > 15 && i < remaining.length() *0.7 ) { // Heuristic
                                breakPoint = i; // Ngắt cứng
                            } else {
                                breakPoint = tempBreakPoint;
                            }
                            break;
                        }
                    }
                    if (breakPoint == remaining.length() && currentLineWidth > maxWidth) { // Nếu không thể ngắt được dòng dài (ví dụ 1 từ rất dài)
                        // Cố gắng ngắt ở ký tự gần nhất có thể
                         for (int i = 1; i < remaining.length(); i++) {
                            if (font.getStringWidth(remaining.substring(0, i)) / 1000 * fontSize > maxWidth) {
                                breakPoint = i-1 > 0 ? i-1 : 1;
                                break;
                            }
                        }
                    }
                }
                lines.add(remaining.substring(0, breakPoint));
                remaining = remaining.substring(breakPoint).trim(); // Bỏ khoảng trắng đầu dòng mới
            }
        }
        if (lines.isEmpty()) lines.add(" "); // Đảm bảo luôn có ít nhất một dòng
        return lines;
    }


    // --- DOCX Export ---
    public void exportToDocx(List<Question> questions, String examFilePath, String answerKeyFilePath) throws IOException {
        // Xuất Đề Thi
        try (XWPFDocument examDoc = new XWPFDocument(); FileOutputStream out = new FileOutputStream(examFilePath)) {
            exportContentToDocx(examDoc, questions, "ĐỀ THI TIẾNG NHẬT", false);
            examDoc.write(out);
            System.out.println("Đã xuất đề thi DOCX: " + examFilePath);
        }

        // Xuất Đáp Án
        try (XWPFDocument answerDoc = new XWPFDocument(); FileOutputStream out = new FileOutputStream(answerKeyFilePath)) {
            exportContentToDocx(answerDoc, questions, "ĐÁP ÁN CHI TIẾT", true);
            answerDoc.write(out);
            System.out.println("Đã xuất đáp án DOCX: " + answerKeyFilePath);
        }
    }

    private void exportContentToDocx(XWPFDocument document, List<Question> questions, String titleText, boolean isAnswerKey) {
        // Title
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText(titleText);
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.setFontFamily("MS Mincho"); // Hoặc Yu Gothic, Meiryo, Noto Sans JP nếu Word hỗ trợ tốt
        titleRun.addBreak();

        int qNum = 1;
        for (Question q : questions) {
            XWPFParagraph qPara = document.createParagraph();
            qPara.setSpacingBefore(100); // Khoảng cách trước mỗi câu hỏi

            String questionDisplay;
             if (isAnswerKey) {
                questionDisplay = qNum + ". Đáp án: " + q.getResolvedCorrectAnswerText();
            } else {
                questionDisplay = qNum + ". " + q.getQuestionText();
            }
            addMultilineTextToRun(qPara.createRun(), questionDisplay, "MS Mincho", 12, false);


            // In các lựa chọn (nếu không phải là đáp án và là MCQ)
            if (!isAnswerKey && "MultipleChoice".equalsIgnoreCase(q.getQuestionType()) && q.getOptions() != null) {
                for (QuestionOption opt : q.getOptions()) {
                    XWPFParagraph optionPara = document.createParagraph();
                    // Để thụt lề, bạn có thể dùng setIndentationLeft hoặc thêm khoảng trắng/tab vào đầu chuỗi
                    optionPara.setIndentationLeft(360); // 0.25 inch = 360 twentieths of a point
                    String optionLine = opt.getOptionLetter() + ". " + opt.getOptionText();
                    addMultilineTextToRun(optionPara.createRun(), optionLine, "MS Mincho", 11, false);
                }
            }

            // In giải thích (nếu là đáp án và có giải thích)
            if (isAnswerKey && q.getAnswerExplanation() != null && !q.getAnswerExplanation().isEmpty()) {
                XWPFParagraph explanationPara = document.createParagraph();
                explanationPara.setIndentationLeft(360);
                String explanationLine = "Giải thích: " + q.getAnswerExplanation();
                addMultilineTextToRun(explanationPara.createRun(), explanationLine, "MS Mincho", 11, true); // Italic cho giải thích
            }

            // Thông tin file âm thanh
             if (!isAnswerKey && q.getAudioFilePath() != null && !q.getAudioFilePath().isEmpty()) {
                XWPFParagraph audioPara = document.createParagraph();
                audioPara.setIndentationLeft(360);
                XWPFRun audioRun = audioPara.createRun();
                audioRun.setText("(File âm thanh: " + q.getAudioFilePath() + ")");
                audioRun.setFontFamily("MS Mincho");
                audioRun.setFontSize(10);
                audioRun.setItalic(true);
            }
            qNum++;
        }
    }

    // Helper để thêm text có thể có nhiều dòng vào XWPFRun
    private void addMultilineTextToRun(XWPFRun run, String text, String fontFamily, int fontSize, boolean isItalic) {
        if (text == null) return;
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            run.setText(lines[i]);
            run.setFontFamily(fontFamily);
            run.setFontSize(fontSize);
            run.setItalic(isItalic);
            if (i < lines.length - 1) {
                run.addBreak(); // Thêm ngắt dòng của Word
            }
        }
    }
}