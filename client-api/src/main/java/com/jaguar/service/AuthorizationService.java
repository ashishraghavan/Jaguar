package com.jaguar.service;

import com.google.gson.Gson;
import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.*;
import com.jaguar.om.common.EmailManager;
import com.jaguar.om.common.SMSManager;
import com.jaguar.om.impl.Device;
import com.jaguar.om.impl.DeviceUser;
import com.jaguar.om.impl.User;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.testng.util.Strings;

import javax.annotation.security.PermitAll;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;

/**
 * Takes care of authenticating a user against
 * the supplied password.
 */

@Component
@Path("/authorize")
public class AuthorizationService extends CommonService {

    private static final Logger authorizationServiceLogger = Logger.getLogger(AuthorizationService.class.getSimpleName());

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
    @Transactional(readOnly = false)
    public Response login(@FormDataParam("username") final String username,
                          @FormDataParam("password") final String password,
                          @FormDataParam("device_uid") final String deviceId,
                          @CookieParam(APP_COOKIE_NAME) final String appCookie) {
        if (Strings.isNullOrEmpty(username)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Username")
                    .build())
                    .build();
        }

        if (Strings.isNullOrEmpty(password)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Password")
                    .build())
                    .build();
        }

        if (Strings.isNullOrEmpty(appCookie)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage(APP_COOKIE_NAME).build()).build();
        }

        //Get the client id from from the verification cookie.
        //Check if the cookie is still valid.

        IApplication application = getCacheManager().getAppCache().getIfPresent(appCookie);
        if (application == null) {
            //The cookie entered was invalid.
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ErrorCode.COOKIE_NOT_VALID.getArgumentCode()).build()).build();
        }
        try {
            application = getDao().load(application.getClass(), application.getId());
            //If this application is a mobile application and no device_id was
            //supplied, it is an error.
            if (Strings.isNullOrEmpty(deviceId)) {
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
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
                        entity(ErrorMessage.builder().withErrorCode(ErrorMessage.ErrorCode.EXCEPTION.getArgumentCode())
                                .withMessage("The device " + deviceId + " doesn't seem to be a valid device").build()).build();
            }
            //Get the user now.
            IUser user = new User(application.getAccount(), username);
            user = getDao().loadSingleFiltered(user, null, false);
            if (user == null) {
                //We did not find this user under this account.
                return Response.status(HttpStatus.BAD_REQUEST.value())
                        .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.ErrorCode.NOT_FOUND.getArgumentCode())
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
                        .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.ErrorCode.NOT_FOUND.getArgumentCode())
                                .withMessage("The user " + username + " with the given password does not exist on this system").build()).build();
            }

            //Create a token
            final String token = UUID.randomUUID().toString();
            getCacheManager().getTokenCache().put(token,user);
            //Create refresh token
            final String refreshToken = UUID.randomUUID().toString();
            getCacheManager().getRefreshTokenCache().put(refreshToken,user);
            getCacheManager().getUserApplicationCache().put(user,application);
            final Map<String,Object> resultMap = ImmutableMap.<String,Object>builder().put("user",user).put("access_token",token).put("refresh_token",refreshToken).build();
            return Response.ok().entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultMap)).build();
        } catch (Exception e) {
            authorizationServiceLogger.error("Error invoking the service login with error message " + e.getLocalizedMessage());
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(new Gson().toJson(e)).build();
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
    @Transactional(readOnly = false)
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
