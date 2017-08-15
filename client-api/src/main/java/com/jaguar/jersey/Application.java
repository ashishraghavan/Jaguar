package com.jaguar.jersey;

import com.jaguar.service.ApplicationService;
import com.jaguar.service.AuthorizationService;
import com.jaguar.service.OAuth2Service;
import com.jaguar.service.VersionService;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;


/**
 * The jersey application class
 * required as the init param
 * for a servlet.
 */
public class Application extends ResourceConfig {

    public Application(){
        register(VersionService.class);
        register(ApplicationService.class);
        register(AuthorizationService.class);
        register(OAuth2Service.class);
        register(MultiPartFeature.class);
    }
}
