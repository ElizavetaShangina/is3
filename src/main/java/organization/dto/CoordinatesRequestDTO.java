package organization.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import organization.entity.Address;
import organization.entity.Coordinates;
import organization.entity.OrganizationType;

import java.time.ZonedDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoordinatesRequestDTO {

    private double x;

    @NotNull
    @Min(value = -460, message = "Значение должно быть больше -461")
    private Integer y;
}
