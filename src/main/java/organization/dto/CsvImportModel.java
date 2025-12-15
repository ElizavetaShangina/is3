package organization.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import organization.entity.OrganizationType;

@Data
public class CsvImportModel {

    @CsvBindByName(column = "name", required = true)
    private String name;

    @CsvBindByName(column = "fullName")
    private String fullName;

    @CsvBindByName(column = "type", required = true)
    private OrganizationType type;

    @CsvBindByName(column = "annualTurnover")
    private Double annualTurnover;

    @CsvBindByName(column = "employeesCount")
    private Integer employeesCount;

    @CsvBindByName(column = "rating")
    private Float rating;

    @CsvBindByName(column = "coordinates.x", required = true)
    private Double x;

    @CsvBindByName(column = "coordinates.y", required = true)
    private Integer y;


    @CsvBindByName(column = "officialAddress.street", required = true)
    private String officialStreet;

    @CsvBindByName(column = "officialAddress.zipCode")
    private String officialZipCode;


    @CsvBindByName(column = "postalAddress.street", required = true)
    private String postalStreet;

    @CsvBindByName(column = "postalAddress.zipCode")
    private String postalZipCode;
}