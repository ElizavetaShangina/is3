package organization.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import organization.entity.Address;
import organization.entity.Coordinates;
import organization.entity.OrganizationType;

import java.time.ZonedDateTime;

@Data
@Builder
public class OrganizationResponseDTO {

    private long id;
    private String name;
    private CoordinatesResponseDTO coordinates;
    private ZonedDateTime creationDate;
    private AddressResponseDTO officialAddress;
    private Double annualTurnover;
    private int employeesCount;
    private Float rating;
    private String fullName;
    private OrganizationType type;
    private AddressResponseDTO postalAddress;

}
