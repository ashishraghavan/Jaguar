package com.jaguar.service;


import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.GenericDaoException;
import com.jaguar.om.IApplication;
import com.jaguar.om.impl.Application;
import jersey.repackaged.com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.testng.util.Strings;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

@Component
@Path("/version")
public class VersionService extends CommonService {

    private static final Logger versionServiceLogger = Logger.getLogger(VersionService.class.getSimpleName());
    private static final String ipUrl = "http://checkip.amazonaws.com";
    //TODO Get the version information from a META-INF file.
    private static final Map<String,String> versionMap = ImmutableMap.<String,String>builder().put("Version","1.0")
            .put("Application Name","Jaguar").build();

    @GET
    @PermitAll
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_HTML})
    public Response showVersion() {
        try {
            return Response.ok().entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(versionMap)).build();
        } catch (Exception e) {
            serviceLogger.error("There was an error getting/serializing the version information.");
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
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

    @POST
    @PermitAll
    @Path("/upload")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_HTML})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadProject(final @FormDataParam("project_file") InputStream projectZip,
                                  final @FormDataParam("prefix") String prefixFromClient,
                                  final @FormDataParam("suffix") String suffixFromClient,
                              final @FormDataParam("project_file") FormDataContentDisposition projectZipContentDisposition) {
        if(projectZip == null) {
            try {
                return Response.status(HttpStatus.BAD_REQUEST.value())
                        .entity(mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString("Project zip is required as a form parameter")).build();
            } catch (Exception e) {
                serviceLogger.error("There was an error sending the error response with message "+e.getLocalizedMessage());
            }
            serviceLogger.info("Sending the response as plain text");
            return Response.status(HttpStatus.BAD_REQUEST.value())
                    .entity("Project zip is requried as a form parameter").build();
        }
        String prefix;
        String suffix;
        if(!Strings.isNullOrEmpty(prefixFromClient)) {
            prefix = prefixFromClient;
        } else {
            prefix = "prefix";
        }
        if(!Strings.isNullOrEmpty(suffixFromClient)) {
            suffix = suffixFromClient;
        } else {
            suffix = "suffix";
        }
        //Save the project zip in a temporary location.
        try {
            final File tempProjectFile = File.createTempFile(prefix,"." +suffix);
            FileCopyUtils.copy(projectZip,new FileOutputStream(tempProjectFile));
            serviceLogger.info("The path to the temporary zip file is "+tempProjectFile.getAbsolutePath());
            return Response.ok().build();
        } catch (Exception e) {
            serviceLogger.error("There was an error creating the temporary file with messsage "+e.getLocalizedMessage());
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity("There was an error creating the temporary zip project").build();
        }
    }

}
