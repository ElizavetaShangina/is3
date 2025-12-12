package organization.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam; // <-- Новый импорт
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import organization.entity.ImportOperation;
import organization.entity.User;
import organization.repository.ImportOperationRepository;
import organization.repository.UserRepository; // <-- Новый импорт

import java.util.List;

@Path("/import-history")
public class ImportHistoryResource {

    @Inject
    private ImportOperationRepository historyRepository;

    @Inject
    private UserRepository userRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    // Пользовательское имя теперь передается как QueryParam
    public Response getHistory(@QueryParam("username") String username) {

        // Получаем пользователя по имени. Если не найден, создаем тестового (для заглушки)
        User currentUser = userRepository.findByUsername(username).orElse(null);

        if (currentUser == null) {
            // Если пользователь не найден (или не введен), возвращаем пустой список
            return Response.ok(List.of()).build();
        }

        List<ImportOperation> history;

        // Предполагаем, что администратор - это пользователь 'admin'
        // В реальной системе это проверяется через currentUser.isAdmin()
        if (currentUser.isAdmin() || "admin".equals(currentUser.getUsername())) {
            // Администратор видит все операции
            history = historyRepository.findAll();
        } else {
            // Обычный пользователь видит только свои операции
            history = historyRepository.findByUser(currentUser);
        }

        // В идеале: маппинг в DTO, но оставляем Entity для простоты
        return Response.ok(history).build();
    }
}