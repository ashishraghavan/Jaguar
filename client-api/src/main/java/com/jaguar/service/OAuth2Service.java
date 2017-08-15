package com.jaguar.service;

import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.impl.Application;
import com.jaguar.om.impl.ApplicationRole;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.testng.util.Strings;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/oauth")
public class OAuth2Service {

    /**
     * The main authorization endpoint accessed by
     * http(s)://{server_name}:port_name(if required)/oauth/authorize with the
     * following URL parameters
     * response_type = mostly should be [code]
     * client_id = is the one when an application registers. See {@link Application#clientId}
     * redirect_uri = is the one when an application registers. See {@link Application#redirectUri}
     * scope = are the roles that the application can request. See {@link ApplicationRole}
     */
    @Path("/authorize")
    @Transactional(readOnly = true)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response authorize(@QueryParam("response_type") final String responseType,
                          @QueryParam("client_id") final String clientId,
                          @QueryParam("redirect_uri") final String redirectUri,
                          @QueryParam("scope") final String scope) {
        //Make sure all query parameters except scope is present
        if(Strings.isNullOrEmpty(responseType)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(new ErrorMessage.Builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Response type")
                    .build())
                    .build();
        }

        if(Strings.isNullOrEmpty(clientId)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(new ErrorMessage.Builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Client Id")
                    .build())
                    .build();
        }

        if(Strings.isNullOrEmpty(redirectUri)) {
            return Response.status(400).entity(new ErrorMessage.Builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Redirect URI")
                    .build())
                    .build();
        }

        //If at this point, the user as already logged in, we send a response ok indicating
        //that the front-end should show the options of "Allow application to give so and so
        //access" or "Deny request". Depending on whether this user has already given this
        //particular application

        //We might have to query the UserApplication table to see
        //scope is not a required parameter for this request.
        return Response.ok().build();
    }
}