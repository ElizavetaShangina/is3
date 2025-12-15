package organization.service;

import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import organization.entity.ImportOperation;
import organization.entity.ImportStatus;
import organization.entity.User;

import java.time.ZonedDateTime;

@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ImportHistoryService {

    @PersistenceContext
    private EntityManager em;

    // REQUIRES_NEW: Останавливает текущую транзакцию, открывает новую,
    // выполняет запись, коммитит её. Если потом вылетит ошибка в импорте,
    // эта запись уже будет в БД.
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public ImportOperation createLogEntry(User user, ImportStatus status) {
        ImportOperation logEntry = new ImportOperation();

        // Мерджим пользователя, чтобы он был в контексте
        User managedUser = em.getReference(User.class, user.getId());
        logEntry.setUser(managedUser);

        logEntry.setStatus(status);
        logEntry.setStartTime(ZonedDateTime.now());

        em.persist(logEntry);
        em.flush(); // Принудительно генерируем ID
        return logEntry;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateLogEntry(Long logEntryId, ImportStatus status, Integer count, String errorMessage) {
        if (logEntryId == null) return;

        ImportOperation managedLog = em.find(ImportOperation.class, logEntryId);
        if (managedLog != null) {
            managedLog.setStatus(status);
            managedLog.setEndTime(ZonedDateTime.now());
            managedLog.setAddedObjectsCount(count);

            if (errorMessage != null && errorMessage.length() > 4000) {
                errorMessage = errorMessage.substring(0, 4000);
            }
            managedLog.setErrorMessage(errorMessage);

        }
    }
}