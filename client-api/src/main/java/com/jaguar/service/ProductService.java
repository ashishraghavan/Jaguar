package com.jaguar.service;

import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.jersey.provider.JaguarSecurityContext;
import com.jaguar.om.*;
import com.jaguar.om.impl.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.testng.util.Strings;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Service class taking care of creating, searching products.
 */
@Component
@Path("/product")
public class ProductService extends CommonService {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProduct(@FormDataParam("title") final String title,
                                  @FormDataParam("description") final String description,
                                  @FormDataParam("price") final String price,
                                  @FormDataParam("buying_format") final String buyingFormat,
                                  @FormDataParam("category") final String category,
                                  @FormDataParam("currency_name") final String currencyName,
                                  @Context JaguarSecurityContext securityContext) {
        //Get the user first. Only user's with seller account in the role list can
        //create/list products.
        final IUser user = (IUser)securityContext.getUserPrincipal();
        if(user == null) {
            serviceLogger.error("There was an error obtaining the user from the security context");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.NOT_AUTHORIZED).build()).build();
        }
        final IUserRole userRole = new UserRole();
        IRole role = new Role("seller");
        serviceLogger.info("Getting the role seller from the database");
        try {
            role = getDao().loadSingleFiltered(role,null,false);
        } catch (Exception e) {
            serviceLogger.error("There was an error getting the role seller with exception "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
        userRole.setRole(role);
        serviceLogger.info("Getting user roles & search for the seller role");
        try {
            final List<IUserRole> userRoles = getDao().loadFiltered(userRole,null,false);
            if(userRoles == null || userRoles.isEmpty()) {
                serviceLogger.error("The user "+user.getEmail()+" is not associated with a seller role. Only sellers are allowed to create/list products");
                return Response.status(HttpStatus.UNAUTHORIZED.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.FREE_FORM).withMessage("The user "+user.getEmail()+
                                " is not associated with a seller role. Only sellers are allowed to create/list products").build()).build();
            }
        } catch (Exception e) {
            serviceLogger.error("There was an error getting the UserRole list with exception "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
        serviceLogger.info("Starting product creation");
        if(Strings.isNullOrEmpty(title)) {
            serviceLogger.error("Can't create a product without a title");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("title").build()).build();
        }
        if(Strings.isNullOrEmpty(description)) {
            serviceLogger.error("Can't create a product without a description");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("description").build()).build();
        }
        if(Strings.isNullOrEmpty(price)) {
            serviceLogger.error("Can't create a product without a price for the product");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("price").build()).build();
        }
        //Check to see if the price is a valid price.
        if(!NumberUtils.isCreatable(price)) {
            serviceLogger.error("The price "+price+" is not in the correct format");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INVALID_ARGUMENT).withMessage("price","123...").build()).build();
        }
        if(Strings.isNullOrEmpty(currencyName)) {
            serviceLogger.error("Need the currency name to set the product price");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("currency_name").build()).build();
        }
        final float productPrice = Float.parseFloat(price);
        serviceLogger.info("Setting the category");
        ICategory productCategory = new Category(category);
        productCategory.setActive(true);
        try {
            productCategory = getDao().loadSingleFiltered(productCategory,null,false);
        } catch (Exception e) {
            serviceLogger.error("There was an error obtaining the product category with the exception "+e.getLocalizedMessage());
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
        serviceLogger.info("Determining the currency");
        ICurrency currency = new Currency(currencyName);
        try {
            currency = getDao().loadSingleFiltered(currency,null,false);
        } catch (Exception e) {
            serviceLogger.error("There was an error querying/determining the currency with the supplied currency name "+currencyName);
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
        IProduct product = new Product(title,description,productPrice);
        product.setCurrency(currency);
        product.setUser(user);
        product.setCategory(productCategory);
        try {
            product = getDao().save(product);
            return Response.accepted().entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(product)).build();
        } catch (Exception e) {
            serviceLogger.error("There was an error saving the product with the exception "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchProducts(@QueryParam("advanced") final String isAdvancedSearch) {
        return Response.ok().build();
    }

}
