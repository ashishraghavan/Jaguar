package com.jaguar.jersey.provider;


import com.jaguar.om.IUser;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.security.Principal;

public class JaguarSecurityContext implements SecurityContext {

    private IUser user;
    private boolean isSecure;

    public JaguarSecurityContext(final IUser user,final UriInfo uriInfo) {
        this.user = user;
        this.isSecure = uriInfo.getBaseUri().getScheme().startsWith("https");
    }

    @Override
    public Principal getUserPrincipal() {
        return user;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return this.isSecure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "OAUTH2";
    }
}
