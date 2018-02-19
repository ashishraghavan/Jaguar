package com.jaguar.service;

import com.jaguar.common.CommonService;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/cart")
public class CartService extends CommonService {

    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCart(@FormDataParam("product_id") final String productId,
                               @FormDataParam("item_number") final String itemNumber,
                               @FormDataParam("quantity") final int quantity) {
        return Response.ok().build();
    }
}
