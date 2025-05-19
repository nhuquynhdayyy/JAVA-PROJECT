package com.nganhangdethi.exammanager.gui; // Hoặc package của bạn

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File; // For JFileChooser
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import com.nganhangdethi.exammanager.db.DatabaseManager;
import com.nganhangdethi.exammanager.model.Exam;
import com.nganhangdethi.exammanager.model.Question;
import com.nganhangdethi.exammanager.model.QuestionOption;
import com.nganhangdethi.exammanager.service.AiSuggestionService;
import com.nganhangdethi.exammanager.service.AudioService;
import com.nganhangdethi.exammanager.service.ExportService;

public class MainFrame extends JFrame {
    // Services and Managers
    private DatabaseManager dbManager;
    private ExportService exportService;
    private AudioService audioService;
    private AiSuggestionService aiSuggestionService;

    // UI Components for Question Bank Tab
    private JTable questionTable;
    private DefaultTableModel questionTableModel;
    private JButton btnAddQuestion;
    private JButton btnEditQuestion;
    private JButton btnDeleteQuestion;
    private JButton btnRefreshQuestions;
    private JButton btnCreateAndExportExam;

    // UI Components for Exams Tab
    private JList<Exam> examList;
    private DefaultListModel<Exam> examListModel;
    private JButton btnViewExamDetails; // Sẽ hiển thị trong một dialog hoặc panel khác
    private JButton btnExportSavedExam;
    private JButton btnDeleteSavedExam;
    private JButton btnRefreshExams;


//    private final Font hybridFont = new Font("MS Gothic", Font.PLAIN, 14);
//    private final Font hybridFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font hybridFont = new Font("Yu Gothic UI", Font.PLAIN, 14);
    private final Font vietnameseFont = new Font("Arial", Font.PLAIN, 13);
    private static final String AUDIO_STORAGE_PATH = "audio_files"; // Định nghĩa đường dẫn lưu audio

