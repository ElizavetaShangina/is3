package organization.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CoordinatesResponseDTO {
    private double x;
    private Integer y;
}

