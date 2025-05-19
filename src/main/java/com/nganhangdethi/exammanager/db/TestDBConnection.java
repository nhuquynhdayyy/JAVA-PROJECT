package com.nganhangdethi.exammanager.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDBConnection {

    // Sử dụng chuỗi kết nối bạn đang dùng trong ứng dụng
    // Ví dụ cho Windows Authentication:
    private static final String DB_URL_WINDOWS_AUTH = "jdbc:sqlserver://localhost:1433;databaseName=JapaneseExamBankDB;encrypt=true;trustServerCertificate=true;integratedSecurity=true;";

    // Ví dụ cho SQL Server Authentication:
    // private static final String DB_URL_SQL_AUTH = "jdbc:sqlserver://localhost:1433;databaseName=JapaneseExamBankDB;encrypt=true;trustServerCertificate=true;integratedSecurity=false;";
    // private static final String USER = "your_sql_username";
    // private static final String PASS = "your_sql_password";

    public static void main(String[] args) {
        Connection connection = null;
        try {
            // Load the SQL Server JDBC driver (cần thiết cho các phiên bản Java cũ hơn,
            // nhưng với JDBC 4.0+ thì không bắt buộc nếu driver nằm trong classpath)
            // Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

            System.out.println("Đang kết nối tới cơ sở dữ liệu...");

            // Chọn phương thức kết nối phù hợp:
            // --- Cho Windows Authentication ---
            connection = DriverManager.getConnection(DB_URL_WINDOWS_AUTH);

            // --- Cho SQL Server Authentication ---
            // connection = DriverManager.getConnection(DB_URL_SQL_AUTH, USER, PASS);

            if (connection != null) {
                System.out.println("Kết nối tới cơ sở dữ liệu THÀNH CÔNG!");
                System.out.println("Thông tin kết nối:");
                System.out.println("  URL: " + connection.getMetaData().getURL());
                System.out.println("  User Name: " + connection.getMetaData().getUserName()); // Sẽ khác nhau tùy loại auth
                System.out.println("  Driver Name: " + connection.getMetaData().getDriverName());
                System.out.println("  Driver Version: " + connection.getMetaData().getDriverVersion());
                System.out.println("  Database Product Name: " + connection.getMetaData().getDatabaseProductName());
                System.out.println("  Database Product Version: " + connection.getMetaData().getDatabaseProductVersion());
            } else {
                System.out.println("Kết nối tới cơ sở dữ liệu THẤT BẠI!");
            }

        } catch (SQLException e) {
            System.err.println("LỖI KẾT NỐI SQL: ");
            e.printStackTrace(); // In ra chi tiết lỗi SQL
        } /*catch (ClassNotFoundException e) {
            System.err.println("LỖI: Không tìm thấy JDBC Driver!");
            e.printStackTrace();
        } */finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Đã đóng kết nối.");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}