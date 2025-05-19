package com.nganhangdethi.exammanager.service; // Hoặc package của bạn

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class AudioService {
    private final Path audioStorageDir; // Thư mục gốc để lưu trữ tất cả file audio

    /**
     * Khởi tạo AudioService với đường dẫn đến thư mục lưu trữ file audio.
     * Thư mục này sẽ được tạo nếu chưa tồn tại.
     * @param storagePathString Đường dẫn dạng String đến thư mục lưu trữ (ví dụ: "audio_files")
     */
    public AudioService(String storagePathString) {
        this.audioStorageDir = Paths.get(storagePathString).toAbsolutePath(); // Lấy đường dẫn tuyệt đối
        try {
            if (!Files.exists(audioStorageDir)) {
                Files.createDirectories(audioStorageDir);
                System.out.println("Thư mục lưu trữ âm thanh đã được tạo tại: " + audioStorageDir);
            } else {
                System.out.println("Sử dụng thư mục lưu trữ âm thanh tại: " + audioStorageDir);
            }
        } catch (IOException e) {
            System.err.println("Không thể khởi tạo hoặc tạo thư mục lưu trữ âm thanh: " + storagePathString);
            e.printStackTrace();
            // Trong ứng dụng thực tế, có thể ném ra một RuntimeException ở đây để dừng ứng dụng nếu không thể lưu audio
        }
    }

    /**
     * Lưu một file âm thanh từ đường dẫn nguồn vào thư mục lưu trữ của ứng dụng.
     * File sẽ được đổi tên thành một UUID duy nhất để tránh trùng lặp, nhưng giữ lại phần mở rộng.
     * @param sourcePath Đường dẫn đến file âm thanh nguồn.
     * @return Đường dẫn tương đối của file đã lưu trong thư mục audioStorageDir (chỉ tên file mới).
     * @throws IOException Nếu có lỗi trong quá trình copy file.
     */
    public Path storeAudioFile(Path sourcePath) throws IOException {
        if (!Files.exists(audioStorageDir)) {
             throw new IOException("Thư mục lưu trữ âm thanh không tồn tại: " + audioStorageDir);
        }
        if (sourcePath == null || !Files.exists(sourcePath) || !Files.isRegularFile(sourcePath)) {
            throw new IOException("Đường dẫn file nguồn không hợp lệ hoặc không tồn tại.");
        }

        String originalFileName = sourcePath.getFileName().toString();
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
            extension = originalFileName.substring(dotIndex); // Bao gồm cả dấu chấm, ví dụ ".mp3"
        }

        // Tạo tên file duy nhất
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        Path targetPath = this.audioStorageDir.resolve(uniqueFileName);

        // Copy file
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Đã lưu file âm thanh: " + sourcePath + " -> " + targetPath);

        // Trả về chỉ tên file (đường dẫn tương đối so với audioStorageDir)
        return Paths.get(uniqueFileName);
    }

    /**
     * Lấy đối tượng File của một file âm thanh dựa trên đường dẫn tương đối (tên file)
     * đã được lưu trong thư mục audioStorageDir.
     * @param relativePath Tên file âm thanh (đường dẫn tương đối so với thư mục lưu trữ).
     * @return Đối tượng File nếu tìm thấy, ngược lại trả về null.
     */
    public File getAudioFile(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }
        Path filePath = audioStorageDir.resolve(relativePath);
        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
            return filePath.toFile();
        }
        System.err.println("Không tìm thấy file âm thanh: " + filePath);
        return null;
    }

    /**
     * Lấy đường dẫn tuyệt đối đến thư mục lưu trữ âm thanh.
     * @return Path đến thư mục lưu trữ.
     */
    public Path getAudioStorageDirectory() {
        return audioStorageDir;
    }
}