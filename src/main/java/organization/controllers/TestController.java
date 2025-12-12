package organization.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
//import organization.entity.TestEntity;
import organization.service.OrganizationService;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Path("/ping")
public class TestController {

    @Inject
    private OrganizationService organizationService;

    @GET
    public Response ping() {
        return Response.ok("pong").build();
    }

//    @POST
//    public Response createTest(TestEntity testEntity) {
//        TestEntity test = organizationService.createEntity(testEntity);
//        return Response.ok(test).build();
//    }
}