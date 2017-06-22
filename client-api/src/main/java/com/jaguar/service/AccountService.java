package com.jaguar.service;


import com.jaguar.common.CommonService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Component
@Path("/account")
public class AccountService extends CommonService {

    @POST
    @Transactional(readOnly = false)
    public Response createAccount() {
        try {
            return Response.ok("It works").build();
        } catch (Exception e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }
}
