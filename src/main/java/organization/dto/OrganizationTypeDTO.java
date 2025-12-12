package organization.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import organization.entity.OrganizationType;

@NoArgsConstructor
@Setter
@Getter
public class OrganizationTypeDTO {
    @NotBlank(message = "Type cannot be blank")
    private OrganizationType type;
}
