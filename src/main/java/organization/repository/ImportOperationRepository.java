package organization.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import organization.entity.ImportOperation;
import organization.entity.User;

import java.util.List;
@ApplicationScoped // CDI Bean
public class ImportOperationRepository {

    @PersistenceContext
    private EntityManager em;

    public List<ImportOperation> findAll() {
        return em.createQuery("SELECT io FROM ImportOperation io ORDER BY io.startTime DESC", ImportOperation.class)
                .getResultList();
    }

    public List<ImportOperation> findByUser(User user) {
        return em.createQuery("SELECT io FROM ImportOperation io WHERE io.user = :user ORDER BY io.startTime DESC", ImportOperation.class)
                .setParameter("user", user)
                .getResultList();
    }
}