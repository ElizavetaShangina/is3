package organization.web;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.file.UploadedFile;
import organization.entity.ImportOperation;
import organization.entity.User;
import organization.repository.ImportOperationRepository;
import organization.repository.UserRepository;
import organization.service.ObjectImportService;
import organization.service.UserService;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

@Named("importManager")
@SessionScoped
public class ImportManagerBean implements Serializable {

    @Inject
    private ObjectImportService importService;

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private ImportOperationRepository historyRepository;

    private String username;
    private UploadedFile uploadedFile;

    private List<ImportOperation> importHistory;
    private boolean isAdmin = false;

    @PostConstruct
    public void init() {
        userService.setupTestUsers();
    }

    public void loadHistory() {
        if (username == null || username.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Error", "Enter user's name."));
            importHistory = List.of();
            return;
        }

        User currentUser = userRepository.findByUsername(username).orElse(null);
        if (currentUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "User wasn't found."));
            importHistory = List.of();
            return;
        }

        this.isAdmin = currentUser.isAdmin();

        if (this.isAdmin) {
            this.importHistory = historyRepository.findAll();
        } else {
            this.importHistory = historyRepository.findByUser(currentUser);
        }
    }

    public void upload() {
        if (uploadedFile == null || uploadedFile.getFileName() == null || uploadedFile.getFileName().isEmpty() || uploadedFile.getSize() == 0 || username == null || username.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Error", "Choose file and enter user's name."));
            return;
        }

        try (InputStream is = uploadedFile.getInputStream()) {
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User doesn't found."));

            importService.performImport(is, currentUser);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Import successfully run."));

            loadHistory();

        } catch (Exception e) {
            // 1. Получаем чистое сообщение
            String cleanMessage = getRootErrorMessage(e);

            // 2. ВАЖНО: Передаем именно cleanMessage, а не e.getMessage()
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Import error", cleanMessage));

            loadHistory();
        }
        // finally не нужен, так как loadHistory() вызывается в обоих блоках try/catch корректно
    }

    private String getRootErrorMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }

        String msg = root.getMessage();

        if (msg == null) {
            return "An internal server error has occurred";
        }

        if (msg.startsWith("Import error: ")) {
            msg = msg.substring("Import error: ".length());
        }

        if (msg.startsWith("java.lang.RuntimeException: ")) {
            msg = msg.substring("java.lang.RuntimeException: ".length());
        }


        if (msg.contains("Organization with Name") && msg.contains("already exists")) {
            return "Unique violation: An organization with this name and type already exists. Please check the CSV file for duplicates.";
        }

        return msg;
    }

    // Геттеры и сеттеры
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public UploadedFile getUploadedFile() { return uploadedFile; }
    public void setUploadedFile(UploadedFile uploadedFile) { this.uploadedFile = uploadedFile; }
    public List<ImportOperation> getImportHistory() { return importHistory; }
    public boolean isAdmin() { return isAdmin; }
}