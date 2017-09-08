package com.jaguar.service;


import com.jaguar.common.CommonService;
import org.springframework.stereotype.Component;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Component
@Path("/version")
public class VersionService extends CommonService{

    @GET
    @PermitAll
    public Response showVersion() {
        return Response.ok().entity("It works").build();
    }
}