    public MainFrame() {
        // Khởi tạo services và managers
        dbManager = new DatabaseManager();
        audioService = new AudioService(AUDIO_STORAGE_PATH); // Truyền đường dẫn
        aiSuggestionService = new AiSuggestionService(); // Khởi tạo
        exportService = new ExportService(AUDIO_STORAGE_PATH); // Truyền đường dẫn nếu ExportService cần


        setTitle("Ngân Hàng Đề Thi Tiếng Nhật");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Căn giữa màn hình

        initComponents();
        addListeners();

        // Load dữ liệu ban đầu
        loadQuestionsToTable();
        loadExamsToList();

        // Test kết nối DB khi khởi động (tùy chọn)
        if (!dbManager.testConnection()) {
            JOptionPane.showMessageDialog(this,
                    "Không thể kết nối đến cơ sở dữ liệu. Một số chức năng có thể không hoạt động.",
                    "Lỗi Kết Nối CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(vietnameseFont);

        // --- Tab Ngân Hàng Câu Hỏi ---
        JPanel questionBankPanel = new JPanel(new BorderLayout(10, 10));
        questionBankPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Table Model for Questions
        questionTableModel = new DefaultTableModel(new String[]{"ID", "Nội Dung Câu Hỏi (Preview)", "Loại", "Độ Khó", "Có Âm Thanh"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp trên table
            }
        };
        questionTable = new JTable(questionTableModel);
        questionTable.setFont(hybridFont);
        questionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questionTable.setRowHeight(25);
        // Set column widths (tùy chỉnh)
        questionTable.getColumnModel().getColumn(0).setPreferredWidth(50); // ID
        questionTable.getColumnModel().getColumn(1).setPreferredWidth(500); // Text Preview
        questionTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Type
        questionTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Difficulty
        questionTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Audio

        JScrollPane questionScrollPane = new JScrollPane(questionTable);
        questionBankPanel.add(questionScrollPane, BorderLayout.CENTER);

        // Buttons Panel for Questions
        JPanel questionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAddQuestion = new JButton("Thêm Câu Hỏi");
        btnEditQuestion = new JButton("Sửa Câu Hỏi");
        btnDeleteQuestion = new JButton("Xóa Câu Hỏi");
        btnRefreshQuestions = new JButton("Làm Mới DS");
        btnCreateAndExportExam = new JButton("Tạo & Xuất Đề Thi Mới");

        btnAddQuestion.setFont(vietnameseFont);
        btnEditQuestion.setFont(vietnameseFont);
        btnDeleteQuestion.setFont(vietnameseFont);
        btnRefreshQuestions.setFont(vietnameseFont);
        btnCreateAndExportExam.setFont(vietnameseFont);

        questionButtonPanel.add(btnAddQuestion);
        questionButtonPanel.add(btnEditQuestion);
        questionButtonPanel.add(btnDeleteQuestion);
        questionButtonPanel.add(btnRefreshQuestions);
        questionButtonPanel.add(btnCreateAndExportExam);
        questionBankPanel.add(questionButtonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Ngân Hàng Câu Hỏi", questionBankPanel);


        // --- Tab Quản Lý Đề Thi ---
        JPanel examsPanel = new JPanel(new BorderLayout(10, 10));
        examsPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        examListModel = new DefaultListModel<>();
        examList = new JList<>(examListModel);
        examList.setFont(hybridFont);
        examList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane examScrollPane = new JScrollPane(examList);
        examsPanel.add(examScrollPane, BorderLayout.CENTER);

        JPanel examButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnViewExamDetails = new JButton("Xem Chi Tiết Đề");
        btnExportSavedExam = new JButton("Xuất Đề Đã Lưu");
        btnDeleteSavedExam = new JButton("Xóa Đề Đã Lưu");
        btnRefreshExams = new JButton("Làm Mới DS Đề");

        btnViewExamDetails.setFont(vietnameseFont);
        btnExportSavedExam.setFont(vietnameseFont);
        btnDeleteSavedExam.setFont(vietnameseFont);
        btnRefreshExams.setFont(vietnameseFont);

        examButtonPanel.add(btnViewExamDetails);
        examButtonPanel.add(btnExportSavedExam);
        examButtonPanel.add(btnDeleteSavedExam);
        examButtonPanel.add(btnRefreshExams);
        examsPanel.add(examButtonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("Quản Lý Đề Thi Đã Tạo", examsPanel);

        // Thêm tabbedPane vào Frame
        add(tabbedPane);
    }

    private void addListeners() {
        // Listeners cho Tab Ngân Hàng Câu Hỏi
        btnAddQuestion.addActionListener(e -> handleAddQuestion());
        btnEditQuestion.addActionListener(e -> handleEditQuestion());
        btnDeleteQuestion.addActionListener(e -> handleDeleteQuestion());
        btnRefreshQuestions.addActionListener(e -> loadQuestionsToTable());
        btnCreateAndExportExam.addActionListener(e -> handleCreateAndExportNewExam());

        // Listeners cho Tab Quản Lý Đề Thi
        btnViewExamDetails.addActionListener(e -> handleViewExamDetails());
        btnExportSavedExam.addActionListener(e -> handleExportSavedExam());
        btnDeleteSavedExam.addActionListener(e -> handleDeleteSavedExam());
        btnRefreshExams.addActionListener(e -> loadExamsToList());
    }

    private void loadQuestionsToTable() {
        questionTableModel.setRowCount(0); // Xóa dữ liệu cũ
        List<Question> questions = dbManager.getAllQuestionsForTable();
        if (questions != null) {
            for (Question q : questions) {
                questionTableModel.addRow(new Object[]{
                        q.getQuestionID(),
                        q.getQuestionText().substring(0, Math.min(q.getQuestionText().length(), 80)) + "...",
                        q.getQuestionType(),
                        q.getDifficultyLevel(),
                        (q.getAudioFilePath() != null && !q.getAudioFilePath().isEmpty()) ? "Có" : "Không"
                });
            }
        }
    }

    private void loadExamsToList() {
        examListModel.clear();
        List<Exam> exams = dbManager.getAllExams();
        if (exams != null) {
            for (Exam exam : exams) {
                examListModel.addElement(exam);
            }
        }
    }

    // --- Xử lý sự kiện cho Tab Ngân Hàng Câu Hỏi ---
    private void handleAddQuestion() {
        QuestionDialog dialog = new QuestionDialog(this, dbManager, null, audioService, aiSuggestionService);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadQuestionsToTable();
        }
    }

    private void handleEditQuestion() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow >= 0) {
            int questionId = (int) questionTableModel.getValueAt(selectedRow, 0);
            Question questionToEdit = dbManager.getQuestionById(questionId);
            if (questionToEdit != null) {
                QuestionDialog dialog = new QuestionDialog(this, dbManager, questionToEdit, audioService, aiSuggestionService);
                dialog.setVisible(true);
                if (dialog.isSaved()) {
                    loadQuestionsToTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy chi tiết câu hỏi để sửa.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để sửa.", "Chưa Chọn Câu Hỏi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleDeleteQuestion() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow >= 0) {
            int questionId = (int) questionTableModel.getValueAt(selectedRow, 0);
            String questionTextPreview = (String) questionTableModel.getValueAt(selectedRow, 1);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn xóa câu hỏi ID " + questionId + "?\n(" + questionTextPreview + ")",
                    "Xác Nhận Xóa Câu Hỏi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (dbManager.deleteQuestion(questionId)) {
                    JOptionPane.showMessageDialog(this, "Đã xóa câu hỏi ID " + questionId, "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                    loadQuestionsToTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa câu hỏi thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để xóa.", "Chưa Chọn Câu Hỏi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleCreateAndExportNewExam() {
        ExportDialog exportDialog = new ExportDialog(this);
        exportDialog.setVisible(true);

        if (exportDialog.isConfirmed()) {
            Map<String, Integer> countsPerType = exportDialog.getQuestionCountsPerType();
            boolean shuffleQuestions = exportDialog.isShuffleQuestionsEnabled();
            boolean shuffleMcqAnswers = exportDialog.isShuffleMcqAnswersEnabled();
            String format = exportDialog.getFormat();

            List<Question> questionsForExam = new ArrayList<>();

            // 1. Lấy câu hỏi theo từng loại
            for (Map.Entry<String, Integer> entry : countsPerType.entrySet()) {
                String questionType = entry.getKey();
                int count = entry.getValue();
                if (count > 0) {
                    // Bạn cần một phương thức trong DatabaseManager để lấy câu hỏi theo loại và số lượng
                    // Ví dụ: dbManager.getQuestionsByType(questionType, count);
                    // Phương thức này nên trả về các câu hỏi đã được chọn ngẫu nhiên nếu có nhiều hơn 'count' câu.
                    // Hoặc, lấy tất cả câu hỏi của loại đó rồi chọn ngẫu nhiên ở đây.
                    List<Question> questionsOfType = dbManager.getQuestionsByTypeAndLimit(questionType, count); // Giả sử có phương thức này
                    
                    if (questionsOfType.size() < count) {
                        JOptionPane.showMessageDialog(this,
                            "Không đủ " + count + " câu loại '" + questionType + "'. Chỉ lấy được " + questionsOfType.size() + " câu.",
                            "Thiếu Câu Hỏi", JOptionPane.WARNING_MESSAGE);
                    }
                    questionsForExam.addAll(questionsOfType);
                }
            }

            if (questionsForExam.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có câu hỏi nào được chọn cho đề thi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Xáo trộn thứ tự câu hỏi tổng thể (nếu được chọn)
            if (shuffleQuestions) {
                Collections.shuffle(questionsForExam);
            }

            // 3. Xáo trộn đáp án cho câu MCQ (nếu được chọn) và cập nhật lại chữ cái A,B,C,D
            if (shuffleMcqAnswers) {
                char[] optionLetters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'}; // Mảng chữ cái
                for (Question q : questionsForExam) {
                    if (ExportDialog.TYPE_MCQ.equalsIgnoreCase(q.getQuestionType()) && q.getOptions() != null && !q.getOptions().isEmpty()) {
                        List<QuestionOption> originalOptions = new ArrayList<>(q.getOptions()); // Tạo bản sao để không ảnh hưởng DB gốc nếu q từ cache
                        Collections.shuffle(originalOptions); // Xáo trộn danh sách options

                        // Cập nhật lại OptionLetter và tìm đáp án đúng mới
                        String newCorrectLetter = null;
                        List<QuestionOption> shuffledOptionsForExam = new ArrayList<>();

                        for (int i = 0; i < originalOptions.size(); i++) {
                            QuestionOption shuffledOpt = originalOptions.get(i);
                            // Tạo một QuestionOption mới hoặc clone để không thay đổi QuestionOption gốc từ DB
                            QuestionOption displayOpt = new QuestionOption();
                            // displayOpt.setOptionID(shuffledOpt.getOptionID()); // Giữ lại ID gốc nếu cần
                            displayOpt.setQuestionID(shuffledOpt.getQuestionID());
                            displayOpt.setOptionText(shuffledOpt.getOptionText());
                            displayOpt.setCorrect(shuffledOpt.isCorrect()); // Quan trọng: giữ cờ isCorrect
                            
                            if (i < optionLetters.length) {
                                displayOpt.setOptionLetter(String.valueOf(optionLetters[i])); // Gán lại A, B, C, D
                            } else {
                                displayOpt.setOptionLetter(String.valueOf(i + 1)); // Nếu nhiều hơn số chữ cái có sẵn
                            }
                            
                            if (displayOpt.isCorrect()) {
                                newCorrectLetter = displayOpt.getOptionLetter();
                            }
                            shuffledOptionsForExam.add(displayOpt);
                        }
                        q.setOptions(shuffledOptionsForExam); // Gán lại danh sách options đã xáo trộn và có chữ cái mới
                        // Cập nhật lại CorrectAnswerKey của Question q để phản ánh chữ cái mới của đáp án đúng
                        // Điều này quan trọng cho file đáp án.
                        // q.setCorrectAnswerKey(newCorrectLetter); // Hoặc bạn có một trường riêng cho việc này
                                                                  // Hoặc getResolvedCorrectAnswerText() sẽ tự tìm
                    }
                }
            }
            
            // (Phần hỏi lưu đề thi và gọi exportExamToFile như cũ)
            // ...
            // Hỏi có muốn lưu bộ đề này không
            int saveExamConfirm = JOptionPane.showConfirmDialog(this,
                    "Bạn có muốn lưu bộ đề này vào hệ thống không?",
                    "Lưu Đề Thi", JOptionPane.YES_NO_OPTION);

            String examBaseName = "DeThiTaoNgay_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());

            if (saveExamConfirm == JOptionPane.YES_OPTION) {
                String examNameInput = JOptionPane.showInputDialog(this, "Nhập tên cho bộ đề thi này:", examBaseName);
                if (examNameInput != null && !examNameInput.trim().isEmpty()) {
                    Exam newExam = new Exam();
                    newExam.setExamName(examNameInput.trim());
                    newExam.setDescription("Đề thi được tạo tự động ngày " + new java.util.Date());
                    newExam.setShuffleEnabled(shuffleQuestions);
                    // questionsForExam lúc này đã có thể có options đã xáo trộn
                    Exam savedExam = dbManager.addExam(newExam, questionsForExam);
                    if (savedExam != null) {
                        JOptionPane.showMessageDialog(this, "Đã lưu đề thi '" + savedExam.getExamName() + "' vào hệ thống.", "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                        loadExamsToList();
                        examBaseName = savedExam.getExamName().replaceAll("[^a-zA-Z0-9.-]", "_"); // Dùng tên đã lưu làm base
                    } else {
                        JOptionPane.showMessageDialog(this, "Lưu đề thi thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (examNameInput != null) {
                     JOptionPane.showMessageDialog(this, "Tên đề thi không được để trống. Đề thi sẽ không được lưu.", "Thông Báo", JOptionPane.WARNING_MESSAGE);
                }
            }
            exportExamToFile(questionsForExam, format, examBaseName);
        }
    }

    // --- Xử lý sự kiện cho Tab Quản Lý Đề Thi ---
    private void handleViewExamDetails() {
        Exam selectedExam = examList.getSelectedValue();
        if (selectedExam == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để xem.", "Chưa Chọn Đề Thi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Lấy chi tiết đề thi (bao gồm các câu hỏi)
        Exam examWithDetails = dbManager.getExamById(selectedExam.getExamID());
        if (examWithDetails != null && examWithDetails.getQuestionsInExam() != null) {
            // Hiển thị trong một dialog mới hoặc một panel chi tiết
            // Ví dụ đơn giản: hiển thị trong JOptionPane
            StringBuilder sb = new StringBuilder();
            sb.append("Đề thi: ").append(examWithDetails.getExamName()).append("\n");
            sb.append("Mô tả: ").append(examWithDetails.getDescription()).append("\n");
            sb.append("Tổng số câu: ").append(examWithDetails.getQuestionsInExam().size()).append("\n\n");
            sb.append("Danh sách câu hỏi:\n");
            int count = 1;
            for (Question q : examWithDetails.getQuestionsInExam()) {
                sb.append(count++).append(". (ID: ").append(q.getQuestionID()).append(") ")
                  .append(q.getQuestionText().substring(0, Math.min(50, q.getQuestionText().length()))).append("...\n");
            }
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setCaretPosition(0);
            textArea.setEditable(false);
            textArea.setFont(hybridFont);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            JOptionPane.showMessageDialog(this, scrollPane, "Chi Tiết Đề Thi: " + examWithDetails.getExamName(), JOptionPane.INFORMATION_MESSAGE);

        } else {
            JOptionPane.showMessageDialog(this, "Không thể tải chi tiết đề thi.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExportSavedExam() {
        Exam selectedExam = examList.getSelectedValue();
        if (selectedExam == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để xuất.", "Chưa Chọn Đề Thi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Exam examWithDetails = dbManager.getExamById(selectedExam.getExamID());
        if (examWithDetails != null && examWithDetails.getQuestionsInExam() != null && !examWithDetails.getQuestionsInExam().isEmpty()) {
            String format = (String) JOptionPane.showInputDialog(
                    this,
                    "Chọn định dạng xuất:",
                    "Xuất Đề Đã Lưu",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    new String[]{"PDF", "DOCX"},
                    "PDF");

            if (format != null) {
                exportExamToFile(examWithDetails.getQuestionsInExam(), format, selectedExam.getExamName().replaceAll("[^a-zA-Z0-9.-]", "_"));
            }
        } else {
            JOptionPane.showMessageDialog(this, "Đề thi này không có câu hỏi hoặc không thể tải chi tiết.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteSavedExam() {
        Exam selectedExam = examList.getSelectedValue();
        if (selectedExam == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đề thi để xóa.", "Chưa Chọn Đề Thi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa đề thi '" + selectedExam.getExamName() + "' không?",
                "Xác Nhận Xóa Đề Thi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dbManager.deleteExam(selectedExam.getExamID())) {
                JOptionPane.showMessageDialog(this, "Đã xóa đề thi '" + selectedExam.getExamName() + "'.", "Thành Công", JOptionPane.INFORMATION_MESSAGE);
                loadExamsToList();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa đề thi thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Phương thức tiện ích ---
    private void exportExamToFile(List<Question> questionsToExport, String format, String baseFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu File Đề Thi");
        String extension = format.equalsIgnoreCase("PDF") ? ".pdf" : ".docx";
        fileChooser.setSelectedFile(new File(baseFileName + extension));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File examFile = fileChooser.getSelectedFile();
            // Đảm bảo file có đuôi đúng
            if (!examFile.getName().toLowerCase().endsWith(extension)) {
                examFile = new File(examFile.getParentFile(), examFile.getName() + extension);
            }

            String answerKeyFileName = "DapAn_" + examFile.getName().substring(0, examFile.getName().lastIndexOf('.')) + extension;
            File answerFile = new File(examFile.getParentFile(), answerKeyFileName);

            try {
                if (format.equalsIgnoreCase("PDF")) {
                    exportService.exportToPdf(questionsToExport, examFile.getAbsolutePath(), answerFile.getAbsolutePath());
                } else { // DOCX
                    exportService.exportToDocx(questionsToExport, examFile.getAbsolutePath(), answerFile.getAbsolutePath());
                }
                JOptionPane.showMessageDialog(this,
                        "Đã xuất đề thi và đáp án thành công!\nĐề thi: " + examFile.getAbsolutePath() +
                        "\nĐáp án: " + answerFile.getAbsolutePath(),
                        "Xuất Thành Công", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất file: " + ex.getMessage(), "Lỗi Xuất File", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // Set Look and Feel (Optional, for better UI consistency)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Đảm bảo ứng dụng chạy trên Event Dispatch Thread của Swing
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}