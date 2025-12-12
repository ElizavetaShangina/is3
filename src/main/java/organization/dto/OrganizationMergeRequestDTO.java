package organization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import organization.entity.Address;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationMergeRequestDTO {

    private Long orgId1;

    private Long orgId2;

    private String newName;

    private Address newAddress;
}
