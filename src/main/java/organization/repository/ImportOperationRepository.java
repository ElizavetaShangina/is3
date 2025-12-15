package organization.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import organization.entity.ImportOperation;
import organization.entity.User;

import java.util.List;

@ApplicationScoped
public class ImportOperationRepository {

    @PersistenceContext
    private EntityManager em;

    // Найти все операции (для админа)
    public List<ImportOperation> findAll() {
        return em.createQuery("SELECT i FROM ImportOperation i ORDER BY i.startTime DESC", ImportOperation.class)
                .getResultList();
    }

    public List<ImportOperation> findByUser(User user) {
        if (user == null) return List.of();

        // Используем user.id
        return em.createQuery("SELECT i FROM ImportOperation i WHERE i.user.id = :userId ORDER BY i.startTime DESC", ImportOperation.class)
                .setParameter("userId", user.getId())
                .getResultList();
    }
}