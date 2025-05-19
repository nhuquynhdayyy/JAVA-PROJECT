package com.nganhangdethi.exammanager.db;

import com.nganhangdethi.exammanager.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    // CẢNH BÁO BẢO MẬT: Không nên hardcode mật khẩu 'sa' trong code production.
    // Xem xét sử dụng file cấu hình hoặc các phương pháp quản lý bí mật an toàn hơn.
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=JapaneseExamBankDB;user=sa;password=Quynh141356@;encrypt=true;trustServerCertificate=true";

    private Connection getConnection() throws SQLException {
        // Với JDBC 4.0+, driver thường được tự động load nếu nằm trong classpath.
        return DriverManager.getConnection(DB_URL);
    }

    // --- Question and QuestionOption Methods ---

    public boolean addQuestion(Question q) {
        String sqlQuestion = "INSERT INTO Questions (QuestionText, QuestionType, DifficultyLevel, CorrectAnswerKey, AnswerExplanation, AISuggestedAnswer, AudioFilePath, Tags) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlOption = "INSERT INTO QuestionOptions (QuestionID, OptionLetter, OptionText, IsCorrect) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmtQuestion = null;
        PreparedStatement pstmtOption = null;
        ResultSet generatedKeys = null; // Khai báo ở đây để đóng trong finally
        boolean success = false;

        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Bắt đầu transaction

            pstmtQuestion = conn.prepareStatement(sqlQuestion, Statement.RETURN_GENERATED_KEYS);
            pstmtQuestion.setNString(1, q.getQuestionText());
            pstmtQuestion.setNString(2, q.getQuestionType());
            pstmtQuestion.setNString(3, q.getDifficultyLevel());

            if ("MultipleChoice".equalsIgnoreCase(q.getQuestionType())) {
                pstmtQuestion.setNull(4, Types.NVARCHAR); // CorrectAnswerKey không dùng cho MCQ trong bảng Questions
            } else {
                pstmtQuestion.setNString(4, q.getCorrectAnswerKey());
            }
            pstmtQuestion.setNString(5, q.getAnswerExplanation());
            pstmtQuestion.setNString(6, q.getAiSuggestedAnswer());
            pstmtQuestion.setNString(7, q.getAudioFilePath());
            pstmtQuestion.setNString(8, q.getTags());

            int affectedRows = pstmtQuestion.executeUpdate();

            if (affectedRows > 0) {
                generatedKeys = pstmtQuestion.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int questionId = generatedKeys.getInt(1);
                    q.setQuestionID(questionId);

                    if ("MultipleChoice".equalsIgnoreCase(q.getQuestionType()) && q.getOptions() != null && !q.getOptions().isEmpty()) {
                        pstmtOption = conn.prepareStatement(sqlOption);
                        for (QuestionOption option : q.getOptions()) {
                            option.setQuestionID(questionId);
                            pstmtOption.setInt(1, option.getQuestionID());
                            pstmtOption.setNString(2, option.getOptionLetter());
                            pstmtOption.setNString(3, option.getOptionText());
                            pstmtOption.setBoolean(4, option.isCorrect());
                            pstmtOption.addBatch();
                        }
                        pstmtOption.executeBatch();
                    }
                    success = true;
                }
            }
            conn.commit(); // Lưu tất cả thay đổi
            System.out.println("Câu hỏi ID " + q.getQuestionID() + " đã được thêm thành công.");
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thêm câu hỏi: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    System.err.println("Transaction đang được rollback.");
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Lỗi khi rollback transaction: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        } finally {
            try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmtOption != null) pstmtOption.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmtQuestion != null) pstmtQuestion.close(); } catch (SQLException e) { e.printStackTrace(); }
            try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Đặt lại autoCommit
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public boolean updateQuestion(Question q) {
        String sqlUpdateQuestion = "UPDATE Questions SET QuestionText=?, QuestionType=?, DifficultyLevel=?, CorrectAnswerKey=?, " +
                                 "AnswerExplanation=?, AISuggestedAnswer=?, AudioFilePath=?, Tags=?, UpdatedAt=GETDATE() " + // Thêm UpdatedAt
                                 "WHERE QuestionID=?";
        String sqlDeleteOptions = "DELETE FROM QuestionOptions WHERE QuestionID=?";
        String sqlInsertOption = "INSERT INTO QuestionOptions (QuestionID, OptionLetter, OptionText, IsCorrect) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement pstmtUpdateQuestion = null;
        PreparedStatement pstmtDeleteOptions = null;
        PreparedStatement pstmtInsertOption = null;
        boolean success = false;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            pstmtUpdateQuestion = conn.prepareStatement(sqlUpdateQuestion);
            pstmtUpdateQuestion.setNString(1, q.getQuestionText());
            pstmtUpdateQuestion.setNString(2, q.getQuestionType());
            pstmtUpdateQuestion.setNString(3, q.getDifficultyLevel());
            if ("MultipleChoice".equalsIgnoreCase(q.getQuestionType())) {
                pstmtUpdateQuestion.setNull(4, Types.NVARCHAR);
            } else {
                pstmtUpdateQuestion.setNString(4, q.getCorrectAnswerKey());
            }
            pstmtUpdateQuestion.setNString(5, q.getAnswerExplanation());
            pstmtUpdateQuestion.setNString(6, q.getAiSuggestedAnswer());
            pstmtUpdateQuestion.setNString(7, q.getAudioFilePath());
            pstmtUpdateQuestion.setNString(8, q.getTags());
            pstmtUpdateQuestion.setInt(9, q.getQuestionID());
            pstmtUpdateQuestion.executeUpdate();

            // Xử lý options: Xóa cũ, thêm mới
            pstmtDeleteOptions = conn.prepareStatement(sqlDeleteOptions);
            pstmtDeleteOptions.setInt(1, q.getQuestionID());
            pstmtDeleteOptions.executeUpdate(); // Xóa hết options cũ

            if ("MultipleChoice".equalsIgnoreCase(q.getQuestionType()) && q.getOptions() != null && !q.getOptions().isEmpty()) {
                pstmtInsertOption = conn.prepareStatement(sqlInsertOption);
                for (QuestionOption option : q.getOptions()) {
                    option.setQuestionID(q.getQuestionID());
                    pstmtInsertOption.setInt(1, option.getQuestionID());
                    pstmtInsertOption.setNString(2, option.getOptionLetter());
                    pstmtInsertOption.setNString(3, option.getOptionText());
                    pstmtInsertOption.setBoolean(4, option.isCorrect());
                    pstmtInsertOption.addBatch();
                }
                pstmtInsertOption.executeBatch();
            }
            conn.commit();
            success = true;
            System.out.println("Câu hỏi ID " + q.getQuestionID() + " đã được cập nhật thành công.");
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi cập nhật câu hỏi ID " + q.getQuestionID() + ": " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    System.err.println("Transaction đang được rollback.");
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Lỗi khi rollback transaction: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        } finally {
            try { if (pstmtInsertOption != null) pstmtInsertOption.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmtDeleteOptions != null) pstmtDeleteOptions.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmtUpdateQuestion != null) pstmtUpdateQuestion.close(); } catch (SQLException e) { e.printStackTrace(); }
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public Question getQuestionById(int questionID) {
        Question question = null;
        String sqlQuestion = "SELECT * FROM Questions WHERE QuestionID = ?";
        String sqlOptions = "SELECT * FROM QuestionOptions WHERE QuestionID = ? ORDER BY OptionLetter ASC";

        try (Connection conn = getConnection(); // Try-with-resources cho Connection
             PreparedStatement pstmtQuestion = conn.prepareStatement(sqlQuestion)) {

            pstmtQuestion.setInt(1, questionID);
            try (ResultSet rsQuestion = pstmtQuestion.executeQuery()) { // Try-with-resources cho ResultSet
                if (rsQuestion.next()) {
                    question = new Question();
                    // ... (Gán các trường cho question như code gốc của bạn) ...
                    question.setQuestionID(rsQuestion.getInt("QuestionID"));
                    question.setQuestionText(rsQuestion.getNString("QuestionText"));
                    question.setQuestionType(rsQuestion.getNString("QuestionType"));
                    question.setDifficultyLevel(rsQuestion.getNString("DifficultyLevel"));
                    question.setCorrectAnswerKey(rsQuestion.getNString("CorrectAnswerKey"));
                    question.setAnswerExplanation(rsQuestion.getNString("AnswerExplanation"));
                    question.setAiSuggestedAnswer(rsQuestion.getNString("AISuggestedAnswer"));
                    question.setAudioFilePath(rsQuestion.getNString("AudioFilePath"));
                    question.setTags(rsQuestion.getNString("Tags"));
                    question.setCreatedAt(rsQuestion.getTimestamp("CreatedAt"));
                    question.setUpdatedAt(rsQuestion.getTimestamp("UpdatedAt"));


                    if ("MultipleChoice".equalsIgnoreCase(question.getQuestionType())) {
                        // Try-with-resources cho PreparedStatement và ResultSet của options
                        try (PreparedStatement pstmtOptions = conn.prepareStatement(sqlOptions)) {
                            pstmtOptions.setInt(1, questionID);
                            try (ResultSet rsOptions = pstmtOptions.executeQuery()) {
                                List<QuestionOption> options = new ArrayList<>();
                                while (rsOptions.next()) {
                                    QuestionOption opt = new QuestionOption();
                                     // ... (Gán các trường cho option như code gốc của bạn) ...
                                    opt.setOptionID(rsOptions.getInt("OptionID"));
                                    opt.setQuestionID(rsOptions.getInt("QuestionID"));
                                    opt.setOptionLetter(rsOptions.getNString("OptionLetter"));
                                    opt.setOptionText(rsOptions.getNString("OptionText"));
                                    opt.setCorrect(rsOptions.getBoolean("IsCorrect"));
                                    options.add(opt);
                                }
                                question.setOptions(options);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy câu hỏi theo ID " + questionID + ": " + e.getMessage());
            e.printStackTrace();
        }
        return question;
    }

    // getAllQuestions() - bạn có thể giữ nguyên phương thức này nếu muốn load đầy đủ chi tiết
    // hoặc ưu tiên dùng getAllQuestionsForTable() cho hiệu năng hiển thị danh sách.
    // Tôi sẽ giữ lại getAllQuestionsForTable() vì nó hiệu quả hơn cho mục đích chính.
    public List<Question> getAllQuestionsForTable() {
        List<Question> questions = new ArrayList<>();
        // Lấy thêm CreatedAt để có thể sort theo ngày tạo nếu muốn
        String sql = "SELECT QuestionID, QuestionText, QuestionType, DifficultyLevel, AudioFilePath, CreatedAt FROM Questions ORDER BY CreatedAt DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Question q = new Question();
                q.setQuestionID(rs.getInt("QuestionID"));
                q.setQuestionText(rs.getNString("QuestionText"));
                q.setQuestionType(rs.getNString("QuestionType"));
                q.setDifficultyLevel(rs.getNString("DifficultyLevel"));
                q.setAudioFilePath(rs.getNString("AudioFilePath"));
                q.setCreatedAt(rs.getTimestamp("CreatedAt"));
                questions.add(q);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy danh sách câu hỏi: " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }

    public boolean deleteQuestion(int questionID) {
        String sql = "DELETE FROM Questions WHERE QuestionID = ?";
        boolean success = false;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, questionID);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                success = true;
                System.out.println("Câu hỏi ID " + questionID + " đã được xóa thành công.");
            } else {
                System.out.println("Không tìm thấy câu hỏi ID " + questionID + " để xóa.");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi xóa câu hỏi ID " + questionID + ": " + e.getMessage());
            e.printStackTrace();
        }
        return success;
    }


    // --- Exam and ExamQuestion Methods ---

    public Exam addExam(Exam exam, List<Question> questionsInThisExam) {
        String sqlExam = "INSERT INTO Exams (ExamName, Description, ShuffleEnabled, TotalQuestions) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmtExam = null;
        ResultSet generatedKeys = null;
        Exam addedExam = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            pstmtExam = conn.prepareStatement(sqlExam, Statement.RETURN_GENERATED_KEYS);
            pstmtExam.setNString(1, exam.getExamName());
            pstmtExam.setNString(2, exam.getDescription());
            pstmtExam.setBoolean(3, exam.isShuffleEnabled());
            pstmtExam.setInt(4, questionsInThisExam != null ? questionsInThisExam.size() : 0);

            int affectedRows = pstmtExam.executeUpdate();
            if (affectedRows > 0) {
                generatedKeys = pstmtExam.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int examId = generatedKeys.getInt(1);
                    exam.setExamID(examId); // Cập nhật ID cho đối tượng Exam
                    addedExam = exam; // Gán exam đã có ID

                    if (questionsInThisExam != null && !questionsInThisExam.isEmpty()) {
                        addQuestionsToExamInternal(conn, examId, questionsInThisExam); // Gọi hàm nội bộ
                    }
                }
            }
            conn.commit();
            if(addedExam != null) System.out.println("Đề thi '" + addedExam.getExamName() + "' đã được thêm thành công với ID: " + addedExam.getExamID());

        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi thêm đề thi: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            addedExam = null; // Trả về null nếu có lỗi
        } finally {
            try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmtExam != null) pstmtExam.close(); } catch (SQLException e) { e.printStackTrace(); }
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return addedExam; // Trả về exam đã được thêm (có ID) hoặc null nếu lỗi
    }

    // Phương thức nội bộ để thêm câu hỏi vào ExamQuestions, sử dụng Connection đã có và là một phần của transaction lớn hơn
    private void addQuestionsToExamInternal(Connection conn, int examID, List<Question> questions) throws SQLException {
        String sql = "INSERT INTO ExamQuestions (ExamID, QuestionID, QuestionOrderInExam) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // PreparedStatement sẽ được đóng tự động
            int order = 1;
            for (Question q : questions) {
                if (q.getQuestionID() <= 0) { // Đảm bảo câu hỏi đã có ID (đã được lưu vào DB)
                    throw new SQLException("Câu hỏi '" + q.getQuestionText().substring(0,10) + "...' chưa có ID, không thể thêm vào đề thi.");
                }
                pstmt.setInt(1, examID);
                pstmt.setInt(2, q.getQuestionID());
                pstmt.setInt(3, order++);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
        // Không commit ở đây vì nó là một phần của transaction lớn hơn
    }


    public Exam getExamById(int examID) {
        Exam exam = null;
        String sqlExam = "SELECT * FROM Exams WHERE ExamID = ?";
        // Sửa câu lệnh SQL để join và lấy QuestionText, không cần gọi getQuestionById nhiều lần
        String sqlExamQuestions = "SELECT eq.QuestionID, eq.QuestionOrderInExam, q.QuestionText, q.QuestionType, q.DifficultyLevel, q.AudioFilePath " +
                                  "FROM ExamQuestions eq " +
                                  "JOIN Questions q ON eq.QuestionID = q.QuestionID " +
                                  "WHERE eq.ExamID = ? ORDER BY eq.QuestionOrderInExam ASC";
        try (Connection conn = getConnection();
             PreparedStatement pstmtExam = conn.prepareStatement(sqlExam)) {

            pstmtExam.setInt(1, examID);
            try (ResultSet rsExam = pstmtExam.executeQuery()) {
                if (rsExam.next()) {
                    exam = new Exam();
                    exam.setExamID(rsExam.getInt("ExamID"));
                    exam.setExamName(rsExam.getNString("ExamName"));
                    exam.setDescription(rsExam.getNString("Description"));
                    exam.setShuffleEnabled(rsExam.getBoolean("ShuffleEnabled"));
                    exam.setTotalQuestions(rsExam.getInt("TotalQuestions")); // Dùng giá trị đã lưu
                    exam.setCreatedAt(rsExam.getTimestamp("CreatedAt"));

                    List<Question> questionsInExam = new ArrayList<>();
                    try (PreparedStatement pstmtExamQ = conn.prepareStatement(sqlExamQuestions)) {
                        pstmtExamQ.setInt(1, examID);
                        try (ResultSet rsExamQ = pstmtExamQ.executeQuery()) {
                            while (rsExamQ.next()) {
                                // Thay vì gọi getQuestionById, tạo Question từ ResultSet của JOIN
                                Question q = new Question();
                                q.setQuestionID(rsExamQ.getInt("QuestionID"));
                                q.setQuestionText(rsExamQ.getNString("QuestionText"));
                                q.setQuestionType(rsExamQ.getNString("QuestionType"));
                                q.setDifficultyLevel(rsExamQ.getNString("DifficultyLevel"));
                                q.setAudioFilePath(rsExamQ.getNString("AudioFilePath"));
                                // q.setQuestionOrderInExam(rsExamQ.getInt("QuestionOrderInExam")); // Nếu bạn thêm trường này vào model Question
                                
                                // Nếu cần load options cho câu hỏi này (ví dụ khi hiển thị chi tiết đề thi)
                                if ("MultipleChoice".equalsIgnoreCase(q.getQuestionType())) {
                                    loadOptionsForQuestion(conn, q); // Hàm private helper
                                }
                                questionsInExam.add(q);
                            }
                        }
                    }
                    exam.setQuestionsInExam(questionsInExam);
                    // Nếu TotalQuestions trong DB là 0 hoặc không khớp, cập nhật lại
                    if (exam.getTotalQuestions() != questionsInExam.size()) {
                         System.out.println("Cảnh báo: TotalQuestions trong DB cho ExamID " + examID + " (" + exam.getTotalQuestions() + ") không khớp với số câu hỏi thực tế (" + questionsInExam.size() + ").");
                        // exam.setTotalQuestions(questionsInExam.size()); // Cân nhắc có nên tự động sửa không
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy đề thi theo ID " + examID + ": " + e.getMessage());
            e.printStackTrace();
        }
        return exam;
    }

    // Helper private method to load options for a given question using an existing connection
    private void loadOptionsForQuestion(Connection conn, Question question) throws SQLException {
        String sqlOptions = "SELECT * FROM QuestionOptions WHERE QuestionID = ? ORDER BY OptionLetter ASC";
        try (PreparedStatement pstmtOptions = conn.prepareStatement(sqlOptions)) {
            pstmtOptions.setInt(1, question.getQuestionID());
            try (ResultSet rsOptions = pstmtOptions.executeQuery()) {
                List<QuestionOption> options = new ArrayList<>();
                while (rsOptions.next()) {
                    QuestionOption opt = new QuestionOption();
                    opt.setOptionID(rsOptions.getInt("OptionID"));
                    opt.setQuestionID(rsOptions.getInt("QuestionID"));
                    opt.setOptionLetter(rsOptions.getNString("OptionLetter"));
                    opt.setOptionText(rsOptions.getNString("OptionText"));
                    opt.setCorrect(rsOptions.getBoolean("IsCorrect"));
                    options.add(opt);
                }
                question.setOptions(options);
            }
        }
    }


    public List<Exam> getAllExams() {
        List<Exam> exams = new ArrayList<>();
        String sql = "SELECT ExamID, ExamName, Description, ShuffleEnabled, TotalQuestions, CreatedAt FROM Exams ORDER BY CreatedAt DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Exam exam = new Exam();
                exam.setExamID(rs.getInt("ExamID"));
                exam.setExamName(rs.getNString("ExamName"));
                exam.setDescription(rs.getNString("Description"));
                exam.setShuffleEnabled(rs.getBoolean("ShuffleEnabled"));
                exam.setTotalQuestions(rs.getInt("TotalQuestions"));
                exam.setCreatedAt(rs.getTimestamp("CreatedAt"));
                exams.add(exam);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi lấy danh sách tất cả đề thi: " + e.getMessage());
            e.printStackTrace();
        }
        return exams;
    }

    public boolean updateExam(Exam exam, List<Question> newQuestionsInThisExam) { // Thêm tham số List<Question> mới
        String sqlUpdateExam = "UPDATE Exams SET ExamName=?, Description=?, ShuffleEnabled=?, TotalQuestions=? WHERE ExamID=?";
        String sqlDeleteExamQuestions = "DELETE FROM ExamQuestions WHERE ExamID=?"; // Để xóa các câu hỏi cũ

        Connection conn = null;
        PreparedStatement pstmtUpdateExam = null;
        PreparedStatement pstmtDeleteEQ = null;
        boolean success = false;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            pstmtUpdateExam = conn.prepareStatement(sqlUpdateExam);
            pstmtUpdateExam.setNString(1, exam.getExamName());
            pstmtUpdateExam.setNString(2, exam.getDescription());
            pstmtUpdateExam.setBoolean(3, exam.isShuffleEnabled());
            pstmtUpdateExam.setInt(4, newQuestionsInThisExam != null ? newQuestionsInThisExam.size() : 0); // Cập nhật TotalQuestions
            pstmtUpdateExam.setInt(5, exam.getExamID());
            pstmtUpdateExam.executeUpdate();

            // Xóa tất cả các liên kết câu hỏi cũ của đề thi này
            pstmtDeleteEQ = conn.prepareStatement(sqlDeleteExamQuestions);
            pstmtDeleteEQ.setInt(1, exam.getExamID());
            pstmtDeleteEQ.executeUpdate();

            // Thêm lại các liên kết câu hỏi mới (nếu có)
            if (newQuestionsInThisExam != null && !newQuestionsInThisExam.isEmpty()) {
                addQuestionsToExamInternal(conn, exam.getExamID(), newQuestionsInThisExam);
            }

            conn.commit();
            success = true;
            System.out.println("Đề thi ID " + exam.getExamID() + " đã được cập nhật thành công.");
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi cập nhật đề thi ID " + exam.getExamID() + ": " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        } finally {
            try { if (pstmtDeleteEQ != null) pstmtDeleteEQ.close(); } catch (SQLException e) { e.printStackTrace(); }
            try { if (pstmtUpdateExam != null) pstmtUpdateExam.close(); } catch (SQLException e) { e.printStackTrace(); }
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public boolean deleteExam(int examID) {
        String sql = "DELETE FROM Exams WHERE ExamID=?"; // ON DELETE CASCADE sẽ xóa các ExamQuestions liên quan
        boolean success = false;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, examID);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                success = true;
                System.out.println("Đề thi ID " + examID + " đã được xóa thành công.");
            } else {
                 System.out.println("Không tìm thấy đề thi ID " + examID + " để xóa.");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL khi xóa đề thi ID " + examID + ": " + e.getMessage());
            e.printStackTrace();
        }
        return success;
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Kiểm tra kết nối thất bại: Lỗi SQL.");
            e.printStackTrace();
            return false;
        }
    }
}