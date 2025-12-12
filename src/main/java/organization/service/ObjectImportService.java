package organization.service;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ValidationException;
import organization.dto.CsvImportModel; // <--- Не забудьте импортировать
import organization.dto.OrganizationRequestDTO;
import organization.entity.*;
import organization.exception.UniqueConstraintViolationException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.ZonedDateTime;
import java.util.List;

@Stateless
public class ObjectImportService {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private ObjectImportService self;

    @Inject
    private OrganizationService organizationService;

    // ----------------------------------------------------------------------
    // ОСНОВНОЙ МЕТОД ИМПОРТА: REQUIRED (Одна транзакция - Все или Ничего)
    // ----------------------------------------------------------------------
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void performImport(InputStream csvData, User currentUser) throws Exception {

        ImportOperation logEntry = self.createLogEntry(currentUser, ImportStatus.IN_PROGRESS);
        int objectsAdded = 0;

        try (Reader reader = new InputStreamReader(csvData)) {

            // 2. Реальный парсинг CSV с OpenCSV
            List<CsvImportModel> importModels = new CsvToBeanBuilder<CsvImportModel>(reader)
                    .withType(CsvImportModel.class)
                    .withSeparator(',')
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            if (importModels.isEmpty()) {
                throw new IllegalArgumentException("Импортируемый файл не содержит данных.");
            }

            // 3. Итерация и создание объектов
            for (CsvImportModel model : importModels) {
                // Преобразование CSV-модели в DTO
                OrganizationRequestDTO orgDto = mapToOrganizationRequestDTO(model);

                // Вызываем метод создания, который включает все проверки
                organizationService.createOrganization(orgDto);

                objectsAdded++;
            }

            self.updateLogEntry(logEntry, ImportStatus.SUCCESS, objectsAdded, null);

        } catch (ValidationException | UniqueConstraintViolationException e) {
            String errorMessage = "Ошибка импорта: " + e.getMessage();
            self.updateLogEntry(logEntry, ImportStatus.FAILURE, 0, errorMessage);
            throw e;
        } catch (Exception e) {
            String errorMessage = "Критическая ошибка импорта: " + e.getMessage();
            self.updateLogEntry(logEntry, ImportStatus.FAILURE, 0, errorMessage);
            throw e;
        }
    }

    /**
     * Преобразование CsvImportModel в OrganizationRequestDTO.
     * Реализует логику вложенных объектов (Coordinates и Address).
     */
    private OrganizationRequestDTO mapToOrganizationRequestDTO(CsvImportModel model) {

        // 1. Создание Coordinates (Coordinates.x: double, Coordinates.y: Integer)
        Coordinates coordinates = new Coordinates();

        // OpenCSV парсит в Double/Integer, как в CsvImportModel, но в Coordinates.x - double.
        // Обеспечиваем корректное преобразование.
        if (model.getX() == null || model.getY() == null) {
            throw new ValidationException("Координаты X и Y не могут быть пустыми.");
        }
        coordinates.setX(model.getX());
        coordinates.setY(model.getY());

        // 2. Создание Address (Address.street: String, Address.zipCode: String)
        Address address = new Address();
        if (model.getStreet() == null || model.getZipCode() == null) {
            throw new ValidationException("Адрес (street, zipCode) не может быть пустым.");
        }
        address.setStreet(model.getStreet());
        address.setZipCode(model.getZipCode());

        // 3. Создание OrganizationRequestDTO
        OrganizationRequestDTO dto = new OrganizationRequestDTO();
        dto.setName(model.getName());
        dto.setFullName(model.getFullName());

        // AnnualTurnover: Double -> Double
        if (model.getAnnualTurnover() == null) {
            throw new ValidationException("Annual turnover не может быть пустым.");
        }
        dto.setAnnualTurnover(model.getAnnualTurnover());

        // EmployeesCount: Integer -> int (может быть null в CSV, но в Organization int)
        if (model.getEmployeesCount() == null) {
            throw new ValidationException("Employees count не может быть пустым.");
        }
        dto.setEmployeesCount(model.getEmployeesCount());

        // Rating: Float -> Float
        if (model.getRating() == null) {
            throw new ValidationException("Rating не может быть пустым.");
        }
        dto.setRating(model.getRating());

        if (model.getType() == null) {
            throw new ValidationException("Organization type не может быть пустым.");
        }
        dto.setType(model.getType()); // OrganizationTypeDTO или OrganizationType

        dto.setCoordinates(coordinates);
        dto.setOfficialAddress(address);
        dto.setPostalAddress(address); // Используем один и тот же адрес для официального и почтового

        return dto;
    }

    // ----------------------------------------------------------------------
    // МЕТОДЫ ЖУРНАЛИРОВАНИЯ: REQUIRES_NEW (Всегда новая транзакция)
    // КОД НЕ ИЗМЕНЕН, кроме импортов
    // ----------------------------------------------------------------------

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public ImportOperation createLogEntry(User user, ImportStatus status) {
        ImportOperation logEntry = new ImportOperation();
        User managedUser = em.find(User.class, user.getId());
        if (managedUser == null) {
            throw new RuntimeException("Пользователь не найден при создании лога.");
        }

        logEntry.setUser(managedUser);
        logEntry.setStatus(status);
        logEntry.setStartTime(ZonedDateTime.now());

        em.persist(logEntry);
        em.flush();
        return logEntry;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateLogEntry(ImportOperation logEntry, ImportStatus status, Integer count, String errorMessage) {
        ImportOperation managedLog = em.find(ImportOperation.class, logEntry.getId());

        if (managedLog != null) {
            managedLog.setStatus(status);
            managedLog.setEndTime(ZonedDateTime.now());
            managedLog.setAddedObjectsCount(count);
            managedLog.setErrorMessage(errorMessage != null && errorMessage.length() > 4000 ?
                    errorMessage.substring(0, 4000) : errorMessage);
            em.merge(managedLog);
        }
    }
}