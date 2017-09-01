package com.jaguar.service;

import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.IApplication;
import com.jaguar.om.IUserApplication;
import com.jaguar.om.impl.Application;
import com.jaguar.om.impl.ApplicationRole;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.testng.util.Strings;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Component
@Path("/oauth")
public class OAuth2Service extends CommonService {

    /**
     * The main authorization endpoint accessed by
     * http(s)://{server_name}:port_name(if required)/oauth/authorize with the
     * following URL parameters
     * response_type = mostly should be [code]
     * client_id = is the one when an application registers. See {@link Application#clientId}
     * redirect_uri = is the one when an application registers. See {@link Application#redirectUri}
     * scope = are the roles that the application can request. See {@link ApplicationRole}
     * This request does not have the annotation of @PermitAll because the user needs to
     * be logged in when this call is made. If not, a 401 will be sent before the request
     * reaches this method. When the client receives a 401, it understands that a token
     * is not available and therefore re-directs the user to login.
     */
    @Path("/authorize")
    @GET
    @PermitAll
    @Transactional(readOnly = true)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authorize(final ContainerRequestContext requestContext,@QueryParam("response_type") final String responseType,
                          @QueryParam("client_id") final String clientId,
                          @QueryParam("redirect_uri") final String redirectUri,
                          @QueryParam("scope") final String scope) {
        //Make sure all query parameters except scope is present
        if(Strings.isNullOrEmpty(responseType)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Response type")
                    .build())
                    .build();
        }

        if(Strings.isNullOrEmpty(clientId)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Client Id")
                    .build())
                    .build();
        }

        if(Strings.isNullOrEmpty(redirectUri)) {
            return Response.status(400).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Redirect URI")
                    .build())
                    .build();
        }

        if(getAuthTokenFromHeaders(requestContext.getHeaderString(AUTHORIZATION)) == null) {
            try {
                //redirect to login page.
                IApplication application = new Application(Integer.parseInt(clientId));
                application = getDao().loadSingleFiltered(application,null,false);
                return Response.temporaryRedirect(URI.create(application.getLoginPage())).build();
            } catch (Exception e) {
                serviceLogger.error("An error occurred while re-directing to the login page with the message "+e.getLocalizedMessage());
                return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.ErrorCode.FREE_FORM.getArgumentCode())
                                .withMessage("An internal server error occurred while re-directing to the login page").build()).build();
            }
        }

        //Send the options that the user can see after the login page.

        //If at this point, the user as already logged in, we send a response ok indicating
        //that the front-end should show the options of "Allow application to give so and so
        //access" or "Deny request". Depending on whether this user has already given this
        //particular application. If not, we have two options. 1) Sen

        //We might have to query the UserApplication table to see
        //scope is not a required parameter for this request.
        return Response.ok().build();
    }

    @Path("/update")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(readOnly = false)
    public Response updateAuthorization(final @FormDataParam("authorization") String authorization) {
        if(Strings.isNullOrEmpty(authorization)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Authorization value").build()).build();
        }
        final List<String> authorizations = Arrays.asList(IUserApplication.Authorization.stringValues());
        if(!authorizations.contains(authorization)) {
            //If authorization is not a valid authorization value, we send back an error.
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ErrorCode.INVALID_ARGUMENT.getArgumentCode())
                    .withMessage("Authorization","among "+ authorizations.toString()).build()).build();
        }

        //Check if this user has already authorized this application.
        //We check the user application table for the authorzation.
        return Response.ok().build();
    }
}
