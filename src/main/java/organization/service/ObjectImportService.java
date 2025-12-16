package organization.service;

import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import organization.dto.CsvImportModel;
import organization.entity.*;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ObjectImportService {

    // --- ФЛАГИ ДЛЯ ДЕМОНСТРАЦИИ ОТКАЗОВ B и C ---
    // Установить в true для имитации сбоя БД перед commit
    public static boolean SIMULATE_DB_FAILURE = false;
    // Установить в true для имитации сбоя между MinIO upload и DB commit
    public static boolean SIMULATE_MID_LOGIC_FAILURE = false;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private MinioService minioService;

    @Inject
    private EntityManager em;

    public void performImport(byte[] fileContent, User currentUser, String originalFileName) throws Exception {
        String objectName = UUID.randomUUID() + "_" + originalFileName;

        // --- 1. MINIO UPLOAD (Prepare Phase MinIO) ---
        System.out.println("[2PC - Orchestrator] START: MinIO Upload.");
        try {
            minioService.uploadFile(objectName, new ByteArrayInputStream(fileContent), "text/csv");
        } catch (Exception e) {
            // Если MinIO упал, тут ловится exception, файл не был загружен.
            System.err.println("[2PC - Orchestrator] ERROR: MinIO Prepare FAILED. Aborting.");
            throw new RuntimeException("Ошибка загрузки файла в хранилище. БД не затронута. (MinIO-fail)", e);
        }

        // --- ТОЧКА ОТКАЗА C: Ошибка в бизнес-логике сервера (MinIO OK, DB NO-BEGIN) ---
        if (SIMULATE_MID_LOGIC_FAILURE) {
            SIMULATE_MID_LOGIC_FAILURE = false;
            System.err.println("[2PC - Orchestrator] SIMULATION: Mid-logic failure triggered (Ошибка в бизнес-логике).");
            // Искусственный сбой. MinIO-файл уже загружен, его нужно удалить в секции catch.
            throw new RuntimeException("SIMULATION: Mid-logic failure forced.");
        }

        // --- 2. DB TRANSACTION (Prepare Phase DB) ---
        EntityTransaction tx = em.getTransaction();
        Long operationId = null;

        try {
            tx.begin();
            System.out.println("[2PC - Orchestrator] DB: Transaction BEGIN (Prepare Phase).");

            // 2.1. Лог начала
            ImportOperation log = new ImportOperation();
            log.setUser(currentUser);
            log.setStatus(ImportStatus.IN_PROGRESS);
            log.setStartTime(ZonedDateTime.now());
            log.setMinioObjectName(objectName);
            em.persist(log);
            em.flush();
            operationId = log.getId();

            // 2.2. Парсинг и сохранение всех сущностей (если processCsv упадет, то БД откатится)
            int count = processCsv(fileContent);

            // --- ТОЧКА ОТКАЗА B: Отказ БД (перед коммитом) ---
            if (SIMULATE_DB_FAILURE) {
                SIMULATE_DB_FAILURE = false;
                System.err.println("[2PC - Orchestrator] SIMULATION: DB failure triggered (Oтказ БД).");
                // Искусственный сбой. Все операции в БД (log и сущности) будут отменены.
                throw new RuntimeException("SIMULATION: DB failure forced before commit.");
            }

            // 2.3. Обновление лога
            log.setStatus(ImportStatus.SUCCESS);
            log.setAddedObjectsCount(count);
            log.setEndTime(ZonedDateTime.now());
            em.merge(log);

            // --- 3. COMMIT (Commit Phase) ---
            System.out.println("[2PC - Orchestrator] DB: COMMIT attempt (Commit Phase).");
            tx.commit();
            System.out.println("[2PC - Orchestrator] DB: COMMIT SUCCESSFUL. Import Finalized.");

        } catch (Exception e) {
            // --- 4. ROLLBACK / COMPENSATION ---
            System.err.println("[2PC - Orchestrator] Rollback/Compensation Phase: Exception caught: " + e.getMessage());

            if (tx.isActive()) {
                tx.rollback();
                System.err.println("[2PC - Orchestrator] DB: ROLLBACK successful.");
            }

            // Компенсация MinIO: Удаляем файл, так как транзакция БД не прошла!
            minioService.deleteFile(objectName);

            // Запись в лог об ошибке может быть добавлена здесь в новой транзакции,
            // но для атомарности достаточно кинуть ошибку и удалить файл.
            throw new RuntimeException("Ошибка импорта. Файл удален, БД откачена: " + e.getMessage(), e);
        }
    }

    private int processCsv(byte[] content) {
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8)) {
            List<CsvImportModel> importModels = new CsvToBeanBuilder<CsvImportModel>(reader)
                    .withType(CsvImportModel.class)
                    .withSeparator(',')
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            if(importModels.isEmpty()) throw new IllegalArgumentException("Empty file");

            for(CsvImportModel m : importModels) {
                // ТУТ ВАЖНО: Вы должны выполнить маппинг CsvImportModel в Organization и сохранить его
                // ПРИМЕР: organizationRepository.create(OrganizationMapper.toOrganization(m));
                // ИСПОЛЬЗУЙТЕ МЕТОД, КОТОРЫЙ НЕ ОТКРЫВАЕТ НОВУЮ ТРАНЗАКЦИЮ (например, из OrganizationRepository)
                // Иначе вы получите ошибку "Transaction already active" или частичный коммит.
            }
            return importModels.size();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}