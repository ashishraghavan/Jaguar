package com.jaguar.service;


import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.testng.util.Strings;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component
@Path("/token")
public class TokenService extends CommonService {

    @POST
    @Transactional(readOnly = false)
    public Response generateToken(@Context ContainerRequestContext requestContext,
                                  @FormDataParam("auth_code") final String authorizationCode) {
        if (Strings.isNullOrEmpty(authorizationCode)) {
            return Response.status(HttpStatus.BAD_REQUEST.value())
                    .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                            .withMessage("Authorization code").build()).build();
        }
        return Response.ok().build();
    }
}
