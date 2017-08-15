package com.jaguar.service;

import com.google.gson.Gson;
import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.IApplication;
import com.jaguar.om.enums.ApplicationType;
import com.jaguar.om.impl.Application;
import com.jaguar.om.impl.User;
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
import javax.ws.rs.core.Response;

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
     * @return a {@link Response} object wrapping the {@link User} details
     * Required form data parameters are
     * username - the username
     * password - the password
     * device_id - the id of the device the user is trying to sign in into.
     * If this is a web application, there won't be a device_id
     * We determine if this is a web application by the client id.
     * TODO: To be revisited after finishing VerificationSERv
     */
    @POST
    @PermitAll
    @Transactional(readOnly = false)
    public Response login(@FormDataParam("username") final String username,
                          @FormDataParam("password") final String password,
                          @FormDataParam("device_uid") final String deviceId,
                          @FormDataParam("client_id") final String clientId) {
        if(Strings.isNullOrEmpty(username)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(new ErrorMessage.Builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Username")
                    .build())
                    .build();
        }

        if(Strings.isNullOrEmpty(password)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(new ErrorMessage.Builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Password")
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

        if(!NumberUtils.isCreatable(clientId)) {
            authorizationServiceLogger.error("Expecting client id to be a valid number, but found "+clientId);
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(new ErrorMessage.Builder()
                    .withErrorCode(ErrorMessage.ErrorCode.INVALID_ARGUMENT.getArgumentCode())
                    .withMessage("Client Id","123..89")
                    .build())
                    .build();
        }

        final Integer clientid = Integer.parseInt(clientId);
        IApplication application = new Application(clientid);

        try {
            application = getDao().loadSingleFiltered(application,null,false);
            final ApplicationType applicationType = application.getApplicationType();
            if(applicationType == ApplicationType.MOBILE_APP) {
                //If this application is a mobile application and no device_id was
                //supplied, it is an error.
                if(Strings.isNullOrEmpty(deviceId)) {
                    return Response.status(HttpStatus.BAD_REQUEST.value()).entity(new ErrorMessage.Builder()
                            .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                            .withMessage("device_uid")
                            .build())
                            .build();
                }
                //It is possible that this might be a new device that the
                //user is trying to login into. In this case, this device
                //must be added to the table of user devices and then a
                //response must be sent back.
                return doAuthentication(username,password,clientid,deviceId);
            }

            //In case of web application, just making sure if the verification
            //succeeded and the session has not yet expired should be enough.
            return doAuthentication(username,password,clientid,null);

        } catch (Exception e) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(new Gson().toJson(e)).build();
        }

    }

    //Since there is only one authentication provider (the PL/SQL)
    //we just need to encrypt the password and match it against
    //the User table.
    //TODO: Modify authentication process. To be revisited after finishing VerificationService.
    private Response doAuthentication(final String userName,final String password,final Integer clientId,final String deviceUid) {
        return Response.ok().build();
    }
}
