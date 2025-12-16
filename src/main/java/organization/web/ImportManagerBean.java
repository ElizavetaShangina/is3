package organization.web;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import organization.config.CacheLoggingInterceptor;
import organization.entity.ImportOperation;
import organization.entity.User;
import organization.repository.ImportOperationRepository;
import organization.repository.UserRepository;
import organization.service.MinioService;
import organization.service.ObjectImportService;
import organization.service.UserService;

import java.io.ByteArrayInputStream;
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
    @Inject
    private MinioService minioService;

    private String username;
    private UploadedFile uploadedFile;
    private List<ImportOperation> importHistory;
    private boolean isAdmin = false;
    private boolean cacheLogEnabled = true; // Для чекбокса

    @PostConstruct
    public void init() {
        userService.setupTestUsers();
        this.cacheLogEnabled = CacheLoggingInterceptor.enabled;
    }

    // Метод для переключения логов кэша
    public void toggleCacheLog() {
        CacheLoggingInterceptor.enabled = this.cacheLogEnabled;
        String status = this.cacheLogEnabled ? "Enabled" : "Disabled";
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Cache Log", "L2 Cache Statistics " + status));
    }

    // Метод для скачивания файла
    public StreamedContent downloadFile(ImportOperation operation) {
        if (operation.getMinioObjectName() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "File not found in storage"));
            return null;
        }
        try {
            InputStream stream = minioService.downloadFile(operation.getMinioObjectName());
            return DefaultStreamedContent.builder()
                    .name("import_" + operation.getId() + ".csv")
                    .contentType("text/csv")
                    .stream(() -> stream)
                    .build();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Download Error", e.getMessage()));
            return null;
        }
    }

    public void upload() {
        if (uploadedFile == null || uploadedFile.getSize() == 0 || username == null || username.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Error", "Choose file and enter username."));
            return;
        }

        try {
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Читаем контент в массив байтов, чтобы передать и в MinIO, и в парсер
            byte[] content = uploadedFile.getContent();

            // Вызываем метод с 2PC транзакцией
            importService.performImport(content, currentUser, uploadedFile.getFileName());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Import completed successfully."));
        } catch (Exception e) {
            String msg = getRootErrorMessage(e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Import Error", msg));
        } finally {
            loadHistory();
        }
    }

    // ... loadHistory и getRootErrorMessage (оставить как были) ...
    public void loadHistory() {
        if (username == null || username.trim().isEmpty()) return;
        User u = userRepository.findByUsername(username).orElse(null);
        if (u != null) {
            this.isAdmin = u.isAdmin();
            this.importHistory = isAdmin ? historyRepository.findAll() : historyRepository.findByUser(u);
        }
    }

    private String getRootErrorMessage(Throwable e) {
        Throwable root = e;
        while(root.getCause() != null) root = root.getCause();
        return root.getMessage();
    }

    // Геттеры/Сеттеры
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public UploadedFile getUploadedFile() { return uploadedFile; }
    public void setUploadedFile(UploadedFile uploadedFile) { this.uploadedFile = uploadedFile; }
    public List<ImportOperation> getImportHistory() { return importHistory; }
    public boolean isAdmin() { return isAdmin; }
    public boolean isCacheLogEnabled() { return cacheLogEnabled; }
    public void setCacheLogEnabled(boolean cacheLogEnabled) { this.cacheLogEnabled = cacheLogEnabled; }
}