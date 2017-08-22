package com.jaguar.service;


import com.google.gson.Gson;
import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.IApplication;
import com.jaguar.om.IBaseDAO;
import com.jaguar.om.enums.ApplicationType;
import com.jaguar.om.impl.Application;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.testng.util.Strings;

import javax.annotation.security.PermitAll;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.UUID;

@Path("/apps")
@Component
public class ApplicationService extends CommonService {

    private static final Logger appServiceLogger = Logger.getLogger(ApplicationService.class.getSimpleName());
    final String APP_COOKIE_NAME = "jaguar_cookie";

    @POST
    @Path("/verify")
    @PermitAll
    @Transactional(readOnly = false)
    public Response verify(final @Context ContainerRequestContext requestContext,
                           final @Context UriInfo uriInfo,
                           @FormDataParam("time") final String currentTime,
                           @FormDataParam("client_id") final String clientId,
                           @FormDataParam("hash") final String computedHash) {
        if(Strings.isNullOrEmpty(currentTime)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(new ErrorMessage.Builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Time")
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

        if(Strings.isNullOrEmpty(computedHash)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(new ErrorMessage.Builder()
                    .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                    .withMessage("Computed Hash")
                    .build())
                    .build();
        }

        if(!NumberUtils.isCreatable(clientId)) {
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(new ErrorMessage.Builder()
                    .withErrorCode(ErrorMessage.ErrorCode.INVALID_ARGUMENT.getArgumentCode())
                    .withMessage("Client Id", "123...9")
                    .build())
                    .build();
        }

        final Integer clientid = Integer.parseInt(clientId);
        //Get the application secret from the application.
        IApplication application = new Application(clientid);
        try {
            application = getDao().loadSingleFiltered(application,null,false);
            if(application == null) {
                return Response.status(HttpStatus.BAD_REQUEST.value())
                        .entity(new ErrorMessage.Builder().withErrorCode(ErrorMessage.ErrorCode.NULL_OBJECT_FROM_QUERY.getArgumentCode())
                                .withMessage("application with client id " + clientId)
                                .build())
                        .build();
            }
            final String appSecret = application.getClientSecret();
            //See if the computed hash the client is sending us is
            //the same as the one computed by us.
            if(!doComputeAndCompare(currentTime,appSecret,computedHash)) {
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(new ErrorMessage.Builder()
                        .withErrorCode(ErrorMessage.ErrorCode.INVALID_HASH.getArgumentCode())
                        .withMessage(computedHash)
                        .build())
                        .build();
            }

            //Create a random app session value.
            final String appCookieSession = UUID.randomUUID().toString();
            //Get application converted to JSON object
            final String appGson = gson.toJson(application);
            //Store this cookie session value in an in-memory cache so that
            //during verification, we know that the cookie is coming from the correct client application.
            //If an application is already present with this app session cookie, we overwrite the value since the application
            //might be verifying for a new session.
            cacheManager.getAppCache().put(appCookieSession,application);
            final NewCookie cookie = new NewCookie(APP_COOKIE_NAME,
                    appCookieSession,
                    uriInfo.getPath(),
                    "",
                    "cookie for creating app session",
                    100,
                    false);
            //Add this session somewhere in the in-memory cache.
            return Response.ok().cookie(cookie).entity(appGson).build();

        } catch (Exception e) {
            appServiceLogger.error("Failed to query/filter application with error "+e.getLocalizedMessage());
            return Response.status(HttpStatus.BAD_REQUEST.value())
                    .entity(new ErrorMessage.Builder()
                            .withErrorCode(ErrorMessage.ErrorCode.EXCEPTION.getArgumentCode())
                            .withMessage(e.getLocalizedMessage())
                    ).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @Transactional(readOnly = true)
    public Response isVerificationValid(@CookieParam(APP_COOKIE_NAME) final String appSessionCookie) {
        if(Strings.isNullOrEmpty(appSessionCookie)) {
            appServiceLogger.error("An application session cookie is required for this request");
            return Response.status(HttpStatus.BAD_REQUEST.value())
                    .entity(new ErrorMessage.Builder()
                            .withErrorCode(ErrorMessage.ErrorCode.ARGUMENT_REQUIRED.getArgumentCode())
                            .withMessage("Cookie param "+APP_COOKIE_NAME).build()).build();
        }
        if(cacheManager.getAppCache().getIfPresent(appSessionCookie) != null) {
            //This session is still valid.
            return Response.ok().build();
        }
        return Response.status(HttpStatus.BAD_REQUEST.value()).entity("Invalid app session. Please re-verify").build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(readOnly = true)
    public Response getAllApplications() {
        final IBaseDAO dao = getDao();
        try {
            final IApplication filterApplication = new Application();
            filterApplication.setActive(true);
            filterApplication.setApplicationType(ApplicationType.MOBILE_APP);
            final List<IApplication> applicationList = dao.loadFiltered(filterApplication,false);
            return Response.ok().entity(new Gson().toJson(applicationList)).build();
        } catch (Exception e) {
            appServiceLogger.error("There was an error processing this request with the message "+e.getMessage()+" and cause "+e.getCause());
            return Response.status(HttpStatus.BAD_REQUEST.value())
                    .entity(new ErrorMessage.Builder()
                            .withErrorCode(ErrorMessage.ErrorCode.EXCEPTION.getArgumentCode())
                            .withMessage(e.getMessage())
                            .build())
                    .build();
        }
    }

    /**
     *
     * @param currentTime The current time or the computed time that was sent by the client
     * @param clientSecret The client secret obtained from the application.
     * @param computedHash The hash as computed by the client.
     * @return True if the computed hash sent by the client matches the one computed by us, false otherwise.
     */
    private boolean doComputeAndCompare(final String currentTime,
                                        final String clientSecret,
                                        final String computedHash) throws Exception {
        final Mac hmac = Mac.getInstance("HmacSHA256");
        final SecretKeySpec secretKeySpec = new SecretKeySpec(clientSecret.getBytes(),"HmacSHA256");
        hmac.init(secretKeySpec);
        return Base64.encodeBase64String(hmac.doFinal(currentTime.getBytes())).equals(computedHash);
    }
}
