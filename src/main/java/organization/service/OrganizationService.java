package organization.service;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.NoArgsConstructor;
import organization.dto.*;
import organization.entity.*;
import organization.mapper.OrganizationMapper;
import organization.repository.OrganizationRepository;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@NoArgsConstructor
public class OrganizationService {

    @Inject
    private OrganizationRepository organizationRepository;

    @Transactional
    public OrganizationResponseDTO getOrganizationWithMaxFullName() {
        return OrganizationMapper.toOrganizationResponseDTO(organizationRepository.getOrganizationWithMaxFullName());
    }

    @Transactional
    public long countByPostalAddress(AddressRequestDTO postalAddress) {
        return organizationRepository.countByPostalAddress(OrganizationMapper.toAddress(postalAddress));
    }

    @Transactional
    public long countByTypeLessThan(OrganizationTypeDTO type) {
        return organizationRepository.countByTypeLessThan(OrganizationMapper.toOrganizationType(type));
    }

    @Transactional
    public OrganizationResponseDTO mergeOrganizations(OrganizationMergeRequestDTO dto) {
        Organization org1 = organizationRepository.findById(dto.getOrgId1());
        Organization org2 = organizationRepository.findById(dto.getOrgId2());
        if (org1 == null || org2 == null) {
            throw new IllegalArgumentException("Одна из организаций не найдена");
        }
        org1.setName(dto.getNewName());
        org1.setFullName(dto.getNewName());
        org1.setPostalAddress(dto.getNewAddress());
        org1.setAnnualTurnover(
                (org1.getAnnualTurnover() != null ? org1.getAnnualTurnover() : 0)
                        + (org2.getAnnualTurnover() != null ? org2.getAnnualTurnover() : 0)
        );
        org1.setEmployeesCount(org1.getEmployeesCount() + org2.getEmployeesCount());
        organizationRepository.delete(org2);
        return OrganizationMapper.toOrganizationResponseDTO(org1);
    }

    @Transactional
    public OrganizationResponseDTO absorbOrganization(OrganizationAbsorbRequestDTO dto) {
        Organization absorber = organizationRepository.findById(dto.getOrgId1());
        Organization absorbed = organizationRepository.findById(dto.getOrgId2());
        if (absorber == null || absorbed == null) {
            throw new IllegalArgumentException("Одна из организаций не найдена");
        }

        absorber.setEmployeesCount(absorber.getEmployeesCount() + absorbed.getEmployeesCount());
        absorber.setAnnualTurnover((absorber.getAnnualTurnover() != null ? absorber.getAnnualTurnover() : 0)
                + (absorbed.getAnnualTurnover() != null ? absorbed.getAnnualTurnover() : 0));

        organizationRepository.update(absorber);
        organizationRepository.delete(absorbed);

        Organization updatedAbsorber = organizationRepository.findById(dto.getOrgId1());

        return OrganizationMapper.toOrganizationResponseDTO(updatedAbsorber);
    }

    @Transactional
    public OrganizationResponseDTO getOrganizationById(Long id) {
        return OrganizationMapper.toOrganizationResponseDTO(organizationRepository.findById(id));
    }
    @Transactional
    public List<OrganizationResponseDTO> getAllOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .map(OrganizationMapper::toOrganizationResponseDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public void deleteOrganization(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }
        organizationRepository.deleteById(id);
    }
    @Transactional
    public OrganizationResponseDTO createOrganization(OrganizationRequestDTO organization) {
        Organization org = OrganizationMapper.toOrganization(organization);
        validateOrganization(org);
        organizationRepository.create(org);
        return OrganizationMapper.toOrganizationResponseDTO(org);
    }

    @Transactional
    public void updateOrganization(Long organizationId, OrganizationResponseDTO organizationDTO) {
        Organization organization = OrganizationMapper.toOrganization(organizationDTO);
        validateOrganization(organization);

        organization.setId(organizationId);
        organizationRepository.update(organization);

    }

    private void validateOrganization(Organization organization) {
        if (organization.getName() == null || organization.getName().trim().isEmpty()) {
            throw new ValidationException("Organization name не может быть пустым");
        }
        if (organization.getOfficialAddress() == null) {
            throw new ValidationException("Official address не может быть null");
        }
        if (organization.getAnnualTurnover() <= 0) {
            throw new ValidationException("Annual turnover должен быть положительным");
        }
        if (organization.getEmployeesCount() <= 0) {
            throw new ValidationException("Employees count должно быть положительным");
        }
        if (organization.getRating() <= 0) {
            throw new ValidationException("Rating должке быть положительным");
        }
        if (organization.getType() == null) {
            throw new ValidationException("Organization type не может быть null");
        }
        if (organization.getPostalAddress() == null) {
            throw new ValidationException("Postal address не может быть null");
        }

        validateAddress(organization.getOfficialAddress(), "Official address");
        validateAddress(organization.getPostalAddress(), "Postal address");
        validateCoordinates(organization.getCoordinates());
    }

    private void validateCoordinates(Coordinates coordinates) {
        if (coordinates.getY() <= -461) {
            throw new ValidationException("Y должен быть больше -461");
        }
    }
    private void validateAddress(Address address, String addressType) {
        if (address.getStreet() == null || address.getStreet().trim().isEmpty()) {
            throw new ValidationException(addressType + " street не может быть пустым");
        }
    }


    //для дебага
//    @Transactional
//    public TestEntity createEntity(TestEntity test) {
//        System.out.println("starting service addEntity");
//        return organizationRepository.createTestEntity(test);
//    }

}
