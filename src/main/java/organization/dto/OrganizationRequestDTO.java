package organization.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import organization.entity.Address;
import organization.entity.Coordinates;
import organization.entity.OrganizationType;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationRequestDTO {

    @NotBlank
    private String name;

    @NotNull
    private Coordinates coordinates;

    @NotNull
    private Address officialAddress;

    @Positive(message = "Значение поля должно быть больше 0")
    private Double annualTurnover;


    @Positive(message = "Значение поля должно быть больше 0")
    private int employeesCount;


    @Positive(message = "Значение поля должно быть больше 0")
    private Float rating;


    @NotBlank
    @Size(max = 1334, message = "Длина строки не должна превышать 1334 символа")
    private String fullName;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OrganizationType type;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private Address postalAddress;

}
