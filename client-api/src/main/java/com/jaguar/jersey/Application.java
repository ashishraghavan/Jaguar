package com.jaguar.jersey;

import com.jaguar.service.VersionService;
import org.glassfish.jersey.server.ResourceConfig;


/**
 * The jersey application class
 * required as the init param
 * for a servlet.
 */
public class Application extends ResourceConfig {

    public Application(){
        register(VersionService.class);
    }
}
