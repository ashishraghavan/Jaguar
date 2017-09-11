package com.jaguar.service;

import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.*;
import com.jaguar.om.common.EmailManager;
import com.jaguar.om.common.SMSManager;
import com.jaguar.om.impl.Application;
import com.jaguar.om.impl.Device;
import com.jaguar.om.impl.DeviceUser;
import com.jaguar.om.impl.User;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
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
@Path("/authorize")
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
     * TODO: To be revisited after finishing VerificationSERv
     */
    @Path("/login")
    @POST
    @PermitAll
    @Transactional
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_HTML})
    public Response login(@Context final ContainerRequestContext requestContext,
                          @FormDataParam("username") final String username,
                          @FormDataParam("password") final String password,
                          @FormDataParam("device_uid") final String deviceId,
                          @FormDataParam("auth_flow") final String isAuthFlow,
                          @FormDataParam("redirect_uri") final String redirectUri,
                          @FormDataParam("client_id") final String clientIdStr) {
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
            if (Strings.isNullOrEmpty(deviceId)) {
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED)
                        .withMessage("device_uid")
                        .build())
                        .build();
            }
            //Get the device first.
            IDevice device = new Device(application.getAccount(), deviceId);
            device = getDao().loadSingleFiltered(device, null, false);
            if (device == null) {
                //There are two cases. Either this user is logging in
                //using a different device, or this user hasn't registered yet.
                //Since the device is null, this device is non-existent.
                return Response.status(HttpStatus.BAD_REQUEST.value()).
                        entity(ErrorMessage.builder().withErrorCode(ErrorMessage.EXCEPTION)
                                .withMessage("The device " + deviceId + " doesn't seem to be a valid device").build()).build();
            }
            //Get the user now.
            IUser user = new User(application.getAccount(), username);
            user = getDao().loadSingleFiltered(user, null, false);
            if (user == null) {
                //We did not find this user under this account.
                return Response.status(HttpStatus.BAD_REQUEST.value())
                        .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.NOT_FOUND)
                                .withMessage("The device with id " + deviceId).build()).build();
            }
            //Check the UserDevice table to see if we find a correct user -> device match.
            IDeviceUser deviceUser = new DeviceUser(device, user);
            deviceUser.setAccount(application.getAccount());
            final IDeviceUser deviceUserFromDB = getDao().loadSingleFiltered(deviceUser, null, false);
            if (deviceUserFromDB == null) {
                //This device has not been assoicated with this user.
                //The user is probably logging in with a different device
                //for the same application.
                //Before saving this device user, we have to make sure, it is
                //indeed this user who is signing through a separate device.
                //Maybe generate a one time passcode and send to their provided phone number.
                final INotificationManager notificationManager;
                if (!Strings.isNullOrEmpty(user.getPhoneNumber())) {
                    //We prefer to send an SMS if we have the phone number.
                    //construct the SMS message.
                    notificationManager = new SMSManager();
                } else {
                    //Send an email through our SMTP server.
                    //Construct the EMail message.
                    notificationManager = new EmailManager();
                }
                final Object dummyObject = new Object();
                notificationManager.sendNotificationMessage(dummyObject);
                //Once we have verified, create any entry in the UserDevice table.
                getDao().save(deviceUser);
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

            //If this is the authorization flow (consent), we send the user to the redirection URI
            //with the authorization code appended to the URI.
            if(isConsentFlow) {
                //We need the re-redirect URI for this case.
                if(Strings.isNullOrEmpty(redirectUri)) {
                    return Response.status(HttpStatus.BAD_REQUEST.value())
                            .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("Redirect URI").build()).build();
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
                        + "&" + DEVICE_UID + "=" +deviceId;
                final URI baseUri = URI.create(absolutePath.getScheme() + "://" + absolutePath.getAuthority() + "/" + "consent.html" + authQueryParams);
                final Map<String,Object> response = ImmutableMap.<String,Object>builder().put("Location",baseUri.toString()).build();
                return Response.ok(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response)).build();
            }

            //Just send an ok message
            return Response.ok().build();
        } catch (Exception e) {
            authorizationServiceLogger.error("Error invoking the service login with error message " + e.getLocalizedMessage());
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(wrapExceptionForEntity(e)).build();
        }
    }

    /**
     * Does the new user registration.
     * @param username The email for this user
     * @param password The password in plain text for this user.
     * @param firstName The first name.
     * @param lastName The last name.
     * @param phone The phone number [is optional]
     * @param deviceUid The unique device id of the user. Has to be generated from the client side.
     * @param model The model of the device.
     * @param api THe ANDROID api version of the device. Minimum supported version is from 15.
     * @param notificationServiceId The notification service id of the device if available.
     * @return {@link HttpStatus#OK} is the user registered successfully, {@link HttpStatus#BAD_REQUEST} with the reason.
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @PermitAll
    public Response registerUser(final @FormDataParam("username") String username,
                                 final @FormDataParam("password") String password,
                                 final @FormDataParam("first_name") String firstName,
                                 final @FormDataParam("last_name") String lastName,
                                 final @FormDataParam("phone") String phone,
                                 final @FormDataParam("device_uid") String deviceUid,
                                 final @FormDataParam("model") String model,
                                 final @FormDataParam("ap_version") String api,
                                 final @FormDataParam("notification_service_id") String notificationServiceId) {
        return Response.ok().build();
    }
}
