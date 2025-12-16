package organization.view;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import organization.config.EntityManagerProducer;
import organization.dto.*;
import organization.entity.Address;
import organization.entity.Coordinates;
import organization.entity.Organization;
import organization.entity.OrganizationType;
import organization.service.OrganizationService;
import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Named
@ViewScoped
public class OrganizationView implements Serializable {

    @Inject
    private EntityManagerProducer emProducer;

    @Inject
    private OrganizationService organizationService;

    private OrganizationRequestDTO newOrganizationDTO;

    private List<OrganizationResponseDTO> organizationsDTO;
    private String filterName;

    private List<OrganizationResponseDTO> allOrganizations; // Сохранённый полный список
    private String filterFullName; // Новое поле для фильтрации по fullName

    private boolean showMaxFullNameModal;
    private OrganizationResponseDTO selectedOrganization;
    private OrganizationResponseDTO editedOrganization;

    // === Поля для специальных операций ===
    private OrganizationResponseDTO maxFullNameOrg;
    private Long countByPostalAddressResult;
    private Long countByTypeResult;
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

    @PostConstruct
    public void init() {
        loadAll();
    }

    public void loadAll() {

        organizationsDTO = organizationService.getAllOrganizations();

        if (allOrganizations == null) {
            allOrganizations = new ArrayList<>(organizationsDTO);
        }
    }

    public void loadFiltered() {
        if (filterName == null || filterName.trim().isEmpty()) {
            loadAll();
        } else {
            // Фильтрация по полному совпадению имени
            organizationsDTO = organizationService.getAllOrganizations().stream()
                    .filter(org -> filterName.equals(org.getName()))
                    .toList();
        }
    }


    public void init_filter() {
        loadAll_filter();
    }

    public void loadAll_filter() {
        allOrganizations = organizationService.getAllOrganizations();
        organizationsDTO = new ArrayList<>(allOrganizations); // начальное состояние — полный список
    }

    public void clearL2Cache() {
        emProducer.clearAllCaches();
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cache L2 cleared", "Cached data deleted."));
    }

    public void applyFullNameFilter() {
        if (filterFullName == null || filterFullName.trim().isEmpty()) {
            organizationsDTO = new ArrayList<>(allOrganizations);
        } else {
            organizationsDTO = allOrganizations.stream()
                    .filter(org -> filterFullName.equals(org.getFullName()))
                    .collect(Collectors.toList());
        }
    }

    public void clearFilter() {
        filterFullName = null;
        organizationsDTO = new ArrayList<>(allOrganizations);
    }

    public void view(Long id) {
        selectedOrganization = organizationService.getOrganizationById(id);
        System.out.println("Выбрана организация: " + selectedOrganization);
    }

    public void resetSelectedOrganization() {
        this.selectedOrganization = null;
    }

    public void resetAllParams() {
        this.selectedOrganization = null;
        this.editedOrganization = null;
        this.newOrganizationDTO = null;
        this.maxFullNameOrg = null;
    }

    public void edit(Long id) {
        this.editedOrganization = organizationService.getOrganizationById(id);
    }

    public void updateOrganization() {
        resetSelectedOrganization();
        System.out.println("организация с правками:" + editedOrganization);
        if (editedOrganization != null) {
            organizationService.updateOrganization(editedOrganization.getId(), editedOrganization);
            this.organizationsDTO = organizationService.getAllOrganizations();
            this.editedOrganization = null;
        }
    }

    public void delete(Long id) {
        resetSelectedOrganization();
        organizationService.deleteOrganization(id);
        loadAll(); // обновить список
    }

    public void prepareNewOrganization() {
        resetSelectedOrganization();
        this.newOrganizationDTO = new OrganizationRequestDTO();
        this.newOrganizationDTO.setCoordinates(new Coordinates());
        this.newOrganizationDTO.setOfficialAddress(new Address());
        this.newOrganizationDTO.setPostalAddress(new Address());
    }

    public void addOrganization() {
        resetSelectedOrganization();
        if (newOrganizationDTO != null) {
            organizationService.createOrganization(newOrganizationDTO);
            this.organizationsDTO = organizationService.getAllOrganizations();
            this.newOrganizationDTO = null;
        }
    }


    public void initNewOrganization() {
        resetSelectedOrganization();
        newOrganizationDTO = new OrganizationRequestDTO();
        newOrganizationDTO.setCoordinates(new Coordinates());
        newOrganizationDTO.setOfficialAddress(new Address());
        newOrganizationDTO.setPostalAddress(new Address());
    }

    public void prepareMaxFullName() {
        resetAllParams();
        this.showMaxFullNameModal = true;
    }

    public void findMaxFullName() {
        maxFullNameOrg = organizationService.getOrganizationWithMaxFullName();
    }
//    public String addOrganization() {
//        try {
//            organizationService.createOrganization(newOrganizationDTO);
//            initNewOrganization();
//            loadAll();
//            System.out.println("Организация успешно добавлена");
//            return "list?faces-redirect=true";
//        } catch (Exception e) {
//            System.out.println("Ошибка при добавлении: " + e.getMessage());
//            e.printStackTrace();
//            return null;
//        }
//    }


    public void executeCountByPostalAddress() {
        AddressRequestDTO addr = new AddressRequestDTO();
        addr.setStreet(postalStreet);
        addr.setZipCode(postalZipCode);
        countByPostalAddressResult = organizationService.countByPostalAddress(addr);
    }

    public void executeCountByTypeLessThan() {
        OrganizationTypeDTO dto = new OrganizationTypeDTO();
        dto.setType(selectedType);
        countByTypeResult = organizationService.countByTypeLessThan(dto);
    }

    public void executeMerge() {
        OrganizationMergeRequestDTO req = new OrganizationMergeRequestDTO();
        req.setOrgId1(mergeOrgId1);
        req.setOrgId2(mergeOrgId2);
        req.setNewName(mergeNewName);

        Address newAddress = new Address();
        newAddress.setStreet(mergeNewStreet);
        newAddress.setZipCode(mergeNewZipCode);
        req.setNewAddress(newAddress);

        mergeResult = organizationService.mergeOrganizations(req);
        loadAll(); // обновить список
    }

    public void executeAbsorb() {
        OrganizationAbsorbRequestDTO req = new OrganizationAbsorbRequestDTO();
        req.setOrgId1(absorbOrgId1);
        req.setOrgId2(absorbOrgId2);
        absorbResult = organizationService.absorbOrganization(req);
        loadAll();
    }


}

