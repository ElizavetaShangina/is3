package organization.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import organization.service.MinioService;
import organization.service.ObjectImportService;

@Named("testFlagsManager")
@ApplicationScoped
public class TestFlagsManager {

    /**
     * Включает симуляцию отказа MinIO (Сценарий А)
     */
    public void enableMinioFailure() {
        MinioService.SIMULATE_UPLOAD_FAILURE = true;
        ObjectImportService.SIMULATE_DB_FAILURE = false;
        ObjectImportService.SIMULATE_MID_LOGIC_FAILURE = false;

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "THE CRASH IS ENABLED", "MinIO: File Storage Failure."));
    }

    /**
     * Включает симуляцию отказа БД (Сценарий B)
     */
    public void enableDBFailure() {
        MinioService.SIMULATE_UPLOAD_FAILURE = false;
        ObjectImportService.SIMULATE_DB_FAILURE = true;
        ObjectImportService.SIMULATE_MID_LOGIC_FAILURE = false;

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "THE CRASH IS ENABLED", "DB: DATABASE failure before commit."));
    }

    /**
     * Включает симуляцию ошибки в бизнес-логике (Сценарий С)
     */
    public void enableMidLogicFailure() {
        MinioService.SIMULATE_UPLOAD_FAILURE = false;
        ObjectImportService.SIMULATE_DB_FAILURE = false;
        ObjectImportService.SIMULATE_MID_LOGIC_FAILURE = true;

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "THE CRASH IS ENABLED", "LOGIC: Error in the middle of the business logic."));
    }

    /**
     * Сбрасывает все флаги
     */
    public void resetAllFlags() {
        MinioService.SIMULATE_UPLOAD_FAILURE = false;
        ObjectImportService.SIMULATE_DB_FAILURE = false;
        ObjectImportService.SIMULATE_MID_LOGIC_FAILURE = false;

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "flags have been reset", "All failure simulations are disabled."));
    }

    public String getCurrentStatus() {
        if (MinioService.SIMULATE_UPLOAD_FAILURE) return "MinIO (А)";
        if (ObjectImportService.SIMULATE_DB_FAILURE) return "DB (B)";
        if (ObjectImportService.SIMULATE_MID_LOGIC_FAILURE) return "Logic (C)";
        return "ОК";
    }
}