//package com.nganhangdethi.exammanager.gui;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.HashMap;
//import java.util.Map;
//
//public class ExportDialog extends JDialog {
//    // Spinners cho từng loại câu hỏi
//    private JSpinner spnNumMcq;          // MultipleChoice
//    private JSpinner spnNumFillInBlank;
//    private JSpinner spnNumListening;
//    private JSpinner spnNumReading;
//    // Bạn có thể thêm các loại khác nếu cần
//
//    private JCheckBox chkShuffleQuestions; // Xáo trộn thứ tự câu hỏi
//    private JCheckBox chkShuffleMcqAnswers; // Xáo trộn thứ tự đáp án của câu MCQ
//    private JComboBox<String> cmbFormat;
//    private JButton btnConfirm;
//    private JButton btnCancel;
//
//    private boolean confirmed = false;
//    private final Font vietnameseFont = new Font("Arial", Font.PLAIN, 13);
//    private final Font hybridFont = new Font("Yu Gothic UI", Font.PLAIN, 14);
//
//    // Danh sách các loại câu hỏi bạn muốn người dùng có thể chọn số lượng
//    public static final String TYPE_MCQ = "MultipleChoice";
//    public static final String TYPE_FILL = "FillInBlank";
//    public static final String TYPE_LISTEN = "Listening";
//    public static final String TYPE_READ = "Reading";
//    // Thêm các loại khác ở đây nếu muốn, ví dụ:
//    // public static final String TYPE_ESSAY = "Essay";
//
//    public ExportDialog(Frame owner) {
//        super(owner, "Tùy Chọn Tạo Đề Thi Nâng Cao", true);
//        initComponents();
//        addListeners(); // Sẽ được thêm sau
//        pack();
//        setLocationRelativeTo(owner);
//        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//    }
//
//    private void initComponents() {
//        setLayout(new BorderLayout(10, 10));
//        JPanel panel = new JPanel(new GridBagLayout());
//        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(5, 5, 5, 5);
//        gbc.anchor = GridBagConstraints.WEST;
//
//        int gridY = 0;
//
//        // Số lượng câu hỏi MultipleChoice
//        gbc.gridx = 0; gbc.gridy = gridY;
//        JLabel lblNumMcq = new JLabel("Số câu Trắc nghiệm (MCQ):");
//        lblNumMcq.setFont(vietnameseFont);
//        panel.add(lblNumMcq, gbc);
//        gbc.gridx = 1;
//        spnNumMcq = new JSpinner(new SpinnerNumberModel(10, 0, 200, 1)); // Mặc định 10, min 0, max 200
//        spnNumMcq.setFont(hybridFont);
//        panel.add(spnNumMcq, gbc);
//        gridY++;
//
//        // Số lượng câu hỏi FillInBlank
//        gbc.gridx = 0; gbc.gridy = gridY;
//        JLabel lblNumFill = new JLabel("Số câu Điền khuyết:");
//        lblNumFill.setFont(vietnameseFont);
//        panel.add(lblNumFill, gbc);
//        gbc.gridx = 1;
//        spnNumFillInBlank = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
//        spnNumFillInBlank.setFont(hybridFont);
//        panel.add(spnNumFillInBlank, gbc);
//        gridY++;
//
//        // Số lượng câu hỏi Listening
//        gbc.gridx = 0; gbc.gridy = gridY;
//        JLabel lblNumListen = new JLabel("Số câu Nghe hiểu:");
//        lblNumListen.setFont(vietnameseFont);
//        panel.add(lblNumListen, gbc);
//        gbc.gridx = 1;
//        spnNumListening = new JSpinner(new SpinnerNumberModel(3, 0, 50, 1));
//        spnNumListening.setFont(hybridFont);
//        panel.add(spnNumListening, gbc);
//        gridY++;
//
//        // Số lượng câu hỏi Reading
//        gbc.gridx = 0; gbc.gridy = gridY;
//        JLabel lblNumRead = new JLabel("Số câu Đọc hiểu:");
//        lblNumRead.setFont(vietnameseFont);
//        panel.add(lblNumRead, gbc);
//        gbc.gridx = 1;
//        spnNumReading = new JSpinner(new SpinnerNumberModel(2, 0, 50, 1));
//        spnNumReading.setFont(hybridFont);
//        panel.add(spnNumReading, gbc);
//        gridY++;
//
//        // Thêm các JSpinner cho các loại câu hỏi khác nếu cần theo cách tương tự
//
//        // Đường kẻ ngang
//        gbc.gridx = 0; gbc.gridy = gridY; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
//        panel.add(new JSeparator(), gbc);
//        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; // Reset
//        gridY++;
//
//
//        // Xáo trộn thứ tự câu hỏi
//        gbc.gridx = 0; gbc.gridy = gridY;
//        JLabel lblShuffleQ = new JLabel("Xáo trộn thứ tự câu hỏi:");
//        lblShuffleQ.setFont(vietnameseFont);
//        panel.add(lblShuffleQ, gbc);
//        gbc.gridx = 1;
//        chkShuffleQuestions = new JCheckBox("", true);
//        panel.add(chkShuffleQuestions, gbc);
//        gridY++;
//
//        // Xáo trộn thứ tự đáp án MCQ
//        gbc.gridx = 0; gbc.gridy = gridY;
//        JLabel lblShuffleA = new JLabel("Xáo trộn đáp án (MCQ):");
//        lblShuffleA.setFont(vietnameseFont);
//        panel.add(lblShuffleA, gbc);
//        gbc.gridx = 1;
//        chkShuffleMcqAnswers = new JCheckBox("", false); // Mặc định không chọn
//        panel.add(chkShuffleMcqAnswers, gbc);
//        gridY++;
//
//        // Định dạng xuất
//        gbc.gridx = 0; gbc.gridy = gridY;
//        JLabel lblFormat = new JLabel("Định dạng xuất:");
//        lblFormat.setFont(vietnameseFont);
//        panel.add(lblFormat, gbc);
//        gbc.gridx = 1;
//        cmbFormat = new JComboBox<>(new String[]{"PDF", "DOCX"});
//        cmbFormat.setFont(hybridFont);
//        panel.add(cmbFormat, gbc);
//        gridY++;
//
//
//        add(panel, BorderLayout.CENTER);
//
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        btnConfirm = new JButton("Tạo Đề");
//        btnConfirm.setFont(vietnameseFont);
//        btnCancel = new JButton("Hủy");
//        btnCancel.setFont(vietnameseFont);
//        buttonPanel.add(btnConfirm);
//        buttonPanel.add(btnCancel);
//        add(buttonPanel, BorderLayout.SOUTH);
//    }
//
//    private void addListeners() {
//        btnConfirm.addActionListener(e -> {
//            // Kiểm tra tổng số câu > 0
//            if (getTotalNumberOfSelectedQuestions() <= 0) {
//                JOptionPane.showMessageDialog(this,
//                        "Vui lòng chọn ít nhất một câu hỏi cho đề thi.",
//                        "Chưa Chọn Câu Hỏi", JOptionPane.WARNING_MESSAGE);
//                return;
//            }
//            confirmed = true;
//            dispose();
//        });
//        btnCancel.addActionListener(e -> {
//            confirmed = false;
//            dispose();
//        });
//    }
//
//    public boolean isConfirmed() {
//        return confirmed;
//    }
//
//    /**
//     * Trả về một Map chứa số lượng câu hỏi mong muốn cho từng loại.
//     * Key là hằng số loại câu hỏi (ví dụ: ExportDialog.TYPE_MCQ), Value là số lượng.
//     */
//    public Map<String, Integer> getQuestionCountsPerType() {
//        Map<String, Integer> counts = new HashMap<>();
//        counts.put(TYPE_MCQ, (Integer) spnNumMcq.getValue());
//        counts.put(TYPE_FILL, (Integer) spnNumFillInBlank.getValue());
//        counts.put(TYPE_LISTEN, (Integer) spnNumListening.getValue());
//        counts.put(TYPE_READ, (Integer) spnNumReading.getValue());
//        // Thêm các loại khác nếu có
//        return counts;
//    }
//    
//    public int getTotalNumberOfSelectedQuestions() {
//        int total = 0;
//        total += (Integer) spnNumMcq.getValue();
//        total += (Integer) spnNumFillInBlank.getValue();
//        total += (Integer) spnNumListening.getValue();
//        total += (Integer) spnNumReading.getValue();
//        return total;
//    }
//
//
//    public boolean isShuffleQuestionsEnabled() {
//        return chkShuffleQuestions.isSelected();
//    }
//
//    public boolean isShuffleMcqAnswersEnabled() {
//        return chkShuffleMcqAnswers.isSelected();
//    }
//
//    public String getFormat() {
//        return (String) cmbFormat.getSelectedItem();
//    }
//}
package com.nganhangdethi.exammanager.gui;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ExportDialog extends JDialog {
    // Spinners cho từng loại câu hỏi (để tạo bộ đề gốc)
    private JSpinner spnNumMcq;
    private JSpinner spnNumFillInBlank;
    private JSpinner spnNumListening;
    private JSpinner spnNumReading;

    // Spinner cho số lượng mã đề cần tạo từ bộ đề gốc ở trên
    private JSpinner spnNumberOfExamVersions; // <<<< THÊM MỚI

    private JCheckBox chkShuffleQuestionsOrder; // Xáo trộn thứ tự câu hỏi trong mỗi mã đề
    private JCheckBox chkShuffleMcqAnswers;    // Xáo trộn thứ tự đáp án của câu MCQ trong mỗi mã đề
    private JComboBox<String> cmbFormat;
    private JButton btnConfirm;
    private JButton btnCancel;

    private boolean confirmed = false;
    private final Font vietnameseFont = new Font("Arial", Font.PLAIN, 13);
    private final Font hybridFont = new Font("Yu Gothic UI", Font.PLAIN, 14);

    public static final String TYPE_MCQ = "MultipleChoice";
    public static final String TYPE_FILL = "FillInBlank";
    public static final String TYPE_LISTEN = "Listening";
    public static final String TYPE_READ = "Reading";

    public ExportDialog(Frame owner) {
        super(owner, "Tùy Chọn Tạo Đề Thi", true);
        initComponents();
        addListeners();
        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Cho phép JSpinner co giãn theo chiều ngang

        int gridY = 0;

        // --- Phần chọn số lượng câu hỏi cho từng loại (để tạo bộ đề gốc) ---
        JLabel lblTitleSelection = new JLabel("Bước 1: Chọn số lượng câu hỏi cho bộ đề gốc");
        lblTitleSelection.setFont(vietnameseFont.deriveFont(Font.BOLD));
        gbc.gridx = 0; gbc.gridy = gridY; gbc.gridwidth = 2;
        panel.add(lblTitleSelection, gbc);
        gbc.gridwidth = 1; // Reset
        gridY++;


        // Số lượng câu hỏi MultipleChoice
        gbc.gridx = 0; gbc.gridy = gridY;
        JLabel lblNumMcq = new JLabel("Số câu Trắc nghiệm (MCQ):");
        lblNumMcq.setFont(vietnameseFont);
        panel.add(lblNumMcq, gbc);
        gbc.gridx = 1;
        spnNumMcq = new JSpinner(new SpinnerNumberModel(10, 0, 200, 1));
        spnNumMcq.setFont(hybridFont);
        panel.add(spnNumMcq, gbc);
        gridY++;

        // Số lượng câu hỏi FillInBlank
        gbc.gridx = 0; gbc.gridy = gridY;
        JLabel lblNumFill = new JLabel("Số câu Điền khuyết:");
        lblNumFill.setFont(vietnameseFont);
        panel.add(lblNumFill, gbc);
        gbc.gridx = 1;
        spnNumFillInBlank = new JSpinner(new SpinnerNumberModel(5, 0, 100, 1));
        spnNumFillInBlank.setFont(hybridFont);
        panel.add(spnNumFillInBlank, gbc);
        gridY++;

        // Số lượng câu hỏi Listening
        gbc.gridx = 0; gbc.gridy = gridY;
        JLabel lblNumListen = new JLabel("Số câu Nghe hiểu:");
        lblNumListen.setFont(vietnameseFont);
        panel.add(lblNumListen, gbc);
        gbc.gridx = 1;
        spnNumListening = new JSpinner(new SpinnerNumberModel(3, 0, 50, 1));
        spnNumListening.setFont(hybridFont);
        panel.add(spnNumListening, gbc);
        gridY++;

        // Số lượng câu hỏi Reading
        gbc.gridx = 0; gbc.gridy = gridY;
        JLabel lblNumRead = new JLabel("Số câu Đọc hiểu:");
        lblNumRead.setFont(vietnameseFont);
        panel.add(lblNumRead, gbc);
        gbc.gridx = 1;
        spnNumReading = new JSpinner(new SpinnerNumberModel(2, 0, 50, 1));
        spnNumReading.setFont(hybridFont);
        panel.add(spnNumReading, gbc);
        gridY++;

        // Đường kẻ ngang phân cách
        gbc.gridx = 0; gbc.gridy = gridY; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        gridY++;

        // --- Phần tùy chọn tạo nhiều mã đề ---
        JLabel lblTitleVersions = new JLabel("Bước 2: Tùy chọn tạo nhiều mã đề từ bộ đề gốc");
        lblTitleVersions.setFont(vietnameseFont.deriveFont(Font.BOLD));
        gbc.gridx = 0; gbc.gridy = gridY; gbc.gridwidth = 2;
        panel.add(lblTitleVersions, gbc);
        gbc.gridwidth = 1;
        gridY++;

        gbc.gridx = 0; gbc.gridy = gridY;
        JLabel lblNumVersions = new JLabel("Số lượng mã đề cần tạo:"); // <<<< LABEL MỚI
        lblNumVersions.setFont(vietnameseFont);
        panel.add(lblNumVersions, gbc);
        gbc.gridx = 1;
        spnNumberOfExamVersions = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1)); // <<<< SPINNER MỚI
        spnNumberOfExamVersions.setFont(hybridFont);
        panel.add(spnNumberOfExamVersions, gbc);
        gridY++;


        gbc.gridx = 0; gbc.gridy = gridY;
        JLabel lblShuffleQ = new JLabel("Xáo trộn thứ tự câu hỏi (trong mỗi mã đề):");
        lblShuffleQ.setFont(vietnameseFont);
        panel.add(lblShuffleQ, gbc);
        gbc.gridx = 1;
        chkShuffleQuestionsOrder = new JCheckBox("", true);
        panel.add(chkShuffleQuestionsOrder, gbc);
        gridY++;

        gbc.gridx = 0; gbc.gridy = gridY;
        JLabel lblShuffleA = new JLabel("Xáo trộn đáp án MCQ (trong mỗi mã đề):");
        lblShuffleA.setFont(vietnameseFont);
        panel.add(lblShuffleA, gbc);
        gbc.gridx = 1;
        chkShuffleMcqAnswers = new JCheckBox("", true); // Mặc định chọn xáo trộn đáp án
        panel.add(chkShuffleMcqAnswers, gbc);
        gridY++;

        gbc.gridx = 0; gbc.gridy = gridY;
        JLabel lblFormat = new JLabel("Định dạng xuất:");
        lblFormat.setFont(vietnameseFont);
        panel.add(lblFormat, gbc);
        gbc.gridx = 1;
        cmbFormat = new JComboBox<>(new String[]{"PDF", "DOCX"});
        cmbFormat.setFont(hybridFont);
        panel.add(cmbFormat, gbc);
        gridY++;


        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnConfirm = new JButton("Tạo Đề");
        btnConfirm.setFont(vietnameseFont);
        btnCancel = new JButton("Hủy");
        btnCancel.setFont(vietnameseFont);
        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        btnConfirm.addActionListener(e -> {
            if (getTotalNumberOfSelectedQuestions() <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn ít nhất một câu hỏi cho bộ đề gốc (Bước 1).",
                        "Chưa Chọn Câu Hỏi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        });
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
    }

    public boolean isConfirmed() { return confirmed; }

    public Map<String, Integer> getQuestionCountsPerType() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put(TYPE_MCQ, (Integer) spnNumMcq.getValue());
        counts.put(TYPE_FILL, (Integer) spnNumFillInBlank.getValue());
        counts.put(TYPE_LISTEN, (Integer) spnNumListening.getValue());
        counts.put(TYPE_READ, (Integer) spnNumReading.getValue());
        return counts;
    }

    public int getTotalNumberOfSelectedQuestions() {
        int total = 0;
        Map<String, Integer> counts = getQuestionCountsPerType();
        for (int count : counts.values()) {
            total += count;
        }
        return total;
    }

    // Getter cho số lượng mã đề
    public int getNumberOfExamVersions() { // <<<< GETTER MỚI
        return (int) spnNumberOfExamVersions.getValue();
    }

    public boolean isShuffleQuestionsOrderEnabled() { return chkShuffleQuestionsOrder.isSelected(); }
    public boolean isShuffleMcqAnswersEnabled() { return chkShuffleMcqAnswers.isSelected(); }
    public String getFormat() { return (String) cmbFormat.getSelectedItem(); }
}