package organization.service;

import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.enterprise.context.ApplicationScoped; // EJB @Stateless не дружит с RESOURCE_LOCAL так просто
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.validation.ValidationException;
import organization.dto.CsvImportModel;
import organization.dto.OrganizationRequestDTO;
import organization.entity.*;
import organization.repository.ImportOperationRepository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ObjectImportService {

    @Inject
    private OrganizationService organizationService; // Важно: сервис тоже должен быть ApplicationScoped

    @Inject
    private MinioService minioService;

    @Inject
    private EntityManager em; // Инжектим наш кастомный EM

    public void performImport(byte[] fileContent, User currentUser, String originalFileName) throws Exception {
        String objectName = UUID.randomUUID() + "_" + originalFileName;

        // --- ШАГ 1: Загрузка в MinIO (Вне транзакции БД) ---
        try {
            minioService.uploadFile(objectName, new ByteArrayInputStream(fileContent), "text/csv");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки файла в хранилище. БД не затронута.", e);
        }

        // --- ШАГ 2: Транзакция БД ---
        EntityTransaction tx = em.getTransaction();
        Long operationId = null;

        try {
            tx.begin();

            // 1. Лог начала
            ImportOperation log = new ImportOperation();
            log.setUser(currentUser);
            log.setStatus(ImportStatus.IN_PROGRESS);
            log.setStartTime(ZonedDateTime.now());
            log.setMinioObjectName(objectName); // Связываем файл с БД
            em.persist(log);
            em.flush(); // Получаем ID
            operationId = log.getId();

            // 2. Парсинг
            int count = processCsv(fileContent);

            // 3. Обновление лога
            log.setStatus(ImportStatus.SUCCESS);
            log.setAddedObjectsCount(count);
            log.setEndTime(ZonedDateTime.now());
            em.merge(log);

            tx.commit(); // КОММИТ БД

        } catch (Exception e) {
            // --- ШАГ 3: Компенсация (Откат) ---
            if (tx.isActive()) {
                tx.rollback();
            }

            // Если БД откатилась -> Удаляем файл из MinIO
            minioService.deleteFile(objectName);

            // Пытаемся записать лог об ошибке (в новой транзакции, если нужно, или просто кидаем ошибку)
            // В задании сказано: "не был создан ни один объект". Удаление файла обеспечивает чистоту.
            throw new RuntimeException("Ошибка импорта. Файл удален, БД откачена: " + e.getMessage(), e);
        }
    }

    private int processCsv(byte[] content) {
        // Логика парсинга такая же, только em.persist вызывается внутри текущей транзакции
        // Нужно убедиться, что OrganizationService не открывает новую транзакцию, а использует текущую
        // Для этого OrganizationRepository должен использовать тот же @Inject EntityManager
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8)) {
            List<CsvImportModel> importModels = new CsvToBeanBuilder<CsvImportModel>(reader)
                    .withType(CsvImportModel.class)
                    .withSeparator(',')
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            if(importModels.isEmpty()) throw new IllegalArgumentException("Empty file");

            for(CsvImportModel m : importModels) {
                // Тут маппинг и сохранение
                // organizationService.createOrganization(dto);
                // ВАЖНО: В createOrganization нужно убрать открытие транзакции, так как мы уже в ней!
            }
            return importModels.size();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}