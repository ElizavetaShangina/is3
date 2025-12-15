package organization.view;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import organization.dto.*;
import organization.entity.Address;
import organization.entity.OrganizationType;
import organization.service.OrganizationService;

import java.io.Serializable;

@Named
@RequestScoped
public class SpecialOperationsView implements Serializable {

    @Inject
    private OrganizationService organizationService;

    // Результаты
    private OrganizationResponseDTO maxFullNameOrg;
    private Long countResult;
    private OrganizationResponseDTO mergeResult;
    private OrganizationResponseDTO absorbResult;

    private String postalStreet;
    private String postalZipCode;
    private OrganizationType selectedType;

    private Long mergeOrgId1;
    private Long mergeOrgId2;
    private String mergeNewName;
    private String mergeNewStreet;
    private String mergeNewZipCode;

    private Long absorbOrgId1;
    private Long absorbOrgId2;

    public void findMaxFullName() {
        maxFullNameOrg = organizationService.getOrganizationWithMaxFullName();
    }

    public void countByPostalAddress() {
        AddressRequestDTO address = new AddressRequestDTO();
        address.setStreet(postalStreet);
        address.setZipCode(postalZipCode); // может быть null
        countResult = organizationService.countByPostalAddress(address);
    }

    public void countByTypeLessThan() {
        OrganizationTypeDTO dto = new OrganizationTypeDTO();
        dto.setType(selectedType);
        countResult = organizationService.countByTypeLessThan(dto);
    }

    public void mergeOrganizations() {
        OrganizationMergeRequestDTO request = new OrganizationMergeRequestDTO();
        request.setOrgId1(mergeOrgId1);
        request.setOrgId2(mergeOrgId2);
        request.setNewName(mergeNewName);

        // Создаём Address из AddressRequestDTO
        Address newAddress = new Address();
        newAddress.setStreet(mergeNewStreet);
        newAddress.setZipCode(mergeNewZipCode);
        request.setNewAddress(newAddress); // ← теперь тип совпадает!

        mergeResult = organizationService.mergeOrganizations(request);
    }

//    public void mergeOrganizations() {
//        OrganizationMergeRequestDTO request = new OrganizationMergeRequestDTO();
//        request.setOrgId1(mergeOrgId1);
//        request.setOrgId2(mergeOrgId2);
//        request.setNewName(mergeNewName);
//
//        AddressRequestDTO newAddress = new AddressRequestDTO();
//        newAddress.setStreet(mergeNewStreet);
//        newAddress.setZipCode(mergeNewZipCode);
//        request.setNewAddress(newAddress);
//
//        mergeResult = organizationService.mergeOrganizations(request);
//    }

    public void absorbOrganization() {
        OrganizationAbsorbRequestDTO request = new OrganizationAbsorbRequestDTO();
        request.setOrgId1(absorbOrgId1);
        request.setOrgId2(absorbOrgId2);
        absorbResult = organizationService.absorbOrganization(request);
    }

    // --- Геттеры и сеттеры ---

    public OrganizationResponseDTO getMaxFullNameOrg() { return maxFullNameOrg; }
    public Long getCountResult() { return countResult; }
    public OrganizationResponseDTO getMergeResult() { return mergeResult; }
    public OrganizationResponseDTO getAbsorbResult() { return absorbResult; }

    public String getPostalStreet() { return postalStreet; }
    public void setPostalStreet(String postalStreet) { this.postalStreet = postalStreet; }

    public String getPostalZipCode() { return postalZipCode; }
    public void setPostalZipCode(String postalZipCode) { this.postalZipCode = postalZipCode; }

    public OrganizationType getSelectedType() { return selectedType; }
    public void setSelectedType(OrganizationType selectedType) { this.selectedType = selectedType; }

    public Long getMergeOrgId1() { return mergeOrgId1; }
    public void setMergeOrgId1(Long mergeOrgId1) { this.mergeOrgId1 = mergeOrgId1; }

    public Long getMergeOrgId2() { return mergeOrgId2; }
    public void setMergeOrgId2(Long mergeOrgId2) { this.mergeOrgId2 = mergeOrgId2; }

    public String getMergeNewName() { return mergeNewName; }
    public void setMergeNewName(String mergeNewName) { this.mergeNewName = mergeNewName; }

    public String getMergeNewStreet() { return mergeNewStreet; }
    public void setMergeNewStreet(String mergeNewStreet) { this.mergeNewStreet = mergeNewStreet; }

    public String getMergeNewZipCode() { return mergeNewZipCode; }
    public void setMergeNewZipCode(String mergeNewZipCode) { this.mergeNewZipCode = mergeNewZipCode; }

    public Long getAbsorbOrgId1() { return absorbOrgId1; }
    public void setAbsorbOrgId1(Long absorbOrgId1) { this.absorbOrgId1 = absorbOrgId1; }

    public Long getAbsorbOrgId2() { return absorbOrgId2; }
    public void setAbsorbOrgId2(Long absorbOrgId2) { this.absorbOrgId2 = absorbOrgId2; }
}