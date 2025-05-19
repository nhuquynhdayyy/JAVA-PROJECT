package com.nganhangdethi.exammanager.gui; // Hoặc package của bạn

import com.nganhangdethi.exammanager.db.DatabaseManager;
import com.nganhangdethi.exammanager.model.Question;
import com.nganhangdethi.exammanager.model.QuestionOption;
import com.nganhangdethi.exammanager.service.AiSuggestionService;
import com.nganhangdethi.exammanager.service.AudioService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // For unique option ID generation if not from DB

public class QuestionDialog extends JDialog {
    // Components cho thông tin câu hỏi chung
    private JTextField txtQuestionID;
    private JTextArea txtQuestionText;
    private JComboBox<String> cmbQuestionType;
    private JComboBox<String> cmbDifficulty;
    private JTextArea txtCorrectAnswerKey;
    private JTextArea txtAnswerExplanation;
    private JTextField txtAudioFilePath;
    private JButton btnBrowseAudio;
    private JTextArea txtAiSuggestedAnswer;
    private JButton btnGetAiSuggestion;
    private JTextField txtTags;

    // Components cho quản lý QuestionOptions
    private JPanel panelOptionsManagement;
    private JTextField txtOptionLetter;
    private JTextArea txtOptionTextContent;
    private JCheckBox chkIsCorrectOption;
    private JButton btnAddOptionToList;
    private JButton btnUpdateSelectedOption;
    private JButton btnRemoveSelectedOption;
    private JList<QuestionOption> listGuiOptions;
    private DefaultListModel<QuestionOption> optionListModel;

    private JButton btnSave;
    private JButton btnCancel;

    private DatabaseManager dbManager;
    private AiSuggestionService aiService;
    private AudioService audioService;
    private Question currentQuestion;
    private boolean saved = false;
//    private final Font hybridFont = new Font("MS Gothic", Font.PLAIN, 14);
    private final Font vietnameseFont = new Font("Arial", Font.PLAIN, 13);
//    private final Font hybridFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font hybridFont = new Font("Yu Gothic UI", Font.PLAIN, 14);

    // Giả định MainFrame sẽ truyền AUDIO_BASE_PATH này cho AudioService khi khởi tạo
    // private String audioStoragePath; // Nếu AudioService không tự biết

