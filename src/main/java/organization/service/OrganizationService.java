package organization.service;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ValidationException;
import lombok.NoArgsConstructor;
import organization.dto.*;
import organization.entity.*;
import organization.exception.UniqueConstraintViolationException;
import organization.mapper.OrganizationMapper;
import organization.repository.OrganizationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Stateless
@NoArgsConstructor
public class OrganizationService {

    @Inject
    private OrganizationRepository organizationRepository;

    @PersistenceContext
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public OrganizationResponseDTO createOrganization(OrganizationRequestDTO organizationDTO) {
        Organization org = OrganizationMapper.toOrganization(organizationDTO);

        // 1. Проверка уникальности (бизнес-логика)
        checkProgrammaticUniqueness(org.getName(), org.getType());

        // 2. Валидация полей
        validateOrganization(org);

        // 3. Сохранение (транзакция управляется контейнером)
        organizationRepository.create(org);

        return OrganizationMapper.toOrganizationResponseDTO(org);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateOrganization(Long organizationId, OrganizationResponseDTO organizationDTO) {
        Organization organization = OrganizationMapper.toOrganization(organizationDTO);
        validateOrganization(organization);
        organization.setId(organizationId);
        organizationRepository.update(organization);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public OrganizationResponseDTO getOrganizationWithMaxFullName() {
        return OrganizationMapper.toOrganizationResponseDTO(organizationRepository.getOrganizationWithMaxFullName());
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public long countByPostalAddress(AddressRequestDTO postalAddress) {
        return organizationRepository.countByPostalAddress(OrganizationMapper.toAddress(postalAddress));
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public long countByTypeLessThan(OrganizationTypeDTO type) {
        return organizationRepository.countByTypeLessThan(OrganizationMapper.toOrganizationType(type));
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public OrganizationResponseDTO mergeOrganizations(OrganizationMergeRequestDTO dto) {
        Organization org1 = organizationRepository.findById(dto.getOrgId1());
        Organization org2 = organizationRepository.findById(dto.getOrgId2());
        if (org1 == null || org2 == null) {
            throw new IllegalArgumentException("One of the organizations was not found");
        }
        org1.setName(dto.getNewName());
        org1.setFullName(dto.getNewName());
        org1.setPostalAddress(dto.getNewAddress());
        org1.setAnnualTurnover(
                (org1.getAnnualTurnover() != null ? org1.getAnnualTurnover() : 0)
                        + (org2.getAnnualTurnover() != null ? org2.getAnnualTurnover() : 0)
        );
        org1.setEmployeesCount(org1.getEmployeesCount() + org2.getEmployeesCount());

        organizationRepository.update(org1); // Явный update для надежности
        organizationRepository.delete(org2);
        return OrganizationMapper.toOrganizationResponseDTO(org1);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public OrganizationResponseDTO absorbOrganization(OrganizationAbsorbRequestDTO dto) {
        Organization absorber = organizationRepository.findById(dto.getOrgId1());
        Organization absorbed = organizationRepository.findById(dto.getOrgId2());
        if (absorber == null || absorbed == null) {
            throw new IllegalArgumentException("One of the organizations was not found");
        }

        absorber.setEmployeesCount(absorber.getEmployeesCount() + absorbed.getEmployeesCount());
        absorber.setAnnualTurnover((absorber.getAnnualTurnover() != null ? absorber.getAnnualTurnover() : 0)
                + (absorbed.getAnnualTurnover() != null ? absorbed.getAnnualTurnover() : 0));

        organizationRepository.update(absorber);
        organizationRepository.delete(absorbed);

        return OrganizationMapper.toOrganizationResponseDTO(absorber);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public OrganizationResponseDTO getOrganizationById(Long id) {
        return OrganizationMapper.toOrganizationResponseDTO(organizationRepository.findById(id));
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public List<OrganizationResponseDTO> getAllOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .map(OrganizationMapper::toOrganizationResponseDTO)
                .collect(Collectors.toList());
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteOrganization(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID can't be null");
        }
        organizationRepository.deleteById(id);
    }


    private void checkProgrammaticUniqueness(String title, OrganizationType type) {
        try {
            Long count = em.createQuery("SELECT count(o) FROM Organization o WHERE o.name = :name AND o.type = :type", Long.class)
                    .setParameter("name", title)
                    .setParameter("type", type)
                    .getSingleResult();

            if (count > 0) {
                throw new UniqueConstraintViolationException(
                        "Organization with Name='" + title + "' and Organization Type='" + type + "' already exists."
                );
            }
        } catch (NoResultException e) {
            // OK
        }
    }

    private void validateOrganization(Organization organization) {
        if (organization.getName() == null || organization.getName().trim().isEmpty()) {
            throw new ValidationException("Organization name can't be empty");
        }
        if (organization.getOfficialAddress() == null) {
            throw new ValidationException("Official address can't be null");
        }
        if (organization.getAnnualTurnover() <= 0) {
            throw new ValidationException("Annual turnover must be positive");
        }
        if (organization.getEmployeesCount() <= 0) {
            throw new ValidationException("Employees count must be positive");
        }
        if (organization.getRating() <= 0) {
            throw new ValidationException("Rating must be positive");
        }
        if (organization.getType() == null) {
            throw new ValidationException("Organization type can't be null");
        }
        if (organization.getPostalAddress() == null) {
            throw new ValidationException("Postal address can't be null");
        }

        validateAddress(organization.getOfficialAddress(), "Official address");
        validateAddress(organization.getPostalAddress(), "Postal address");
        validateCoordinates(organization.getCoordinates());
    }

    private void validateCoordinates(Coordinates coordinates) {
        if (coordinates.getY() <= -461) {
            throw new ValidationException("Y must be more than -461");
        }
    }

    private void validateAddress(Address address, String addressType) {
        if (address.getStreet() == null || address.getStreet().trim().isEmpty()) {
            throw new ValidationException(addressType + " street can't be empty");
        }
    }
}