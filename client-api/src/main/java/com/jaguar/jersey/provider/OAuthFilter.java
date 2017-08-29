package com.jaguar.jersey.provider;


import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.IUser;
import org.springframework.http.HttpStatus;
import org.testng.util.Strings;

import javax.annotation.security.PermitAll;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;

@Provider
public class OAuthFilter extends BaseFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final Method resourceMethod = resourceInfo.getResourceMethod();
        if(resourceMethod != null) {
            if(resourceMethod.getAnnotation(PermitAll.class) == null) {
                //If this method does not contai the PermitAll annotation
                //we need to look for the Access token.
                final String authorization = requestContext.getHeaderString(AUTHORIZATION);
                if(Strings.isNullOrEmpty(authorization)) {
                    requestContext.abortWith(Response.status(HttpStatus.UNAUTHORIZED.value()).build());
                }
                //Bearer 3456.....
                final String[] authTokenized = authorization.trim().split(" ");
                if(authTokenized.length != 2) {
                   requestContext.abortWith(Response.status(HttpStatus.BAD_REQUEST.value())
                           .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.ErrorCode.FREE_FORM.getArgumentCode())
                                   .withMessage("The access token is expected to be of the format Bearer token").build()).build());
                }
                final String authToken = authTokenized[1];
                //Check against the CacheManager.
                final IUser authorizedUser = getCacheManager().getTokenCache().getIfPresent(authToken);
                if(authorizedUser == null) {
                    requestContext.abortWith(Response.status(HttpStatus.UNAUTHORIZED.value()).build());
                }
                //Create the security context
                requestContext.setSecurityContext(new JaguarSecurityContext(authorizedUser,requestContext.getUriInfo()));
            }
        }
    }
}
