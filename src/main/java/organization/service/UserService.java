package organization.service;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import organization.entity.User;
import organization.repository.UserRepository;

@Stateless // Это EJB, который управляет транзакцией
public class UserService {

    @Inject
    private UserRepository userRepository;

    /**
     * Создает или находит тестовых пользователей в отдельной транзакции.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW) // <-- Гарантирует новую транзакцию
    public void setupTestUsers() {
        // Логика создания тестовых пользователей
        userRepository.findOrCreate("admin", true);
        userRepository.findOrCreate("user1", false);
    }
}