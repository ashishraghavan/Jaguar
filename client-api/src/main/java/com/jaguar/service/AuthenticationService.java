package com.jaguar.service;

import com.google.common.collect.ImmutableMap;
import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.jersey.provider.JaguarSecurityContext;
import com.jaguar.om.IApplication;
import com.jaguar.om.IDevice;
import com.jaguar.om.IUser;
import com.jaguar.om.impl.Application;
import com.jaguar.om.impl.Device;
import com.jaguar.om.impl.User;
import com.jaguar.om.notification.Email;
import com.jaguar.om.notification.EmailManager;
import com.jaguar.om.notification.SMS;
import com.jaguar.om.notification.SMSManager;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.testng.util.Strings;

import javax.annotation.security.PermitAll;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

/**
 * Takes care of authenticating a user against
 * the supplied password.
 */

@Component
@Path("/login")
public class AuthenticationService extends CommonService {

    private static final Logger authorizationServiceLogger = Logger.getLogger(AuthenticationService.class.getSimpleName());

    /**
     * Does user login
     *
     * @return a {@link Response} object wrapping the {@link User} details
     * Required form data parameters are
     * username - the username
     * password - the password
     * device_id - the id of the device the user is trying to sign in into.
     * If this is a web application, there won't be a device_id
     * We determine if this is a web application by the client id.
     */
    @POST
    @PermitAll
    @Transactional
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_HTML})
    public Response login(@Context final ContainerRequestContext requestContext,
                          @FormDataParam("username") final String username,
                          @FormDataParam("password") final String password,
                          @FormDataParam("device_uid") final String deviceUid,
                          @FormDataParam("auth_flow") final String isAuthFlow,
                          @FormDataParam("redirect_uri") final String redirectUri,
                          @FormDataParam("client_id") final String clientIdStr,
                          @FormDataParam("scopes") final String scopes,
                          @FormDataParam("api") final String api,
                          @FormDataParam("model") final String model,
                          @FormDataParam("notification_service_id") final String notificationServiceId) {
        if (Strings.isNullOrEmpty(username)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                    .withMessage("Username")
                    .build())
                    .build();
        }

        if (Strings.isNullOrEmpty(password)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                    .withMessage("Password")
                    .build())
                    .build();
        }


        if(Strings.isNullOrEmpty(clientIdStr)) {
            serviceLogger.error("The client id is needed for the login operation");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                    .withMessage("client_id").build()).build();
        }

        if(!NumberUtils.isCreatable(clientIdStr)) {
            serviceLogger.error("The client id "+clientIdStr+" is not a number");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INVALID_ARGUMENT)
                    .withMessage("client_id","Integer(123...").build()).build();
        }
        //Get the client id from from the verification cookie.
        //Check if the cookie is still valid.
        final int clientId = Integer.parseInt(clientIdStr);
        IApplication application = new Application(clientId);
        try {
            application = getDao().loadSingleFiltered(application,null,false);
        } catch (Exception e) {
            serviceLogger.error("An exception occurred while querying for the application with client id "+clientId);
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR)
                    .withMessage(INTERNAL_SERVER_ERROR_MSG).build()).build();
        }

        if (application == null) {
            //The cookie entered was invalid.
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.COOKIE_NOT_VALID).build()).build();
        }
        try {
            application = getDao().load(application.getClass(), application.getId());
            //If this application is a mobile application and no device_id was
            //supplied, it is an error.
            if (Strings.isNullOrEmpty(deviceUid)) {
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                        .withMessage("device_uid")
                        .build())
                        .build();
            }
            //Get the user first.
            IUser user = new User(application.getAccount(), username);
            user = getDao().loadSingleFiltered(user, null, false);
            if (user == null) {
                //We did not find this user under this account.
                serviceLogger.error("We did not find the user "+username+" in our database");
                return Response.status(HttpStatus.BAD_REQUEST.value())
                        .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.NOT_FOUND)
                                .withMessage("The device with id " + deviceUid).build()).build();
            }
            //Check if this user is active.
            if(!user.isActive()) {
                serviceLogger.error("The user with the email "+user.getEmail()+" has not verified and is still inactive");
                return Response.status(HttpStatus.UNAUTHORIZED.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.EXCEPTION).withMessage("User needs to be verify either using email or phone").build()).build();
            }
            //Get the device now that is scoped to a user.
            IDevice device = new Device(deviceUid,user);
            final IDevice deviceFromDB = getDao().loadSingleFiltered(device, null, false);
            boolean isDeviceCreationRequired = false;
            if (deviceFromDB == null) {
                //There are two cases. Either this user is logging in
                //using a different device, or this user hasn't registered yet.
                //Since the device is null, this device is non-existent.
                serviceLogger.info("The user "+user.getEmail()+" is not mapped to the device with deviceUid "+deviceUid);
                isDeviceCreationRequired = true;
            }
            //Check the UserDevice table to see if we find a correct user -> device match.
            if(isDeviceCreationRequired) {
                //TODO Only if the application is a mobile application, do the following.
                //Check if all required parameters for device creation are present (api,model etc)
                if(Strings.isNullOrEmpty(api)) {
                    serviceLogger.error("The device "+deviceUid
                            +" is not registered with this user. An api version of the application is needed to login into this device");
                    return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                            .withErrorCode(ErrorMessage.FREE_FORM).withMessage("The device "+deviceUid
                                    +" is not registered with this user. An api version of the application is needed to login into this device").build()).build();
                }
                if(Strings.isNullOrEmpty(model)) {
                    serviceLogger.error("The device "+deviceUid
                            +" is not registered with this user. A model name of the device is needed to login into this device");
                    return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                            .withErrorCode(ErrorMessage.FREE_FORM).withMessage("The device "+deviceUid
                                    +" is not registered with this user. A model name of the device is needed to login into this device").build()).build();
                }
                //API version needs to be a number.
                if(!NumberUtils.isCreatable(api)) {
                    serviceLogger.error("The api version "+api+" is not a number");
                    return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                            .withErrorCode(ErrorMessage.INVALID_ARGUMENT).withMessage(api,"123...").build()).build();
                }
                if(!Strings.isNullOrEmpty(notificationServiceId)) {
                    device.setNotificationServiceId(notificationServiceId);
                }
                //If a device creation is required, we create it first
                //and then query for the DeviceUser table.
                //This device has not been assoicated with this user.
                //The user is probably logging in with a different device
                //for the same application.
                //Before saving this device user, we have to make sure, it is
                //indeed this user who is signing through a separate device.
                //Maybe generate a one time passcode and send to their provided phone number.
                device.setModel(model);
                device.setApiVersion(Integer.parseInt(api));
                device.setActive(true);
                getDao().save(device);
                //Since we have associated a device with this user at login time
                //we sent a notification either on the mobile app (if it's a mobile app) or web notification.
                //Or an SMS.
                final String scheme = getScheme(requestContext);
                final URI absolutePath = requestContext.getUriInfo().getAbsolutePath();
                final URI deviceDetailLink = URI.create(scheme + "://" + absolutePath.getAuthority() + "/user/"+username+"/device/"+deviceUid);
                final String notificationBody = String.format(DEVICE_ADDITION_LINK,deviceUid,deviceDetailLink);
                //Send a notification using both SMS and Email.
                //First send using the SMSManager.
                if (!Strings.isNullOrEmpty(user.getPhoneNumber())) {
                    //We prefer to send an SMS if we have the phone number.
                    //construct the SMS message.
                    //Get phone number in the proper format if it is not.
                    if(validatePhoneNumber(user.getPhoneNumber())) {
                        final String toPhoneNumber = getUSPhoneNumber(user.getPhoneNumber());
                        try {
                            final SMSManager smsManager = new SMSManager();
                            final SMS smsMessage = SMSManager.smsBuilder()
                                    .messageBody(notificationBody)
                                    .toPhone(toPhoneNumber)
                                    .build();
                            smsManager.sendSMS(smsMessage);
                        } catch (Exception e) {
                            serviceLogger.error("There was an error sending SMS message to "+username+" with device_uid "+deviceUid+" with message "+e.getMessage());
                            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                                    .withErrorCode(ErrorMessage.EXCEPTION).withMessage("Failed to send a message to phone number "+user.getPhoneNumber()).build()).build();
                        }
                    }
                }

                //Send an email through our SMTP server.
                //Construct the EMail message.
                try {
                    final EmailManager emailManager = new EmailManager();
                    final Email emailMessage = EmailManager.builder()
                            .body(notificationBody)
                            .subject(NEW_DEVICE_ADDED)
                            .to(user.getEmail())
                            .build();
                    emailManager.sendEmail(emailMessage);
                } catch (Exception e) {
                    serviceLogger.error("There was an error sending email message to "+username+" with email "+username+" with exception "+e.getMessage());
                    return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                            .withErrorCode(ErrorMessage.EXCEPTION).withMessage("Failed to send an email to "+username).build()).build();
                }

            }
            //Do the actual authentication.
            user.setPassword(password);
            user = getDao().loadSingleFiltered(user,null,false);
            if(user == null) {
                return Response.status(HttpStatus.BAD_REQUEST.value())
                        .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.NOT_FOUND)
                                .withMessage("The user " + username + " with the given password does not exist on this system").build()).build();
            }

            boolean isConsentFlow = false;
            if(!Strings.isNullOrEmpty(isAuthFlow)) {
                isConsentFlow = Boolean.parseBoolean(isAuthFlow);
            }

            final String scheme = getScheme(requestContext);
            //If this is the authorization flow (consent), we send the user to the redirection URI
            //with the authorization code appended to the URI.
            //Check if this device has already authorized application for this device.
            if(isConsentFlow) {
                //We need the re-redirect URI for this case.
                URI finalRedirectURI = null;
                if(Strings.isNullOrEmpty(redirectUri)) {
                    //try getting the re-direct URI from the application record.
                    final String strRedirectURI = application.getRedirectURI();
                    if(!Strings.isNullOrEmpty(strRedirectURI)) {
                        finalRedirectURI = URI.create(strRedirectURI);
                    }
                } else {
                    finalRedirectURI = URI.create(redirectUri);
                }

                if(finalRedirectURI == null || Strings.isNullOrEmpty(String.valueOf(finalRedirectURI))) {
                    serviceLogger.error("The redirect-URI cannot be obtained from the application record.");
                    return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                            .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("redirect_uri").build()).build();
                }
                //Create the authorization code.
                final String authorizationCode = UUID.randomUUID().toString();
                getCacheManager().getUserAuthorizationCache().put(authorizationCode,user);
                final URI absolutePath = requestContext.getUriInfo().getAbsolutePath();
                final String authQueryParams = "?"
                        + REDIRECT_URI + "=" + redirectUri
                        + "&" + OAUTH2_FLOW + "=" +isAuthFlow
                        + "&" + CLIENT_ID + "=" +clientId
                        + "&" + AUTHORIZATION_CODE + "=" +authorizationCode
                        + "&" + DEVICE_UID + "=" + deviceUid
                        + "&" + SCOPES + "=" +scopes;
                final URI consentURI = URI.create(scheme + "://" + absolutePath.getAuthority() + context + files + "/" + "consent.html" + authQueryParams);
                return Response.seeOther(consentURI).location(consentURI).build();
            }

            //Just send an ok message
            return Response.ok().build();
        } catch (Exception e) {
            authorizationServiceLogger.error("Error invoking the service login with error message " + e.getLocalizedMessage());
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.EXCEPTION).withMessage("There was an error invoking the service").build()).build();
        }
    }

    @POST
    @Path("/refresh")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshToken(@FormDataParam("access_token") final String accessToken,
                                 @FormDataParam("refresh_token") final String refreshToken,
                                 @Context JaguarSecurityContext securityContext) {
        //The access token needs to be valid when the refresh token call is made.
        final IUser userPrincipal = (IUser)securityContext.getUserPrincipal();
        if(userPrincipal == null) {
            serviceLogger.error("The user principal from the authorization header is null/invalid");
            return Response.status(HttpStatus.UNAUTHORIZED.value()).entity(ErrorMessage.builder().withErrorCode(ErrorMessage.NOT_AUTHORIZED)).build();
        }
        if(Strings.isNullOrEmpty(accessToken)) {
            serviceLogger.error("The parameter access token is required for this request");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("access_token").build()).build();
        }
        if(Strings.isNullOrEmpty(refreshToken)) {
            serviceLogger.error("The parameter refresh token is required for this request");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("refresh_token").build()).build();
        }
        //Get the user using the access token.
        final IUser authenticatedUser = getCacheManager().getUserAuthorizationCache().getIfPresent(accessToken);
        if(authenticatedUser == null) {
            serviceLogger.error("The access token does not correspond to any authenticated user");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.FREE_FORM).withMessage("The token "+accessToken+" does not correspond to an authenticated user").build()).build();
        }
        //Check for the refresh token too.
        final IUser refreshTokenUser = getCacheManager().getRefreshTokenCache().getIfPresent(refreshToken);
        if(refreshTokenUser == null) {
            serviceLogger.error("The refresh token "+refreshToken+" does not correspond to any authenticated user");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.FREE_FORM).withMessage("The refresh token "+refreshToken+" does not correspond to an authenticated user").build()).build();
        }
        //Generate a new token and save it into the cache.
        final String generatedAccessToken = UUID.randomUUID().toString();
        getCacheManager().getUserAuthorizationCache().put(generatedAccessToken,authenticatedUser);
        final String generatedRefreshToken = UUID.randomUUID().toString();
        getCacheManager().getRefreshTokenCache().put(generatedRefreshToken,authenticatedUser);
        final Map<String,String> tokenMap = ImmutableMap.<String,String>builder()
                .put("access_token",generatedAccessToken)
                .put("refresh_token",generatedRefreshToken)
                .build();
        try {
            return Response.ok().entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tokenMap)).build();
        } catch (Exception e) {
            serviceLogger.error("There was an error writing the refresh token response as JSON with exception "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
    }
}
