package organization.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import organization.entity.User;

import java.util.Optional;

@ApplicationScoped
public class UserRepository {

    @Inject
    private EntityManager em;

    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }


    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public User findOrCreate(String username, boolean isAdmin) {
        return findByUsername(username).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setAdmin(isAdmin);
            em.persist(newUser);
            return newUser;
        });
    }
}
