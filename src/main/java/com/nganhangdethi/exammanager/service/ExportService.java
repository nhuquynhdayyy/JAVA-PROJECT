package com.nganhangdethi.exammanager.service;

import com.nganhangdethi.exammanager.model.Question;
import com.nganhangdethi.exammanager.model.QuestionOption;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import org.apache.poi.xwpf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExportService {
    private String audioStorageBasePath;

    public ExportService(String audioStorageBasePath) {
        this.audioStorageBasePath = audioStorageBasePath;
    }

    // --- PDF Export ---
    public void exportToPdf(List<Question> questions, String examFilePath, String answerKeyFilePath) throws IOException {
        // Xuất Đề Thi
        try (PDDocument examDoc = new PDDocument()) {
            PDType0Font fontForExamDoc = loadJapaneseFont(examDoc);
            if (fontForExamDoc == null) {
                throw new IOException("Không thể load font tiếng Nhật cho đề thi PDF. Kiểm tra file font trong resources/fonts.");
            }
            // Truyền font đã load vào phương thức exportContentToPdf
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
            // Truyền font đã load vào phương thức exportContentToPdf
            exportContentToPdf(answerDoc, fontForAnswerDoc, questions, "ĐÁP ÁN CHI TIẾT", true);
            answerDoc.save(answerKeyFilePath);
            System.out.println("Đã xuất đáp án PDF: " + answerKeyFilePath);
        }
    }

    private PDType0Font loadJapaneseFont(PDDocument document) throws IOException {
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/NotoSansJP-Regular.ttf")) {
            if (fontStream == null) {
                System.err.println("LỖI NGHIÊM TRỌNG: Không tìm thấy file font '/fonts/NotoSansJP-Regular.ttf' trong classpath (resources).");
                System.err.println("Hãy đảm bảo file font tồn tại trong thư mục 'src/main/resources/fonts/' của project.");
                return null;
            }
            return PDType0Font.load(document, fontStream, false);
        } catch (IOException e) {
            System.err.println("Lỗi IO khi load font tiếng Nhật cho PDF: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Phương thức này giờ nhận PDType0Font làm tham số
    private void exportContentToPdf(PDDocument document, PDType0Font japaneseFont, List<Question> questions, String title, boolean isAnswerKey) throws IOException {
        PDPage currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);
        PDPageContentStream contentStream = new PDPageContentStream(document, currentPage);

        float margin = 50;
        float yStart = currentPage.getMediaBox().getHeight() - margin;
        float yPosition = yStart;
        float usableWidth = currentPage.getMediaBox().getWidth() - 2 * margin;
        float defaultLeading = 18f;
        float questionFontSize = 12f;
        float optionFontSize = 11f;
        float titleFontSize = 16f;
        float explanationFontSize = 10f;
        float audioNoteFontSize = 9f;

        // Title
        contentStream.beginText();
        contentStream.setFont(japaneseFont, titleFontSize); // Sử dụng font được truyền vào
        contentStream.newLineAtOffset(margin, yPosition);
        contentStream.showText(title);
        yPosition -= defaultLeading * 1.5f;
        contentStream.endText();

        int qNum = 1;
        for (Question q : questions) {
            // --- Phần hiển thị câu hỏi/đáp án chính ---
            contentStream.beginText();
            contentStream.setFont(japaneseFont, questionFontSize); // Font cho câu hỏi
            contentStream.newLineAtOffset(margin, yPosition);

            String questionDisplay;
            if (isAnswerKey) {
                // Giả định getResolvedCorrectAnswerText() trả về chữ cái (đã xáo trộn nếu cần) và nội dung đáp án đúng
                questionDisplay = qNum + ". Đáp án: " + q.getResolvedCorrectAnswerText();
            } else {
                questionDisplay = qNum + ". " + q.getQuestionText();
            }

            List<String> wrappedQuestionLines = wrapText(questionDisplay, usableWidth, japaneseFont, questionFontSize);
            for (String line : wrappedQuestionLines) {
                if (yPosition - defaultLeading < margin) { // Page break
                    contentStream.endText(); contentStream.close();
                    currentPage = new PDPage(PDRectangle.A4); document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage); yPosition = yStart;
                    contentStream.beginText(); contentStream.setFont(japaneseFont, questionFontSize); contentStream.newLineAtOffset(margin, yPosition);
                }
                contentStream.showText(line);
                yPosition -= defaultLeading;
                if (!line.equals(wrappedQuestionLines.get(wrappedQuestionLines.size()-1))) {
                    contentStream.newLineAtOffset(0, -defaultLeading);
                }
            }
            contentStream.endText(); // Kết thúc khối text cho câu hỏi/đáp án chính
            // yPosition đã được cập nhật


            // --- Phần hiển thị các lựa chọn (chỉ cho đề thi, không phải đáp án, và là MCQ) ---
            if (!isAnswerKey && "MultipleChoice".equalsIgnoreCase(q.getQuestionType()) && q.getOptions() != null) {
                // yPosition đã ở vị trí sau câu hỏi, không cần reset yPosition = yStart
                // Bắt đầu khối text mới cho các options
                contentStream.beginText();
                contentStream.setFont(japaneseFont, optionFontSize); // Font cho options
                // Thiết lập vị trí bắt đầu cho option đầu tiên, thụt lề so với câu hỏi
                contentStream.newLineAtOffset(margin + 20, yPosition); 

                for (QuestionOption opt : q.getOptions()) { // q.getOptions() NÊN chứa các options đã xáo trộn với OptionLetter đúng
                    String optionLine = opt.getOptionLetter() + ". " + opt.getOptionText();
                    List<String> wrappedOptionLines = wrapText(optionLine, usableWidth - 20, japaneseFont, optionFontSize);
                    for (String line : wrappedOptionLines) {
                         if (yPosition - defaultLeading * 0.9f < margin) { // Page break
                            contentStream.endText(); contentStream.close();
                            currentPage = new PDPage(PDRectangle.A4); document.addPage(currentPage);
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
                contentStream.endText(); // Kết thúc khối text cho options
            }

            // --- Phần hiển thị giải thích (chỉ cho đáp án) ---
            if (isAnswerKey && q.getAnswerExplanation() != null && !q.getAnswerExplanation().isEmpty()) {
                contentStream.beginText();
                contentStream.setFont(japaneseFont, explanationFontSize);
                contentStream.newLineAtOffset(margin + 10, yPosition);

                String explanationFull = "Giải thích: " + q.getAnswerExplanation();
                List<String> wrappedExplanationLines = wrapText(explanationFull, usableWidth - 10, japaneseFont, explanationFontSize);
                for(String line : wrappedExplanationLines){
                     if (yPosition - defaultLeading * 0.8f < margin) { // Page break
                        contentStream.endText(); contentStream.close();
                        currentPage = new PDPage(PDRectangle.A4); document.addPage(currentPage);
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

            // --- Phần thông tin file âm thanh (chỉ cho đề thi) ---
            if (!isAnswerKey && q.getAudioFilePath() != null && !q.getAudioFilePath().isEmpty()) {
                contentStream.beginText();
                contentStream.setFont(japaneseFont, audioNoteFontSize);
                contentStream.newLineAtOffset(margin + 20, yPosition);

                String audioNote = "(File âm thanh: " + q.getAudioFilePath() + ")";
                 if (yPosition - defaultLeading * 0.7f < margin) { // Page break
                    contentStream.endText(); contentStream.close();
                    currentPage = new PDPage(PDRectangle.A4); document.addPage(currentPage);
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
        contentStream.close();
    }

    private List<String> wrapText(String text, float maxWidth, PDType0Font font, float fontSize) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add(" ");
            return lines;
        }
        String[] paragraphs = text.split("\n");
        for (String paragraph : paragraphs) {
            if (paragraph.isEmpty()) {
                lines.add(" ");
                continue;
            }
            String remaining = paragraph;
            while (remaining.length() > 0) {
                int breakPoint = remaining.length();
                float currentLineWidth = font.getStringWidth(remaining) / 1000 * fontSize;
                if (currentLineWidth > maxWidth) {
                    for (int i = remaining.length() - 1; i > 0; i--) {
                        String sub = remaining.substring(0, i);
                        if (font.getStringWidth(sub) / 1000 * fontSize <= maxWidth) {
                            int tempBreakPoint = i;
                            for (int j = i; j > 0; j--) {
                                char c = remaining.charAt(j - 1);
                                if (c == ' ' || c == '　' || c == '。' || c == '、' || c == '.' || c == ',' || c == '?' || c == '!') {
                                    tempBreakPoint = j;
                                    break;
                                }
                            }
                            if (i - tempBreakPoint > 15 && i < remaining.length() * 0.7) {
                                breakPoint = i;
                            } else {
                                breakPoint = tempBreakPoint;
                            }
                            // Đảm bảo không ngắt giữa chừng một từ tiếng Nhật quá ngắn nếu không cần thiết
                            if (breakPoint < 2 && remaining.length() > 2 && font.getStringWidth(remaining.substring(0, 2)) / 1000 * fontSize <=maxWidth) {
                                // Cố gắng không ngắt nếu chỉ có 1 ký tự và ký tự tiếp theo vẫn vừa
                            } else {
                                break;
                            }
                        }
                    }
                    if (breakPoint == remaining.length() && currentLineWidth > maxWidth) {
                         for (int i = 1; i < remaining.length(); i++) {
                            if (font.getStringWidth(remaining.substring(0, i)) / 1000 * fontSize > maxWidth) {
                                breakPoint = i-1 > 0 ? i-1 : 1;
                                break;
                            }
                        }
                    }
                }
                lines.add(remaining.substring(0, breakPoint));
                remaining = remaining.substring(breakPoint).trim();
            }
        }
        if (lines.isEmpty()) lines.add(" ");
        return lines;
    }

    // --- DOCX Export ---
    public void exportToDocx(List<Question> questions, String examFilePath, String answerKeyFilePath) throws IOException {
        try (XWPFDocument examDoc = new XWPFDocument(); FileOutputStream out = new FileOutputStream(examFilePath)) {
            exportContentToDocx(examDoc, questions, "ĐỀ THI TIẾNG NHẬT", false);
            examDoc.write(out);
            System.out.println("Đã xuất đề thi DOCX: " + examFilePath);
        }
        try (XWPFDocument answerDoc = new XWPFDocument(); FileOutputStream out = new FileOutputStream(answerKeyFilePath)) {
            exportContentToDocx(answerDoc, questions, "ĐÁP ÁN CHI TIẾT", true);
            answerDoc.write(out);
            System.out.println("Đã xuất đáp án DOCX: " + answerKeyFilePath);
        }
    }

    private void exportContentToDocx(XWPFDocument document, List<Question> questions, String titleText, boolean isAnswerKey) {
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        titleRun.setText(titleText);
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.setFontFamily("MS Mincho");
        titleRun.addBreak();

        int qNum = 1;
        for (Question q : questions) {
            XWPFParagraph qPara = document.createParagraph();
            qPara.setSpacingBefore(100);

            String questionDisplay;
             if (isAnswerKey) {
                questionDisplay = qNum + ". Đáp án: " + q.getResolvedCorrectAnswerText();
            } else {
                questionDisplay = qNum + ". " + q.getQuestionText();
            }
            addMultilineTextToRun(qPara.createRun(), questionDisplay, "MS Mincho", 12, false);

            if (!isAnswerKey && "MultipleChoice".equalsIgnoreCase(q.getQuestionType()) && q.getOptions() != null) {
                for (QuestionOption opt : q.getOptions()) { // Giả định q.getOptions() đã được xáo trộn và có OptionLetter đúng
                    XWPFParagraph optionPara = document.createParagraph();
                    optionPara.setIndentationLeft(360);
                    String optionLine = opt.getOptionLetter() + ". " + opt.getOptionText();
                    addMultilineTextToRun(optionPara.createRun(), optionLine, "MS Mincho", 11, false);
                }
            }
            if (isAnswerKey && q.getAnswerExplanation() != null && !q.getAnswerExplanation().isEmpty()) {
                XWPFParagraph explanationPara = document.createParagraph();
                explanationPara.setIndentationLeft(360);
                String explanationLine = "Giải thích: " + q.getAnswerExplanation();
                addMultilineTextToRun(explanationPara.createRun(), explanationLine, "MS Mincho", 11, true);
            }
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

    private void addMultilineTextToRun(XWPFRun run, String text, String fontFamily, int fontSize, boolean isItalic) {
        if (text == null) return;
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            run.setText(lines[i]);
            run.setFontFamily(fontFamily);
            run.setFontSize(fontSize);
            run.setItalic(isItalic);
            if (i < lines.length - 1) {
                run.addBreak();
            }
        }
    }
}