package organization.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponseDTO {
    private String street;
    private String zipCode;
}
