package com.jaguar.jersey;

import com.jaguar.service.*;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.MvcFeature;
import org.springframework.web.filter.RequestContextFilter;


/**
 * The jersey application class
 * required as the init param
 * for a servlet.
 */
public class Application extends ResourceConfig {

    public Application(){
        register(MvcFeature.class);
        register(RequestContextFilter.class);
        register(VersionService.class);
        register(ApplicationService.class);
        register(AuthenticationService.class);
        register(OAuth2Service.class);
        register(MultiPartFeature.class);
        register(UserService.class);
        register(ProductService.class);
        register(CartService.class);
    }
}
