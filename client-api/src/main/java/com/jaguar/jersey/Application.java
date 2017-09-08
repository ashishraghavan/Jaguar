package com.jaguar.jersey;

import com.jaguar.service.ApplicationService;
import com.jaguar.service.AuthenticationService;
import com.jaguar.service.OAuth2Service;
import com.jaguar.service.VersionService;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.springframework.web.filter.RequestContextFilter;

import javax.ws.rs.ApplicationPath;


/**
 * The jersey application class
 * required as the init param
 * for a servlet.
 */
@ApplicationPath("/api")
public class Application extends ResourceConfig {

    public Application(){
        register(MvcFeature.class);
        register(RequestContextFilter.class);
        register(VersionService.class);
        register(ApplicationService.class);
        register(AuthenticationService.class);
        register(OAuth2Service.class);
        register(MultiPartFeature.class);
    }
}
