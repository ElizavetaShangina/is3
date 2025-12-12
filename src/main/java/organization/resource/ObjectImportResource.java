//package organization.resource;
//
//import jakarta.inject.Inject;
//import jakarta.ws.rs.*;
//import jakarta.ws.rs.core.Context;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.Response;
//import jakarta.ws.rs.core.SecurityContext;
//import organization.entity.User;
//import organization.exception.UniqueConstraintViolationException;
//import organization.service.ObjectImportService;
//import organization.repository.UserRepository;
//
//import jakarta.servlet.http.Part;
//import java.io.InputStream;
//
//@Path("/objects/import")
//public class ObjectImportResource {
//
//    @Inject
//    private ObjectImportService importService;
//
//    @Inject
//    private UserRepository userRepository;
//
//    @POST
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    @Produces(MediaType.APPLICATION_JSON)
//
//    public Response uploadFile(@FormParam("file") Part file, @QueryParam("username") String username) {
//
//        if (file == null || username == null || username.trim().isEmpty()) {
//            return Response.status(Response.Status.BAD_REQUEST)
//                    .entity("{\"error\": \"Необходимо выбрать файл и указать имя пользователя.\"}")
//                    .build();
//        }
////получение текущего пользователя из бд
//
//        User currentUser = userRepository.findByUsername(username).orElse(null);
//        if (currentUser == null) {
//            // Если пользователь не найден (и не создан в заглушке init в ImportManagerBean)
//            return Response.status(Response.Status.UNAUTHORIZED)
//                    .entity("{\"error\": \"Пользователь '" + username + "' не найден. Проверьте имя.\"}")
//                    .build();
//        }
//
//
//        try (InputStream is = file.getInputStream()) {
//            importService.performImport(is, currentUser);
//            return Response.ok("Import works successfully.").build();
//
//        } catch (UniqueConstraintViolationException e) {
//            return Response.status(Response.Status.CONFLICT) // 409 Conflict
//                    .entity("{\"error\": \"Нарушено ограничение уникальности: " + e.getMessage() + "\"}")
//                    .build();
//        } catch (Exception e) {
//            // Ошибка уже залогирована в ImportOperation
//            return Response.status(Response.Status.BAD_REQUEST)
//                    .entity("{\"error\": \"Ошибка импорта: " + e.getMessage() + "\"}")
//                    .build();
//        }
//    }
//
//    // МЕТОД getMockUser УДАЛЕН, поскольку UserRepository выполняет эту функцию.
//}