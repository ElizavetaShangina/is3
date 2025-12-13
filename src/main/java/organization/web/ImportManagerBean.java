package organization.web;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped; // SessionScoped для сохранения username
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.primefaces.model.file.UploadedFile;
import organization.repository.UserRepository;
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
@SessionScoped // Храним состояние, чтобы не терять имя пользователя и историю
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
        System.out.println("--- ЗАГРУЗКА ИСТОРИИ ДЛЯ: " + username + " ---");

        if (username == null || username.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Ошибка", "Введите имя пользователя."));
            return;
        }

        // Ищем пользователя свежим запросом
        User currentUser = userRepository.findByUsername(username).orElse(null);

        if (currentUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Пользователь '" + username + "' не найден."));
            importHistory = List.of();
            return;
        }

        this.isAdmin = currentUser.isAdmin();
        System.out.println("Пользователь найден. Admin: " + isAdmin + ", ID: " + currentUser.getId());

        if (this.isAdmin) {
            this.importHistory = historyRepository.findAll();
        } else {
            this.importHistory = historyRepository.findByUser(currentUser);
        }

        System.out.println("Найдено записей истории: " + (importHistory != null ? importHistory.size() : "null"));

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Успех",
                        "Загружено записей: " + (importHistory != null ? importHistory.size() : 0)));
    }

    // --- Логика Импорта Файла ---

    public void upload() {
        // Проверяем на null и на размер PrimeFaces-объекта
        if (uploadedFile == null || uploadedFile.getFileName() == null || uploadedFile.getFileName().isEmpty() || uploadedFile.getSize() == 0 || username == null || username.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Ошибка", "Выберите файл и введите имя пользователя."));
            return;
        }

        try (InputStream is = uploadedFile.getInputStream()) {
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден."));

            importService.performImport(is, currentUser);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Успех", "Импорт успешно запущен/завершен."));

            // Обновляем историю после импорта
            loadHistory();

        } catch (Exception e) {
            // Выводим ошибку из сервиса/валидации
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка импорта", e.getMessage()));
            // Также обновляем историю, чтобы увидеть запись 'FAILURE'
            loadHistory();
        }
    }

    // --- Геттеры и Сеттеры ---

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public UploadedFile getUploadedFile() { return uploadedFile; }
    public void setUploadedFile(UploadedFile uploadedFile) { this.uploadedFile = uploadedFile; }
    public List<ImportOperation> getImportHistory() { return importHistory; }
    public boolean isAdmin() { return isAdmin; }
}
