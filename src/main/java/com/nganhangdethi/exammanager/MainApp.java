package com.nganhangdethi.exammanager;

import java.awt.Font;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.nganhangdethi.exammanager.db.DatabaseManager;
import com.nganhangdethi.exammanager.gui.MainFrame;

public class MainApp { // Hoặc MainFrame
	public static void main(String[] args) { // Hoặc trong constructor/init method của GUI
		DatabaseManager dbManager = new DatabaseManager();
		System.out.println("Đang kiểm tra kết nối cơ sở dữ liệu...");
		if (dbManager.testConnection()) {
			System.out.println("Kết nối CSDL thành công từ DatabaseManager!");
			// Tiếp tục khởi tạo GUI hoặc các thành phần khác của ứng dụng
			// SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
		} else {
			System.err.println("Không thể kết nối tới cơ sở dữ liệu. Vui lòng kiểm tra cấu hình.");
			// Có thể hiển thị một JOptionPane thông báo lỗi cho người dùng
			// JOptionPane.showMessageDialog(null, "Không thể kết nối CSDL. Kiểm tra log.",
			// "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
			// Và có thể thoát ứng dụng hoặc vô hiệu hóa các chức năng liên quan đến DB
			// System.exit(1);

		}

		try {
			// Set Look and Feel cho giao diện đồng nhất hơn với hệ điều hành
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// Đặt font mặc định cho các component Swing
//			Font defaultFont = new Font("Segoe UI", Font.PLAIN, 14); // Hoặc Arial, Tahoma
			Font defaultFont = new Font("Yu Gothic UI", Font.PLAIN, 14);


			// Lấy danh sách các key UI mặc định
			java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
			while (keys.hasMoreElements()) {
				Object key = keys.nextElement();
				Object value = UIManager.get(key);
				if (value instanceof javax.swing.plaf.FontUIResource) {
					UIManager.put(key, new javax.swing.plaf.FontUIResource(defaultFont));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				new MainFrame().setVisible(true);
//			}
//		});
		SwingUtilities.invokeLater(() -> {
	        MainFrame frame = new MainFrame();
	        frame.setVisible(true);
	    });
	}
}
