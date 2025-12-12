package organization.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import organization.dto.*;
import organization.entity.Organization;
import organization.mapper.OrganizationMapper;
import organization.service.OrganizationService;

import java.util.List;

@Path("/organization")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class UserController {
    @Inject
    OrganizationService organizationService;

    @POST
    public Response createOrganization(OrganizationRequestDTO organization) {
        OrganizationResponseDTO createdOrganization = organizationService.createOrganization(organization);
        return Response.status(Response.Status.CREATED).entity(createdOrganization).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteOrganization(@PathParam("id") Long id) {
        organizationService.deleteOrganization(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/get-organization-with-max-full-name")
    public Response getOrganizationWithMaxFullName() {
        OrganizationResponseDTO dto = organizationService.getOrganizationWithMaxFullName();
        return Response.status(Response.Status.OK).entity(dto).build();

    }

    @POST
    @Path("/count-by-postal-address")
    public Response countByPostalAddress(AddressRequestDTO postalAddressDTO) {
        long count = organizationService.countByPostalAddress(postalAddressDTO);
        return Response.status(Response.Status.OK).entity(count).build();

    }
    @POST
    @Path("/count-by-type-less-than")
    public Response countByTypeLessThan(OrganizationTypeDTO dto) {
        long count = organizationService.countByTypeLessThan(dto);
        return Response.ok(count).build();
    }

    @POST
    @Path("/merge-organization") //нужно ли удалять старые организации?
    public Response mergeOrganizations(OrganizationMergeRequestDTO organizationMergeRequestDTO) {
        OrganizationResponseDTO dto = organizationService.mergeOrganizations(organizationMergeRequestDTO);
        return Response.status(Response.Status.OK).entity(dto).build();
    }

    @POST
    @Path("/absorb-organization")
    public Response absorbOrganization(OrganizationAbsorbRequestDTO organizationAbsorbRequestDTO) {
        OrganizationResponseDTO dto = organizationService.absorbOrganization(organizationAbsorbRequestDTO);
        return Response.status(Response.Status.OK).entity(dto).build();
    }
    @GET
    @Path("/all")
    public Response getAllOrganizations() {
        List<OrganizationResponseDTO> result = organizationService.getAllOrganizations();
        return Response.status(Response.Status.OK).entity(result).build();
    }


}

