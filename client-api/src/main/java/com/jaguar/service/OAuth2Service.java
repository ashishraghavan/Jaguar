package com.jaguar.service;

import com.beust.jcommander.internal.Sets;
import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.*;
import com.jaguar.om.impl.*;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.math.NumberUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.testng.collections.Lists;
import org.testng.util.Strings;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

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
     * scopeString = are the roles that the application can request. See {@link ApplicationRole}
     * This request does not have the annotation of @PermitAll because the user needs to
     * be logged in when this call is made. If not, a 401 will be sent before the request
     * reaches this method. When the client receives a 401, it understands that a token
     * is not available and therefore re-directs the user to login.
     */
    @Path("/authorize")
    @GET
    @PermitAll
    @Transactional(readOnly = true)
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_HTML})
    public Response authorize(
            final ContainerRequestContext requestContext,
            @Context ServletContext servletContext,
            @QueryParam("response_type") final String responseType,
            @QueryParam("client_id") final String clientId,
            @QueryParam("redirect_uri") final String redirectUri,
            @QueryParam("scope") final String scopeString,
            @QueryParam("prompt") final String prompt,
            @QueryParam("device_uid") final String deviceUid,
            @QueryParam("username") final String email) {
        //Make sure all query parameters except scopeString is present
        if(Strings.isNullOrEmpty(responseType)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                    .withMessage("Response type")
                    .build())
                    .build();
        }

        if(Strings.isNullOrEmpty(clientId)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                    .withMessage("Client Id")
                    .build())
                    .build();
        }

        if(Strings.isNullOrEmpty(redirectUri)) {
            return Response.status(400).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                    .withMessage("Redirect URI")
                    .build())
                    .build();
        }

        if(Strings.isNullOrEmpty(deviceUid)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                    .withMessage("device_uid")
                    .build())
                    .build();
        }

        //We need to make sure the application is a valid one.
        IApplication application;
        try {
            application = new Application(Integer.parseInt(clientId));
            application = getDao().loadSingleFiltered(application,null,false);
        } catch (Exception e) {
            serviceLogger.error("An exception occurred while querying for the application with message "+e.getLocalizedMessage());
            return Response.status(HttpStatus.BAD_REQUEST.value())
                    .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.NULL_OBJECT_FROM_QUERY)
                            .withMessage("Application with client id " + clientId).build()).build();
        }

        if(application == null) {
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }

        //Check if scopeString being sent is among the roles that this application contains.
        //We need the name of the scopeString, not its description.
        final IApplicationRole appRole = new ApplicationRole(application);
        final Set<String> roles;
        Set<IRole> roleSet;
        String[] tokenizedScopeString;
        try {
            //Compare each role that the user has sent against the roles we have from the application.
            if(Strings.isNullOrEmpty(scopeString)) {
                serviceLogger.error("No scopes/roles specified for this request.");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                        .withMessage("Scope/role").build()).build();
            }
            tokenizedScopeString = scopeString.split(",");
            if(tokenizedScopeString.length <= 0) {
                serviceLogger.error("The scope parameter is not in the requried format. Expected format was ?scope=abc,xyz but found "+Arrays.toString(tokenizedScopeString));
                return Response.status(HttpStatus.BAD_REQUEST.value())
                        .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.INVALID_ARGUMENT)
                                .withMessage("scope", "?scope=abc,xyz").build()).build();
            }
            final List<IApplicationRole> appRoleList = getDao().loadFiltered(appRole,false);
            if(appRoleList == null || appRoleList.isEmpty()) {
                serviceLogger.error("The application with client id "+clientId+" does not have any roles associated with it.");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.FREE_FORM).withMessage("The application with client id "+clientId+
                                " does not have any roles associated with it").build()).build();
            }
            roles = Sets.newHashSet();
            roleSet = Sets.newHashSet();
            roles.addAll(appRoleList.stream().map(apRole -> apRole.getRole().getName()).collect(Collectors.toList()));
            //Collect all the roles requested by the client.
            roleSet.addAll(appRoleList.stream().map(IApplicationRole::getRole).collect(Collectors.toSet()));
            for(String clientRole : tokenizedScopeString) {
                if(!roles.contains(clientRole)) {
                    return Response.status(HttpStatus.BAD_REQUEST.value())
                            .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.NOT_FOUND).withMessage("The role " + clientRole).build()).build();
                }
            }
        } catch (Exception e) {
            serviceLogger.error("An exception occurred while querying for the application role with message "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }

        final String authToken = getAuthTokenFromHeaders(requestContext.getHeaderString(AUTHORIZATION));
        //If authToken is null/empty, the authenticated user is null, otherwise check if the authToken is valid from the token cache.
        final IUser authenticatedUser = Strings.isNullOrEmpty(authToken) ? null : getCacheManager().getTokenCache().getIfPresent(authToken);
        boolean isAuthenticationRequired = false;
        boolean isSelectAccount = false;
        if(authenticatedUser == null) {
            serviceLogger.error("There was an error obtaining the user from the token");
            isAuthenticationRequired = true;
        }
        if(!Strings.isNullOrEmpty(prompt)) {
            //There is a prompt query. Check to see what its value is.
            //Allowed values are [none,login,consent, select_account]
            if(prompt.equalsIgnoreCase(PROMPT_LOGIN)) {
                isAuthenticationRequired = true;
            }
            if(prompt.equalsIgnoreCase(PROMPT_NONE) && Strings.isNullOrEmpty(authToken)) {
                //This is an error since prompt specified was none and the user is not authenticated.
                return Response.status(HttpStatus.UNAUTHORIZED.value())
                        .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.NOT_AUTHORIZED).build()).build();
            }
            if(prompt.equalsIgnoreCase(PROMPT_SELECT_ACCOUNT)) {
                isSelectAccount = true;
                //If there is no email for this request, we send back a Bad request.
                if(Strings.isNullOrEmpty(email)) {
                    serviceLogger.error("No email given with prompt "+PROMPT_SELECT_ACCOUNT);
                    return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                            .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                            .withMessage("Username/Email").build()).build();
                }
            }
        }

        //If this is select account prompt, send back a list of accounts that this user belongs to.
        if(isSelectAccount) {
            try {
                final IUser user = new User(email);
                final List<IUser> userList = getDao().loadFiltered(user,false);
                if(userList == null || userList.isEmpty()) {
                    return Response.noContent().build();
                }
                final List<IAccount> accountList = Lists.newArrayList();
                for(IUser accountUser : userList) {
                    accountList.add(accountUser.getAccount());
                }
                return Response.ok(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(accountList)).build();
            } catch (Exception e) {
                serviceLogger.error("There was an error querying accounts for this user with exception "+e.getLocalizedMessage());
                return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
            }
        }
        final String scheme = getScheme(requestContext);
        final URI absolutePath = requestContext.getUriInfo().getAbsolutePath();
        //If the current device is a desktop, we generate a unique id and append
        //it along with the url. If this is a mobile device (ANDROID), the device
        //id is generated using the android serial version and appended before
        //calling this authorization URL.
        //We pass the scopes to the login to get the user after authentication
        //and update the DeviceApplication table for roles to be used by
        //a user against an application.
        final String authQueryParams = "?"
                + REDIRECT_URI + "=" + redirectUri + "&"
                + OAUTH2_FLOW + "=" + "true" +  "&"
                + CLIENT_ID + "=" +clientId + "&"
                + DEVICE_UID + "=" +deviceUid + "&"
                + SCOPES + "=" +scopeString;
        if(isAuthenticationRequired || Strings.isNullOrEmpty(authToken)) {
            final URI loginUri;
            try {
                //If this application has its own customizable login page, we re-direct the user there.
                //If not, we use our login page in /webapp/consent.html.

                //We add a header so that the Authorization Service will know that a re-direction
                //from OAuth service happened and that after a successfully login, the client
                //must be sent the role list as in the code block below.
                //Append the re-direct URI along with the login url.
                //We don't allow custom login pages. It has to be a login page that we serve.
                loginUri = URI.create(scheme + "://" + absolutePath.getAuthority() + context + files + "/" + "login.html" + authQueryParams);
                serviceLogger.info("Generated login link:"+loginUri);
                return Response.temporaryRedirect(loginUri).location(loginUri).build();
            } catch (Exception e) {
                serviceLogger.error("An error occurred while re-directing to the login page with the message "+e.getLocalizedMessage());
                return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.FREE_FORM)
                                .withMessage(INTERNAL_SERVER_ERROR_MSG).build()).build();
            }
        }

        //
        //Take the user to the consent page since the user has authenticated.
        final URI consentUri = URI.create(scheme + "://" + absolutePath.getAuthority() + context + files + "/" + "consent.html" + authQueryParams);
        serviceLogger.info("Generated consent URI:"+consentUri);
        return Response.temporaryRedirect(consentUri).location(consentUri).build();
    }

    @Path("/update")
    @POST
    @Produces({MediaType.TEXT_HTML,MediaType.APPLICATION_JSON})
    @PermitAll
    @Transactional
    public Response updateAuthorization(final @FormDataParam("authorization") String authorization,
                                        final @FormDataParam("redirect_uri") String redirectUri,
                                        final @FormDataParam("authorization_code") String authorizationCode,
                                        final @FormDataParam("client_id") String clientIdStr,
                                        final @FormDataParam("scopes") String scopes,
                                        final @FormDataParam("device_uid") String deviceUid) {
        IDeviceApplication iDeviceApplication;
        if(Strings.isNullOrEmpty(authorization)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                    .withMessage("Authorization value").build()).build();
        }
        final List<String> authorizations = Arrays.asList(IDeviceApplication.Authorization.stringValues());
        if(!authorizations.contains(authorization)) {
            //If authorization is not a valid authorization value, we send back an error.
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INVALID_ARGUMENT)
                    .withMessage("Authorization", "among " + authorizations.toString()).build()).build();
        }

        if(Strings.isNullOrEmpty(authorizationCode)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INVALID_ARGUMENT).withMessage("Authorization Code","xxxxx-xxxxx")
                    .build()).build();
        }

        if(Strings.isNullOrEmpty(redirectUri)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("Redirect URI")
                    .build()).build();
        }

        if(Strings.isNullOrEmpty(clientIdStr)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("Client Id")
                    .build()).build();
        }

        if(!NumberUtils.isCreatable(clientIdStr)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INVALID_ARGUMENT).withMessage("Client Id","123...")
                    .build()).build();
        }

        if(Strings.isNullOrEmpty(deviceUid)) {
            serviceLogger.error("Device Uid is required for updating the authorization status");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("device_uid").build()).build();
        }

        final int clientId = Integer.parseInt(clientIdStr);
        //See if the authorization code is still valid. If we can get the user from the authorization code,
        //it is still valid.
        final IUser user = getCacheManager().getUserAuthorizationCache().getIfPresent(authorizationCode);
        if(user == null) {
            serviceLogger.error("The authorization code is invalid or has expired");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.EXCEPTION).withMessage("The authorization code is invalid or has expired")
                    .build()).build();
        }

        try {
            //If this is the agree option, update the application authorization
            //Get the application using the client id.
            IApplication application = new Application(clientId);
            application = getDao().loadSingleFiltered(application,null,false);
            if(application == null) {
                serviceLogger.error("The application with client id "+clientIdStr+" does not exist");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.EXCEPTION)
                        .withMessage("application with client id "+clientIdStr+" does not exist")
                        .build())
                        .build();
            }
            //Get the device for which we are updating the authorization.
            IDevice device = new Device(application.getAccount(),deviceUid);
            device.setActive(true);
            device = getDao().loadSingleFiltered(device,null,false);
            if(device == null) {
                serviceLogger.error("Invalid device_uid specified for update authorization request");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.EXCEPTION).withMessage("The device with uid "+deviceUid+" does not exist").build()).build();
            }
            //No need for separate blocks for agree & disagree.
            final IDeviceApplication.Authorization decision = IDeviceApplication.Authorization.valueOf(authorization);
            //Create an entry in the user application table.
            iDeviceApplication = new DeviceApplication(device,application);
            final IDeviceApplication loadedDeviceApplication = getDao().loadSingleFiltered(iDeviceApplication,null,false);
            if(loadedDeviceApplication != null) {
                //update operation.
                serviceLogger.info("The application with the client id "+clientIdStr+" has already authorized.");
                iDeviceApplication = loadedDeviceApplication;
            }
            //Set the consent decision.
            iDeviceApplication.setAuthorization(decision);
            getDao().save(iDeviceApplication);
            //Only if the user agreed to the roles, we send a re-direct.
            final URI uri;
            if(decision == IDeviceApplication.Authorization.AGREE) {
                uri = URI.create(redirectUri + "?" +AUTHORIZATION_CODE + "=" +authorizationCode + "&" + CLIENT_ID + "=" + clientIdStr + "&" + DEVICE_UID + "="+deviceUid);
            } else {
                uri = URI.create(redirectUri + "?" +ERROR_REASON+ "=" +"The user denied the authorization request");
            }
            return Response.seeOther(uri).location(uri).build();
        } catch (Exception e) {
            serviceLogger.error("There was an error processing this request with message "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.
                    builder().withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
    }

    @POST
    @PermitAll
    @Transactional
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getToken(@FormDataParam("authorization_code") final String authorizationCode,
                             @FormDataParam("client_id") final String clientIdStr,
                             @FormDataParam("device_uid") final String deviceUidStr) {
        if(Strings.isNullOrEmpty(authorizationCode)) {
            serviceLogger.error("The parameter authorization code is required for this request");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                    .withMessage("Authorization code").build()).build();
        }

        if(Strings.isNullOrEmpty(clientIdStr)) {
            serviceLogger.error("The parameter client id is required for this request");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                    .withMessage("Client id").build()).build();
        }

        if(!NumberUtils.isCreatable(clientIdStr)) {
            serviceLogger.error("The client id is expected to be a number, but found "+clientIdStr);
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INVALID_ARGUMENT)
                    .withMessage("Client id","123...").build()).build();
        }
        if(Strings.isNullOrEmpty(deviceUidStr)) {
            serviceLogger.error("The device uid is required for this request");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("device_uid").build()).build();
        }


        try {
            //First check if the user confirmed/denied the consent.
            //If the consent was denied, there won't be any authorization code.
            final int clientId = Integer.parseInt(clientIdStr);
            //If this is the agree option, update the application authorization
            //Get the application using the client id.
            IApplication application = new Application(clientId);
            application = getDao().loadSingleFiltered(application,null,false);
            if(application == null) {
                serviceLogger.error("The application with client id "+clientIdStr+" does not exist");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.EXCEPTION).withMessage("application with client id "+clientIdStr+" does not exist")
                        .build()).build();
            }

            final IUser user = getCacheManager().getUserAuthorizationCache().getIfPresent(authorizationCode);
            if(user == null) {
                serviceLogger.error("The authorization code is invalid or has expired");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.EXCEPTION).withMessage("The authorization code is invalid or has expired")
                        .build()).build();
            }

            //Get the device using the deviceUidStr parameter.
            IDevice device = new Device(deviceUidStr,user);
            device = getDao().loadSingleFiltered(device,null,false);
            if(device == null) {
                serviceLogger.error("There is no entry for DeviceUser. This is unexpected. User : "+user.getEmail()+" deviceUid : "+deviceUidStr);
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.NULL_OBJECT_FROM_QUERY).withMessage("deviceUid : "+deviceUidStr).build()).build();
            }
            IDeviceApplication deviceApplication = new DeviceApplication(device,application);
            deviceApplication = getDao().loadSingleFiltered(deviceApplication,null,false);
            if(deviceApplication == null) {
                serviceLogger.error("The device with Uid"+deviceUidStr+" cannot be associated with the application "+application.getName());
                return Response.status(HttpStatus.BAD_REQUEST.value())
                        .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.EXCEPTION)
                                .withMessage("The device with Uid"+deviceUidStr+" cannot be associated with the application "+application.getName()).build()).build();
            }
            if(deviceApplication.getAuthorization() == IDeviceApplication.Authorization.DISAGREE) {
                //Do not send the token in this case.
                serviceLogger.error("THe user "+user.getEmail()+" denied authorization for "+application.getName());
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.EXCEPTION).withMessage("The user with email "+user.getEmail()+" cannot be associated with the application "+application.getName()).build()).build();
            }
            //Create the access and refresh token for this user.
            final String token = UUID.randomUUID().toString();
            getCacheManager().getTokenCache().put(token,user);
            //Create refresh token
            final String refreshToken = UUID.randomUUID().toString();
            getCacheManager().getRefreshTokenCache().put(refreshToken,user);
            getCacheManager().getUserApplicationCache().put(user,application);
            final Map<String,Object> resultMap = ImmutableMap.<String,Object>builder()
                    .put("user",user)
                    .put("access_token",token)
                    .put("refresh_token",refreshToken)
                    .build();
            return Response.ok().entity(resultMap).build();
        } catch (Exception e) {
            serviceLogger.error("There was an error processing this request with message "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder().withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
    }
}
