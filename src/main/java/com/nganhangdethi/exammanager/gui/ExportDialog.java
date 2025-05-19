package com.nganhangdethi.exammanager.gui; // Hoặc package của bạn

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExportDialog extends JDialog {
    private JSpinner spnNumQuestions;
    private JCheckBox chkShuffle;
    private JComboBox<String> cmbFormat;
    private JButton btnConfirm;
    private JButton btnCancel;

    private boolean confirmed = false;
//    private final Font hybridFont = new Font("MS Gothic", Font.PLAIN, 14); // Hoặc font bạn dùng
    private final Font vietnameseFont = new Font("Arial", Font.PLAIN, 13);
//    private final Font hybridFont = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font hybridFont = new Font("Yu Gothic UI", Font.PLAIN, 14);

    public ExportDialog(Frame owner) {
        super(owner, "Tùy Chọn Xuất Đề Thi", true); // true = modal dialog
        initComponents();
        addListeners();
        pack(); // Tự động điều chỉnh kích thước dialog
        setLocationRelativeTo(owner); // Hiển thị giữa frame cha
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Hàng 0: Số lượng câu hỏi
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblNumQuestions = new JLabel("Số lượng câu hỏi:");
        lblNumQuestions.setFont(vietnameseFont);
        panel.add(lblNumQuestions, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        // Giới hạn số câu từ 1 đến 500, mặc định 20, bước nhảy 1
        spnNumQuestions = new JSpinner(new SpinnerNumberModel(20, 1, 500, 1));
        spnNumQuestions.setFont(hybridFont);
        panel.add(spnNumQuestions, gbc);

        // Hàng 1: Sáo trộn câu hỏi
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel lblShuffle = new JLabel("Sáo trộn câu hỏi:");
        lblShuffle.setFont(vietnameseFont);
        panel.add(lblShuffle, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        chkShuffle = new JCheckBox("", true); // Mặc định là chọn
        panel.add(chkShuffle, gbc);

        // Hàng 2: Định dạng xuất
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel lblFormat = new JLabel("Định dạng xuất:");
        lblFormat.setFont(vietnameseFont);
        panel.add(lblFormat, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        cmbFormat = new JComboBox<>(new String[]{"PDF", "DOCX"});
        cmbFormat.setFont(hybridFont);
        panel.add(cmbFormat, gbc);

        add(panel, BorderLayout.CENTER);

        // Panel chứa nút Confirm và Cancel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnConfirm = new JButton("Xuất File");
        btnConfirm.setFont(vietnameseFont);
        btnCancel = new JButton("Hủy");
        btnCancel.setFont(vietnameseFont);

        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        btnConfirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = true;
                dispose(); // Đóng dialog
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose(); // Đóng dialog
            }
        });
    }

    // Các phương thức getter để MainFrame lấy thông tin từ dialog
    public boolean isConfirmed() {
        return confirmed;
    }

    public int getNumberOfQuestions() {
        return (int) spnNumQuestions.getValue();
    }

    public boolean isShuffleEnabled() {
        return chkShuffle.isSelected();
    }

    public String getFormat() {
        return (String) cmbFormat.getSelectedItem();
    }

    // (Tùy chọn) Một hàm main để test riêng dialog này nếu cần
    /*
    public static void main(String[] args) {
        // Tạo một JFrame ảo để làm owner
        JFrame testOwner = new JFrame();
        testOwner.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        testOwner.setSize(300,200);
        testOwner.setLocationRelativeTo(null);
        // testOwner.setVisible(true); // Không cần thiết phải hiện owner

        ExportDialog dialog = new ExportDialog(testOwner);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            System.out.println("Confirmed!");
            System.out.println("Number of Questions: " + dialog.getNumberOfQuestions());
            System.out.println("Shuffle Enabled: " + dialog.isShuffleEnabled());
            System.out.println("Format: " + dialog.getFormat());
        } else {
            System.out.println("Cancelled!");
        }
        System.exit(0); // Thoát chương trình test
    }
    */
}