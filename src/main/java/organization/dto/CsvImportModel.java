package organization.dto;

import com.opencsv.bean.CsvBindByName;
import organization.entity.OrganizationType;
import lombok.Getter; // Добавил, предполагая, что вы используете Lombok
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter // <-- Добавляем Lombok геттеры/сеттеры для OpenCSV
@Setter
@NoArgsConstructor // Для OpenCSV
public class CsvImportModel {

    // Поля Organization
    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "fullName")
    private String fullName;

    // AnnualTurnover: в Organization Double, в CSV будем парсить как Double
    @CsvBindByName(column = "annualTurnover")
    private Double annualTurnover;

    // EmployeesCount: в Organization int, в CSV будем парсить как Integer
    @CsvBindByName(column = "employeesCount")
    private Integer employeesCount;

    // Rating: в Organization Float, в CSV будем парсить как Float
    @CsvBindByName(column = "rating")
    private Float rating;

    @CsvBindByName(column = "type")
    private OrganizationType type;

    // Поля Coordinates (вложенный объект)
    // X: в Coordinates double, в CSV будем парсить как Double
    @CsvBindByName(column = "x")
    private Double x;

    // Y: в Coordinates Integer, в CSV будем парсить как Integer
    @CsvBindByName(column = "y")
    private Integer y;

    // Поля Address (Official/Postal) - вложенный объект
    @CsvBindByName(column = "street")
    private String street;

    @CsvBindByName(column = "zipCode")
    private String zipCode;

    // Поле 'town' УДАЛЕНО, так как отсутствует в Address
}