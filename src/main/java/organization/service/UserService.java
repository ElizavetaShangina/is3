package organization.service;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import organization.entity.User;
import java.util.List;

@Stateless
public class UserService {

    @PersistenceContext
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void setupTestUsers() {
        createRequestUser("admin", true);
        createRequestUser("user1", false);
    }

    private void createRequestUser(String username, boolean isAdmin) {
        // Проверяем, есть ли юзер
        List<User> existing = em.createQuery("SELECT u FROM User u WHERE u.username = :name", User.class)
                .setParameter("name", username)
                .getResultList();

        if (existing.isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setAdmin(isAdmin);
            // user.setPassword(...) <--- ЭТО УБРАЛИ

            em.persist(user);
            System.out.println("--- CREATED USER: " + username + " ---");
        } else {
            System.out.println("--- USER ALREADY EXISTS: " + username + " ---");
        }
    }
}