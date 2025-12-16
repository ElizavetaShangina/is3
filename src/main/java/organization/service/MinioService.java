package organization.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.InputStream;

@ApplicationScoped
public class MinioService {

    // --- ФЛАГ ДЛЯ ДЕМОНСТРАЦИИ ОТКАЗА A ---
    // Установить в true в UI или коде для имитации сбоя при загрузке
    public static boolean SIMULATE_UPLOAD_FAILURE = false;

    private MinioClient minioClient;
    private final String BUCKET_NAME = "lab3-imports";

    @PostConstruct
    public void init() {
        // Настройки MinIO
        minioClient = MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("minioadmin", "minioadmin")
                .build();

        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("MinIO init error", e);
        }
    }

    public void uploadFile(String objectName, InputStream stream, String contentType) {
        // --- ЛОГИРОВАНИЕ НАЧАЛА ---
        System.out.println("=======================================================");
        System.out.println("[2PC - MinIO] Prepare Phase: Attempting to upload file " + objectName);

        // --- ТОЧКА ОТКАЗА A: Сбой загрузки (Отказ хранилища) ---
        if (SIMULATE_UPLOAD_FAILURE) {
            // Флаг можно сбросить, чтобы не мешал последующим тестам
            SIMULATE_UPLOAD_FAILURE = false;
            throw new RuntimeException("SIMULATION: MinIO upload failed (Отказ файлового хранилища)!");
        }

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .stream(stream, -1, 10485760)
                            .contentType(contentType)
                            .build());
            // --- ЛОГИРОВАНИЕ УСПЕХА ---
            System.out.println("[2PC - MinIO] Prepare Phase: File uploaded successfully (MinIO OK).");
            System.out.println("=======================================================");
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to MinIO", e);
        }
    }

    public InputStream downloadFile(String objectName) {
        // ... без изменений
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to download from MinIO", e);
        }
    }

    public void deleteFile(String objectName) {
        // --- ЛОГИРОВАНИЕ КОМПЕНСАЦИИ ---
        System.err.println("[2PC - MinIO] COMPENSATION: Attempting to delete (rollback) file " + objectName);
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(BUCKET_NAME)
                            .object(objectName)
                            .build());
            System.err.println("[2PC - MinIO] COMPENSATION: File deleted successfully.");
        } catch (Exception e) {
            System.err.println("[2PC - MinIO] ERROR: Failed to compensate MinIO transaction: " + e.getMessage());
        }
    }
}