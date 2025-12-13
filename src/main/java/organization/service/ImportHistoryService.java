package organization.service;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import organization.entity.ImportOperation;
import organization.entity.ImportStatus;
import organization.entity.User;

import java.time.ZonedDateTime;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN) // <--- МЫ УПРАВЛЯЕМ САМИ
public class ImportHistoryService {

    @PersistenceContext
    private EntityManager em;

    @Resource
    private UserTransaction utx; // <--- Объект для ручного управления

    public ImportOperation createLogEntry(User user, ImportStatus status) {
        try {
            utx.begin(); // 1. НАЧАЛО ТРАНЗАКЦИИ

            ImportOperation logEntry = new ImportOperation();

            // Если юзер "отцепился" от сессии, находим его заново
            User managedUser = em.find(User.class, user.getId());
            logEntry.setUser(managedUser);

            logEntry.setStatus(status);
            logEntry.setStartTime(ZonedDateTime.now());

            em.persist(logEntry);

            utx.commit(); // 2. КОНЕЦ ТРАНЗАКЦИИ (Запись ушла в БД)
            return logEntry;
        } catch (Exception e) {
            e.printStackTrace();
            try { utx.rollback(); } catch (Exception ex) { /* ignore */ }
            throw new RuntimeException("Не удалось создать лог: " + e.getMessage());
        }
    }

    public void updateLogEntry(Long logEntryId, ImportStatus status, Integer count, String errorMessage) {
        try {
            utx.begin(); // 1. НАЧАЛО ТРАНЗАКЦИИ

            ImportOperation managedLog = em.find(ImportOperation.class, logEntryId);
            if (managedLog != null) {
                managedLog.setStatus(status);
                managedLog.setEndTime(ZonedDateTime.now());
                managedLog.setAddedObjectsCount(count);

                if (errorMessage != null && errorMessage.length() > 2000) {
                    errorMessage = errorMessage.substring(0, 2000);
                }
                managedLog.setErrorMessage(errorMessage);

                em.merge(managedLog);
            }

            utx.commit(); // 2. КОНЕЦ ТРАНЗАКЦИИ
        } catch (Exception e) {
            e.printStackTrace();
            try { utx.rollback(); } catch (Exception ex) { /* ignore */ }
        }
    }
}