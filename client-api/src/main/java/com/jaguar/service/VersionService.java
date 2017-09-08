package com.jaguar.service;


import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.GenericDaoException;
import com.jaguar.om.IApplication;
import com.jaguar.om.impl.Application;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.testng.util.Strings;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

@Component
@Path("/version")
public class VersionService extends CommonService {

    private static final Logger versionServiceLogger = Logger.getLogger(VersionService.class.getSimpleName());
    private static final String ipUrl = "http://checkip.amazonaws.com";

    @GET
    @PermitAll
    public Response showVersion() {
        return Response.ok().entity("It works").build();
    }

    @GET
    @Path("/ip")
    @PermitAll
    @Transactional
    public Response getExternalIP(@QueryParam("client_id") final String clientIdStr) {
        if (Strings.isNullOrEmpty(clientIdStr)) {
            versionServiceLogger.error("Null/empty client id encountered, aborting");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INVALID_ARGUMENT).withMessage("Client id", "123...").build()).build();
        }
        try {
            if (!NumberUtils.isCreatable(clientIdStr)) {
                versionServiceLogger.error("The client id " + clientIdStr + " is not in the correct format");
                return Response.status(HttpStatus.BAD_REQUEST.value())
                        .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.INVALID_ARGUMENT)
                                .withMessage("Client id", "123....").build()).build();
            }
            final int clientId = Integer.valueOf(clientIdStr);
            IApplication application = new Application(clientId);
            application = getDao().loadSingleFiltered(application, null, false);
            if (application == null) {
                versionServiceLogger.error("The call to get the IP address is only for valid applications");
                return Response.status(HttpStatus.SERVICE_UNAVAILABLE.value()).build();
            }
            final URL whatIsMyIp = new URL(ipUrl);
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(whatIsMyIp.openStream()));
            return Response.ok().entity(bufferedReader.readLine()).build();
        } catch (Exception e) {
            if (e instanceof IOException) {
                if (e instanceof MalformedURLException) {
                    versionServiceLogger.error("The URL " + ipUrl + " is not correct or malformed");
                    return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                            .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).
                                    withMessage("The url " + ipUrl + " was not well formed").build()).build();
                }
                versionServiceLogger.error("The service at the address " + ipUrl + " is probably not running. It has failed with the message " + e.getLocalizedMessage());
                return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR)
                                .withMessage("The service at " + ipUrl + " is not available. Can't find the IP address at this time").build()).build();
            }
            if (e instanceof GenericDaoException) {
                versionServiceLogger.error("THe query to get the application with client id " + clientIdStr + " resulted in an error with message " + e.getLocalizedMessage());
                return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder().withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR)
                        .withMessage("THe query to get the application with client id " + clientIdStr + " resulted in an error with message " + e.getLocalizedMessage()).build()).build();
            }
            versionServiceLogger.error("An unknown error occurred with the message " + e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR)
                    .withMessage("An unknown error occurred on our side").build()).build();
        }
    }
}
