package organization.service;

import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import organization.dto.CsvImportModel;
import organization.dto.OrganizationRequestDTO;
import organization.entity.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Stateless
public class ObjectImportService {

    @Inject
    private OrganizationService organizationService;

    @Inject
    private ImportHistoryService historyService;
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void performImport(InputStream csvData, User currentUser) throws Exception {

        ImportOperation logEntry = historyService.createLogEntry(currentUser, ImportStatus.IN_PROGRESS);
        int objectsAdded = 0;

        try (Reader reader = new InputStreamReader(csvData, StandardCharsets.UTF_8)) {

            List<CsvImportModel> importModels = new CsvToBeanBuilder<CsvImportModel>(reader)
                    .withType(CsvImportModel.class)
                    .withSeparator(',')
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            if (importModels.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            for (CsvImportModel model : importModels) {
                OrganizationRequestDTO orgDto = mapToOrganizationRequestDTO(model);
                organizationService.createOrganization(orgDto);
                objectsAdded++;
            }

            historyService.updateLogEntry(logEntry.getId(), ImportStatus.SUCCESS, objectsAdded, null);

        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            historyService.updateLogEntry(logEntry.getId(), ImportStatus.FAILURE, 0, msg);

            throw new RuntimeException("Import error: " + msg, e);
        }
    }

    private OrganizationRequestDTO mapToOrganizationRequestDTO(CsvImportModel model) {

        // 1. Координаты
        Coordinates coordinates = new Coordinates();
        if (model.getX() == null || model.getY() == null) {
            throw new ValidationException("Coordinates X and Y can't be empty.");
        }
        coordinates.setX(model.getX());
        coordinates.setY(model.getY());

        // 2. Официальный адрес
        Address officialAddress = new Address();
        if (model.getOfficialStreet() == null) {
            throw new ValidationException("Official address (street) can't be empty.");
        }
        officialAddress.setStreet(model.getOfficialStreet());
        officialAddress.setZipCode(model.getOfficialZipCode());

        // 3. Почтовый адрес
        Address postalAddress = new Address();
        // Если в CSV почтового адреса нет, можно использовать официальный,
        // но судя по твоему файлу - он там есть.
        String pStreet = model.getPostalStreet();
        if (pStreet == null || pStreet.isEmpty()) {
            // Если вдруг пусто, берем официальный (fallback)
            pStreet = model.getOfficialStreet();
        }
        postalAddress.setStreet(pStreet);
        postalAddress.setZipCode(model.getPostalZipCode());

        // 4. Собираем Организацию
        OrganizationRequestDTO dto = new OrganizationRequestDTO();
        dto.setName(model.getName());
        dto.setFullName(model.getFullName());

        if (model.getAnnualTurnover() == null) throw new ValidationException("Annual turnover empty");
        dto.setAnnualTurnover(model.getAnnualTurnover());

        if (model.getEmployeesCount() == null) throw new ValidationException("Employees count empty");
        dto.setEmployeesCount(model.getEmployeesCount());

        if (model.getRating() == null) throw new ValidationException("Rating empty");
        dto.setRating(model.getRating());

        if (model.getType() == null) throw new ValidationException("Type empty");
        dto.setType(model.getType());

        // Устанавливаем вложенные объекты
        dto.setCoordinates(coordinates);
        dto.setOfficialAddress(officialAddress);
        dto.setPostalAddress(postalAddress); // Теперь они разные!

        return dto;
    }
}