    public QuestionDialog(Frame owner, DatabaseManager dbManager, Question questionToEdit, AudioService audioService, AiSuggestionService aiService) {
        super(owner, (questionToEdit == null ? "Thêm Câu Hỏi Mới" : "Sửa Câu Hỏi"), true);
        this.dbManager = dbManager;
        this.currentQuestion = questionToEdit;
        this.audioService = audioService;
        this.aiService = aiService;
        // this.audioStoragePath = audioStoragePath; // Nếu cần truyền

        initComponents();
        addListeners();
        toggleOptionsPanelVisibility(); // Gọi lần đầu để setup dựa trên type mặc định

        if (currentQuestion != null) {
            populateFields();
        }

        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    public boolean isSaved() {
        return saved;
    }

    private void initComponents() {
        // ... (Phần initComponents như bạn đã code ở trên, không thay đổi) ...
        // Đảm bảo các component đã được khởi tạo trong initComponents()
        // Ví dụ:
        setLayout(new BorderLayout(10, 10));

        JPanel mainFormPanel = new JPanel(new GridBagLayout());
        mainFormPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Hàng 0: Question ID
        gbc.gridx = 0; gbc.gridy = 0; mainFormPanel.add(new JLabel("ID:"), gbc);
        txtQuestionID = new JTextField(5);
        txtQuestionID.setEditable(false);
        txtQuestionID.setFont(hybridFont);
        gbc.gridx = 1; gbc.gridwidth = 3; mainFormPanel.add(txtQuestionID, gbc); // Đã sửa gridwidth
        gbc.gridwidth = 1;

        // Hàng 1: Question Text
        gbc.gridx = 0; gbc.gridy = 1; mainFormPanel.add(new JLabel("Nội dung câu hỏi (JP):"), gbc);
        txtQuestionText = new JTextArea(4, 30);
        txtQuestionText.setLineWrap(true);
        txtQuestionText.setWrapStyleWord(true);
        txtQuestionText.setFont(hybridFont);
        JScrollPane scrollQuestionText = new JScrollPane(txtQuestionText);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        mainFormPanel.add(scrollQuestionText, gbc);
        gbc.gridwidth = 1; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL;

        // Hàng 2: Type and Difficulty
        gbc.gridx = 0; gbc.gridy = 2; mainFormPanel.add(new JLabel("Loại câu hỏi:"), gbc);
        cmbQuestionType = new JComboBox<>(new String[]{"MultipleChoice", "FillInBlank", "Listening", "Reading", "Essay"});
        cmbQuestionType.setFont(hybridFont);
        gbc.gridx = 1; mainFormPanel.add(cmbQuestionType, gbc);

        gbc.gridx = 2; gbc.gridy = 2; mainFormPanel.add(new JLabel("Độ khó:"), gbc);
        cmbDifficulty = new JComboBox<>(new String[]{"N5", "N4", "N3", "N2", "N1", "Other"});
        cmbDifficulty.setFont(hybridFont);
        gbc.gridx = 3; mainFormPanel.add(cmbDifficulty, gbc);

        // Hàng 3: Correct Answer Key
        gbc.gridx = 0; gbc.gridy = 3; mainFormPanel.add(new JLabel("Đáp án chính (Điền/Luận):"), gbc);
        txtCorrectAnswerKey = new JTextArea(2, 20);
        txtCorrectAnswerKey.setFont(hybridFont);
        txtCorrectAnswerKey.setLineWrap(true);
        txtCorrectAnswerKey.setWrapStyleWord(true);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        mainFormPanel.add(new JScrollPane(txtCorrectAnswerKey), gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;

        // Hàng 4: Explanation
        gbc.gridx = 0; gbc.gridy = 4; mainFormPanel.add(new JLabel("Giải thích đáp án:"), gbc);
        txtAnswerExplanation = new JTextArea(3, 30);
        txtAnswerExplanation.setFont(hybridFont);
        txtAnswerExplanation.setLineWrap(true);
        txtAnswerExplanation.setWrapStyleWord(true);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        mainFormPanel.add(new JScrollPane(txtAnswerExplanation), gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;

        // Hàng 5: Audio File
        gbc.gridx = 0; gbc.gridy = 5; mainFormPanel.add(new JLabel("File âm thanh:"), gbc);
        txtAudioFilePath = new JTextField(25);
        txtAudioFilePath.setEditable(false);
        txtAudioFilePath.setFont(hybridFont);
        gbc.gridx = 1; gbc.gridwidth = 2; mainFormPanel.add(txtAudioFilePath, gbc);
        btnBrowseAudio = new JButton("Duyệt...");
        gbc.gridx = 3; gbc.gridwidth = 1; mainFormPanel.add(btnBrowseAudio, gbc);

        // Hàng 6: AI Suggestion
        gbc.gridx = 0; gbc.gridy = 6; mainFormPanel.add(new JLabel("Gợi ý AI:"), gbc);
        txtAiSuggestedAnswer = new JTextArea(2,20);
        txtAiSuggestedAnswer.setFont(hybridFont);
        txtAiSuggestedAnswer.setLineWrap(true);
        txtAiSuggestedAnswer.setWrapStyleWord(true);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        mainFormPanel.add(new JScrollPane(txtAiSuggestedAnswer), gbc);
        btnGetAiSuggestion = new JButton("Lấy gợi ý");
        gbc.gridx = 3; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL; mainFormPanel.add(btnGetAiSuggestion, gbc);

        // Hàng 7: Tags
        gbc.gridx = 0; gbc.gridy = 7; mainFormPanel.add(new JLabel("Tags (cách nhau bởi dấu phẩy):"), gbc);
        txtTags = new JTextField(30);
        txtTags.setFont(hybridFont);
        gbc.gridx = 1; gbc.gridwidth = 3; mainFormPanel.add(txtTags, gbc);

        // Panel quản lý các lựa chọn
        panelOptionsManagement = new JPanel(new BorderLayout(5, 5));
        panelOptionsManagement.setBorder(BorderFactory.createTitledBorder(null, "Quản Lý Lựa Chọn Trắc Nghiệm",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, vietnameseFont));

        JPanel optionInputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints optGbc = new GridBagConstraints();
        optGbc.insets = new Insets(2, 2, 2, 2);
        optGbc.anchor = GridBagConstraints.WEST;

        optGbc.gridx = 0; optGbc.gridy = 0; optionInputPanel.add(new JLabel("Ký tự:"), optGbc);
        txtOptionLetter = new JTextField(3);
        txtOptionLetter.setFont(hybridFont);
        optGbc.gridx = 1; optionInputPanel.add(txtOptionLetter, optGbc);

        optGbc.gridx = 2; optGbc.gridy = 0; optionInputPanel.add(new JLabel("Là đáp án đúng:"), optGbc);
        chkIsCorrectOption = new JCheckBox();
        optGbc.gridx = 3; optionInputPanel.add(chkIsCorrectOption, optGbc);

        optGbc.gridx = 0; optGbc.gridy = 1; optionInputPanel.add(new JLabel("Nội dung lựa chọn:"), optGbc);
        txtOptionTextContent = new JTextArea(2, 25);
        txtOptionTextContent.setFont(hybridFont);
        txtOptionTextContent.setLineWrap(true);
        txtOptionTextContent.setWrapStyleWord(true);
        optGbc.gridx = 1; optGbc.gridy = 1; optGbc.gridwidth = 3; optGbc.fill = GridBagConstraints.HORIZONTAL;
        optionInputPanel.add(new JScrollPane(txtOptionTextContent), optGbc);
        optGbc.gridwidth = 1;

        JPanel optionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnAddOptionToList = new JButton("Thêm Lựa Chọn");
        btnUpdateSelectedOption = new JButton("Cập Nhật L.Chọn");
        btnRemoveSelectedOption = new JButton("Xóa L.Chọn");
        optionButtonPanel.add(btnAddOptionToList);
        optionButtonPanel.add(btnUpdateSelectedOption);
        optionButtonPanel.add(btnRemoveSelectedOption);

        optGbc.gridx = 0; optGbc.gridy = 2; optGbc.gridwidth = 4;
        optionInputPanel.add(optionButtonPanel, optGbc);

        panelOptionsManagement.add(optionInputPanel, BorderLayout.NORTH);

        optionListModel = new DefaultListModel<>();
        listGuiOptions = new JList<>(optionListModel);
        listGuiOptions.setFont(hybridFont);
        listGuiOptions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panelOptionsManagement.add(new JScrollPane(listGuiOptions), BorderLayout.CENTER);
        btnUpdateSelectedOption.setEnabled(false); // Disable ban đầu
        btnRemoveSelectedOption.setEnabled(false); // Disable ban đầu

        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");
        bottomButtonPanel.add(btnSave);
        bottomButtonPanel.add(btnCancel);

        add(mainFormPanel, BorderLayout.NORTH);
        add(panelOptionsManagement, BorderLayout.CENTER);
        add(bottomButtonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        btnSave.addActionListener(e -> handleSaveQuestion());
        btnCancel.addActionListener(e -> dispose());
        btnBrowseAudio.addActionListener(e -> handleBrowseAudio());
        btnGetAiSuggestion.addActionListener(e -> handleGetAiSuggestion());

        cmbQuestionType.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                toggleOptionsPanelVisibility();
            }
        });

        // Listeners cho panel options
        btnAddOptionToList.addActionListener(e -> handleAddOptionToList());
        btnUpdateSelectedOption.addActionListener(e -> handleUpdateSelectedOption());
        btnRemoveSelectedOption.addActionListener(e -> handleRemoveSelectedOption());
        listGuiOptions.addListSelectionListener(e -> handleOptionListSelection(e));
    }

    private void toggleOptionsPanelVisibility() {
        String selectedType = (String) cmbQuestionType.getSelectedItem();
        boolean isMcq = "MultipleChoice".equalsIgnoreCase(selectedType);
        panelOptionsManagement.setVisible(isMcq);
        txtCorrectAnswerKey.setEnabled(!isMcq); // Enable/disable dựa trên type
        txtCorrectAnswerKey.setEditable(!isMcq);
        if (!isMcq) {
            txtCorrectAnswerKey.setBackground(UIManager.getColor("TextField.background"));
        } else {
            txtCorrectAnswerKey.setBackground(UIManager.getColor("TextField.disabledBackground"));
        }
        pack(); // Re-pack dialog để điều chỉnh kích thước nếu panel ẩn/hiện
    }

    private void populateFields() {
        if (currentQuestion == null) return;

        txtQuestionID.setText(String.valueOf(currentQuestion.getQuestionID()));
        txtQuestionText.setText(currentQuestion.getQuestionText());
        cmbQuestionType.setSelectedItem(currentQuestion.getQuestionType());
        cmbDifficulty.setSelectedItem(currentQuestion.getDifficultyLevel());

        if (!"MultipleChoice".equalsIgnoreCase(currentQuestion.getQuestionType())) {
            txtCorrectAnswerKey.setText(currentQuestion.getCorrectAnswerKey());
        } else {
            txtCorrectAnswerKey.setText(""); // Xóa nếu là MCQ
        }

        txtAnswerExplanation.setText(currentQuestion.getAnswerExplanation());
        txtAudioFilePath.setText(currentQuestion.getAudioFilePath());
        txtAiSuggestedAnswer.setText(currentQuestion.getAiSuggestedAnswer());
        txtTags.setText(currentQuestion.getTags());

        toggleOptionsPanelVisibility(); // Quan trọng: gọi sau khi set cmbQuestionType

        if ("MultipleChoice".equalsIgnoreCase(currentQuestion.getQuestionType()) && currentQuestion.getOptions() != null) {
            optionListModel.clear();
            for (QuestionOption opt : currentQuestion.getOptions()) {
                optionListModel.addElement(opt);
            }
        }
    }

    private void handleBrowseAudio() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn File Âm Thanh (MP3, WAV, M4A)");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Audio Files", "mp3", "wav", "m4a"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (audioService != null) {
                try {
                    Path relativePath = audioService.storeAudioFile(selectedFile.toPath());
                    txtAudioFilePath.setText(relativePath.toString());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi lưu file âm thanh: " + ex.getMessage(), "Lỗi Âm Thanh", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else {
                 JOptionPane.showMessageDialog(this, "AudioService chưa được khởi tạo.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleGetAiSuggestion() {
        String questionText = txtQuestionText.getText();
        if (questionText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập nội dung câu hỏi trước.", "Gợi Ý AI", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (aiService != null) {
            String suggestion = aiService.getAnswerSuggestion(questionText, (String) cmbQuestionType.getSelectedItem());
            txtAiSuggestedAnswer.setText(suggestion);
        } else {
            JOptionPane.showMessageDialog(this, "AIService chưa được khởi tạo.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddOptionToList() {
        String letter = txtOptionLetter.getText().trim().toUpperCase();
        String text = txtOptionTextContent.getText().trim();
        boolean isCorrect = chkIsCorrectOption.isSelected();

        if (letter.isEmpty() || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ký tự và nội dung lựa chọn không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Kiểm tra xem ký tự đã tồn tại chưa
        for (int i = 0; i < optionListModel.getSize(); i++) {
            if (optionListModel.getElementAt(i).getOptionLetter().equals(letter)) {
                JOptionPane.showMessageDialog(this, "Ký tự lựa chọn '" + letter + "' đã tồn tại.", "Lỗi Trùng Lặp", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Nếu chọn là đáp án đúng, đảm bảo chỉ có 1 đáp án đúng
        if (isCorrect) {
            for (int i = 0; i < optionListModel.getSize(); i++) {
                optionListModel.getElementAt(i).setCorrect(false); // Bỏ chọn các cái khác
            }
        }


        QuestionOption newOption = new QuestionOption();
        // newOption.setOptionID(); // ID sẽ được gán bởi DB khi lưu, hoặc không cần ID client-side trước khi lưu
        newOption.setOptionLetter(letter);
        newOption.setOptionText(text);
        newOption.setCorrect(isCorrect);

        optionListModel.addElement(newOption);
        listGuiOptions.repaint(); // Cập nhật JList để hiển thị thay đổi isCorrect

        // Xóa trắng các trường nhập liệu option
        txtOptionLetter.setText("");
        txtOptionTextContent.setText("");
        chkIsCorrectOption.setSelected(false);
        txtOptionLetter.requestFocus();
    }

    private void handleUpdateSelectedOption() {
        int selectedIndex = listGuiOptions.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lựa chọn để cập nhật.", "Chưa Chọn Lựa Chọn", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String letter = txtOptionLetter.getText().trim().toUpperCase();
        String text = txtOptionTextContent.getText().trim();
        boolean isCorrect = chkIsCorrectOption.isSelected();

        if (letter.isEmpty() || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ký tự và nội dung lựa chọn không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        QuestionOption selectedOption = optionListModel.getElementAt(selectedIndex);

        // Kiểm tra trùng ký tự (ngoại trừ chính nó)
        for (int i = 0; i < optionListModel.getSize(); i++) {
            if (i != selectedIndex && optionListModel.getElementAt(i).getOptionLetter().equals(letter)) {
                JOptionPane.showMessageDialog(this, "Ký tự lựa chọn '" + letter + "' đã tồn tại.", "Lỗi Trùng Lặp", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Nếu chọn là đáp án đúng, đảm bảo chỉ có 1 đáp án đúng
        if (isCorrect) {
            for (int i = 0; i < optionListModel.getSize(); i++) {
                if (i != selectedIndex) { // Bỏ chọn các cái khác
                     optionListModel.getElementAt(i).setCorrect(false);
                }
            }
        }


        selectedOption.setOptionLetter(letter);
        selectedOption.setOptionText(text);
        selectedOption.setCorrect(isCorrect);

        optionListModel.setElementAt(selectedOption, selectedIndex); // Cập nhật lại model
        listGuiOptions.repaint(); // Cập nhật JList

        // Xóa trắng và disable nút update
        txtOptionLetter.setText("");
        txtOptionTextContent.setText("");
        chkIsCorrectOption.setSelected(false);
        listGuiOptions.clearSelection(); // Bỏ chọn
        btnAddOptionToList.setEnabled(true);
        btnUpdateSelectedOption.setEnabled(false);
    }


    private void handleRemoveSelectedOption() {
        int selectedIndex = listGuiOptions.getSelectedIndex();
        if (selectedIndex != -1) {
            optionListModel.remove(selectedIndex);
            // Xóa trắng các trường nhập nếu đang hiển thị option đã xóa
            txtOptionLetter.setText("");
            txtOptionTextContent.setText("");
            chkIsCorrectOption.setSelected(false);
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một lựa chọn để xóa.", "Chưa Chọn Lựa Chọn", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleOptionListSelection(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) { // Chỉ xử lý khi việc chọn đã ổn định
            QuestionOption selectedOption = listGuiOptions.getSelectedValue();
            if (selectedOption != null) {
                txtOptionLetter.setText(selectedOption.getOptionLetter());
                txtOptionTextContent.setText(selectedOption.getOptionText());
                chkIsCorrectOption.setSelected(selectedOption.isCorrect());
                btnAddOptionToList.setEnabled(false); // Không cho thêm khi đang sửa
                btnUpdateSelectedOption.setEnabled(true);
                btnRemoveSelectedOption.setEnabled(true);
            } else {
                txtOptionLetter.setText("");
                txtOptionTextContent.setText("");
                chkIsCorrectOption.setSelected(false);
                btnAddOptionToList.setEnabled(true);
                btnUpdateSelectedOption.setEnabled(false);
                btnRemoveSelectedOption.setEnabled(false);
            }
        }
    }

    private void handleSaveQuestion() {
        // 1. Validate dữ liệu chung
        String qText = txtQuestionText.getText().trim();
        String qType = (String) cmbQuestionType.getSelectedItem();

        if (qText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nội dung câu hỏi không được để trống.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
            txtQuestionText.requestFocus();
            return;
        }

        Question questionToSave;
        if (currentQuestion == null) { // Thêm mới
            questionToSave = new Question();
        } else { // Sửa
            questionToSave = currentQuestion;
        }

        questionToSave.setQuestionText(qText);
        questionToSave.setQuestionType(qType);
        questionToSave.setDifficultyLevel((String) cmbDifficulty.getSelectedItem());
        questionToSave.setAnswerExplanation(txtAnswerExplanation.getText().trim());
        questionToSave.setAiSuggestedAnswer(txtAiSuggestedAnswer.getText().trim());
        questionToSave.setAudioFilePath(txtAudioFilePath.getText().trim().isEmpty() ? null : txtAudioFilePath.getText().trim());
        questionToSave.setTags(txtTags.getText().trim());

        // 2. Xử lý đáp án và lựa chọn tùy theo loại câu hỏi
        if ("MultipleChoice".equalsIgnoreCase(qType)) {
            if (optionListModel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Câu hỏi trắc nghiệm phải có ít nhất một lựa chọn.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean hasCorrectOption = false;
            List<QuestionOption> optionsFromGui = new ArrayList<>();
            for (int i = 0; i < optionListModel.getSize(); i++) {
                QuestionOption opt = optionListModel.getElementAt(i);
                optionsFromGui.add(opt);
                if (opt.isCorrect()) {
                    hasCorrectOption = true;
                }
            }
            if (!hasCorrectOption) {
                JOptionPane.showMessageDialog(this, "Câu hỏi trắc nghiệm phải có ít nhất một lựa chọn được đánh dấu là đáp án đúng.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }
            questionToSave.setOptions(optionsFromGui);
            questionToSave.setCorrectAnswerKey(null); // Không dùng CorrectAnswerKey cho MCQ trong DB
        } else { // FillInBlank, Essay, Listening, Reading (nếu đáp án là text)
            String correctAnswer = txtCorrectAnswerKey.getText().trim();
            if (correctAnswer.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Đáp án chính không được để trống cho loại câu hỏi này.", "Lỗi Nhập Liệu", JOptionPane.ERROR_MESSAGE);
                txtCorrectAnswerKey.requestFocus();
                return;
            }
            questionToSave.setCorrectAnswerKey(correctAnswer);
            questionToSave.setOptions(new ArrayList<>()); // Xóa options nếu có
        }

        // 3. Gọi DatabaseManager để lưu
        boolean result;
        if (currentQuestion == null) {
            result = dbManager.addQuestion(questionToSave);
        } else {
            result = dbManager.updateQuestion(questionToSave);
        }

        if (result) {
            saved = true;
            JOptionPane.showMessageDialog(this, "Lưu câu hỏi thành công!", "Thành Công", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Đóng dialog
        } else {
            JOptionPane.showMessageDialog(this, "Lưu câu hỏi thất bại. Vui lòng kiểm tra log.", "Lỗi Lưu Trữ", JOptionPane.ERROR_MESSAGE);
        }
    }
}