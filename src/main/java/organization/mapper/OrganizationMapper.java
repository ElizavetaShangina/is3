package organization.mapper;

import organization.dto.*;
import organization.entity.Address;
import organization.entity.Coordinates;
import organization.entity.Organization;
import organization.entity.OrganizationType;

public class OrganizationMapper {
    public static OrganizationResponseDTO toOrganizationResponseDTO(Organization organization) {
        if (organization == null) return null;

        return OrganizationResponseDTO.builder()
                .id(organization.getId())
                .name(organization.getName())
                .coordinates(OrganizationMapper.toCoordinatesResponseDTO(organization.getCoordinates()))
                .creationDate(organization.getCreationDate())
                .officialAddress(OrganizationMapper.toAddressResponseDTO(organization.getOfficialAddress()))
                .annualTurnover(organization.getAnnualTurnover())
                .employeesCount(organization.getEmployeesCount())
                .rating(organization.getRating())
                .fullName(organization.getFullName())
                .type(organization.getType())
                .postalAddress(OrganizationMapper.toAddressResponseDTO(organization.getPostalAddress()))
                .build();
    }

    public static Organization toOrganization(OrganizationResponseDTO organizationResponseDTO) {
        if (organizationResponseDTO == null) return null;

        Organization organization = new Organization();
        organization.setId(organizationResponseDTO.getId());
        organization.setName(organizationResponseDTO.getName());
        organization.setCoordinates(OrganizationMapper.toCoordinates(organizationResponseDTO.getCoordinates()));
        organization.setCreationDate(organizationResponseDTO.getCreationDate());
        organization.setOfficialAddress(OrganizationMapper.toAddress(organizationResponseDTO.getOfficialAddress()));
        organization.setAnnualTurnover(organizationResponseDTO.getAnnualTurnover());
        organization.setEmployeesCount(organizationResponseDTO.getEmployeesCount());
        organization.setRating(organizationResponseDTO.getRating());
        organization.setFullName(organizationResponseDTO.getFullName());
        organization.setType(organizationResponseDTO.getType());
        organization.setPostalAddress(OrganizationMapper.toAddress(organizationResponseDTO.getPostalAddress()));
        return organization;
    }

    public static Organization toOrganization(OrganizationRequestDTO organizationRequestDTO) {
        if (organizationRequestDTO == null) return null;
        Organization organization = new Organization();
        organization.setName(organizationRequestDTO.getName());
        organization.setCoordinates(organizationRequestDTO.getCoordinates());
        organization.setOfficialAddress(organizationRequestDTO.getOfficialAddress());
        organization.setAnnualTurnover(organizationRequestDTO.getAnnualTurnover());
        organization.setEmployeesCount(organizationRequestDTO.getEmployeesCount());
        organization.setRating(organizationRequestDTO.getRating());
        organization.setFullName(organizationRequestDTO.getFullName());
        organization.setType(organizationRequestDTO.getType());
        organization.setPostalAddress(organizationRequestDTO.getPostalAddress());
        return organization;
    }

    public static Address toAddress(AddressRequestDTO postalAddress) {
        if (postalAddress == null) return null;
        Address address = new Address();
        address.setStreet(postalAddress.getStreet());
        address.setZipCode(postalAddress.getZipCode());
        return address;

    }

    public static Address toAddress(AddressResponseDTO postalAddress) {
        if (postalAddress == null) return null;
        Address address = new Address();
        address.setStreet(postalAddress.getStreet());
        address.setZipCode(postalAddress.getZipCode());
        return address;
    }

    public static Coordinates toCoordinates(CoordinatesRequestDTO coordinatesRequestDTO) {
        if (coordinatesRequestDTO == null) return null;
        Coordinates coordinates = new Coordinates();
        coordinates.setX(coordinatesRequestDTO.getX());
        coordinates.setY(coordinatesRequestDTO.getY());
        return coordinates;
    }

    public static Coordinates toCoordinates(CoordinatesResponseDTO coordinatesResponseDTO) {
        if (coordinatesResponseDTO == null) return null;
        Coordinates coordinates = new Coordinates();
        coordinates.setX(coordinatesResponseDTO.getX());
        coordinates.setY(coordinatesResponseDTO.getY());
        return coordinates;
    }

    public static CoordinatesResponseDTO toCoordinatesResponseDTO(Coordinates coordinates) {
        if (coordinates == null) return null;
        return CoordinatesResponseDTO.builder()
                .x(coordinates.getX())
                .y(coordinates.getY())
                .build();
    }

    public static AddressResponseDTO toAddressResponseDTO(Address address) {
        if (address == null) return null;
        return AddressResponseDTO.builder()
                .street(address.getStreet())
                .zipCode(address.getZipCode())
                .build();
    }

    public static OrganizationType toOrganizationType(OrganizationTypeDTO organizationTypeDTO) {
        return organizationTypeDTO == null ? null : organizationTypeDTO.getType();
    }
}
