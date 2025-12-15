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
    public Response getHistory(@QueryParam("username") String username) {

        User currentUser = userRepository.findByUsername(username).orElse(null);

        if (currentUser == null) {
            return Response.ok(List.of()).build();
        }

        List<ImportOperation> history;

        if (currentUser.isAdmin() || "admin".equals(currentUser.getUsername())) {
            history = historyRepository.findAll();
        } else {
            history = historyRepository.findByUser(currentUser);
        }

        return Response.ok(history).build();
    }
}