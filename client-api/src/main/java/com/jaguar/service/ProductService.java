package com.jaguar.service;

import com.google.common.io.ByteStreams;
import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.*;
import com.jaguar.om.impl.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.testng.util.Strings;

import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Service class taking care of creating, searching products.
 */
@Component
@Path("/product")
public class ProductService extends CommonService {


    /**
     *
     * @param title The title of the product [Is required]
     * @param description The description of the product [Is required]
     * @param price The price of the product [Is required]
     * @param buyingFormatStr The buying format for this product. [Can be Auction or Buy It Now.]
     * @param category The category under which this product needs to be listed [See {@link Category}]
     * @param currencyName The currency name under which this product is listed. [Is useful only in case this product appears under multiple countries]
     * @param securityContext The client api security context.
     * @return The created {@link Product} or an exception if there was an error creating the product.
     */
    @POST
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProduct(@FormDataParam("title") final String title,
                                  @FormDataParam("description") final String description,
                                  @FormDataParam("price") final String price,
                                  @FormDataParam("buying_format") final String buyingFormatStr,
                                  @FormDataParam("category") final String category,
                                  @FormDataParam("currency_name") final String currencyName,
                                  @Context SecurityContext securityContext) {
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
        if(Strings.isNullOrEmpty(buyingFormatStr)) {
            serviceLogger.error("Can't create a product without the buying format");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("buying_format").build()).build();
        }
        //Check if the buying format is contained in the buying format values.
        BuyingFormat buyingFormat = null;
        final BuyingFormat[] buyingFormats = BuyingFormat.values();
        for(BuyingFormat _buyingFormat :  buyingFormats) {
            if(_buyingFormat.name().equals(buyingFormatStr)) {
                buyingFormat = _buyingFormat;
                break;
            }
        }
        if(buyingFormat == null) {
            serviceLogger.error("The buying format is invalid. Expected one of "+ Arrays.asList(BuyingFormat.values())+", but found "+buyingFormatStr);
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INVALID_ARGUMENT).withMessage("buying_format","BUY_IT_NOW,AUCTION").build()).build();
        }
        IProduct product = new Product(title,description,productPrice);
        product.setCurrency(currency);
        product.setUser(user);
        product.setCategory(productCategory);
        product.setBuyingFormat(buyingFormat);
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
    @Transactional
    public Response searchProducts(@QueryParam("search_term") final String freeText,
                                   @QueryParam("advanced") final String advancedSearchFlag,
                                   @QueryParam("title") final String searchTitle,
                                   @QueryParam("description") final String description,
                                   @QueryParam("starting_price") final String startingPrice,
                                   @QueryParam("ending_price") final String endingPrice,
                                   @QueryParam("item_number") final String itemNumber,
                                   @QueryParam("buying_format") final String buyingFormat,
                                   @QueryParam("category") final String category,
                                   @QueryParam("upc") final String upc,
                                   @QueryParam("start") final String startIndex,
                                   @QueryParam("size") final String endIndex) {
        int start = -1;
        int size = -1;
        if(!Strings.isNullOrEmpty(startIndex)) {
            if(!NumberUtils.isCreatable(startIndex)) {
                serviceLogger.error("The start parameter needs to be a number");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.INVALID_ARGUMENT).withMessage("start","Integer").build()).build();
            }
            start = Integer.parseInt(startIndex);
        }
        if(start == -1) {
            start = DEFAULT_START;
        }
        if(!Strings.isNullOrEmpty(endIndex)) {
            if(!NumberUtils.isCreatable(endIndex)) {
                serviceLogger.error("The size parameter needs to be a number");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.INVALID_ARGUMENT).withMessage("size","Integer").build()).build();
            }
            size = Integer.parseInt(endIndex);
        }
        if(size == -1) {
            size = DEFAULT_SIZE;
        }
        boolean isAdvancedSearch = false;
        if(!Strings.isNullOrEmpty(advancedSearchFlag)) {
            //Make sure the advanced search flag is true or false.
            if(!advancedSearchFlag.toLowerCase().equals("true") && !advancedSearchFlag.toLowerCase().equals("false")) {
                serviceLogger.error("Invalid option specified for the advanced search flag. Expected values are true or false, but found "+advancedSearchFlag);
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.INVALID_ARGUMENT).withMessage("advanced","true or false").build()).build();
            }
            isAdvancedSearch = Boolean.parseBoolean(advancedSearchFlag);
        }

        if(!isAdvancedSearch) {
            //This is a free text search.
            if(Strings.isNullOrEmpty(freeText)) {
                if(Strings.isNullOrEmpty(freeText)) {
                    serviceLogger.info("Free text search is empty, returning 204");
                    return Response.noContent().build();
                }
            }
            try {
                final List<IProduct> productList = getProductDAO().getAllProductsByFreeText(freeText,start,size);
                if(productList == null || productList.isEmpty()) {
                    serviceLogger.info("Empty result from searching products, returning 204");
                    return Response.noContent().build();
                }
                return Response.ok().entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(productList)).build();
            } catch (Exception e) {
                serviceLogger.error("There was an error querying for products by free text with exception "+e.getLocalizedMessage());
                return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
            }
        }

        //determine what type of advanced search is this.
        if(!Strings.isNullOrEmpty(searchTitle)) {
            //search products by product title.
            final List<IProduct> productList = getProductDAO().getProductsByTitle(searchTitle,start,size);
            if(productList == null || productList.isEmpty()) {
                serviceLogger.info("Null/empty product list for the title search term "+searchTitle);
                return Response.noContent().build();
            }
            try {
                return Response.ok().entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(productList)).build();
            } catch (Exception e) {
                serviceLogger.error("Failed to write product list as a JSON with exception "+e.getLocalizedMessage());
                return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
            }
        }

        return Response.ok().build();
    }


    @POST
    @Transactional
    @Path("/{item_number}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadImage(@Context SecurityContext securityContext,
                                @PathParam("item_number") final String itemNumber,
                                @FormDataParam("image") final InputStream imageInputStream,
                                @FormDataParam("file_name") final String fileName,
                                @FormDataParam("image") final FormDataContentDisposition imageInputStreamContentDisposition) {
        final IUser user = (IUser)securityContext.getUserPrincipal();
        if(user == null) {
            serviceLogger.error("Can't proceed without authentication");
            return Response.status(HttpStatus.UNAUTHORIZED.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.NOT_AUTHORIZED).build()).build();
        }
        if(Strings.isNullOrEmpty(fileName)) {
            serviceLogger.error("The parameter file_name is required for this request");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("file_name").build()).build();
        }
        IRole sellerRole = getRoleByName("seller");
        if(sellerRole == null) {
            serviceLogger.error("The role seller does not exist");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.EXCEPTION).withMessage("The role seller does not exist").build()).build();
        }
        //Check to see if the user contains the seller role.
        IUserRole userRole = getUserRole(user,sellerRole);
        if(userRole == null) {
            serviceLogger.error("The user "+user.getEmail()+" is not a seller, cannot create or update the product with exception ");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.FREE_FORM).withMessage("The user "+user.getEmail()+" does not belong to the seller role. A seller role is required for product create/update operations").build()).build();
        }
        //Get the item using the item number.
        IProduct product = new Product(itemNumber);
        try {
            product = getDao().loadSingleFiltered(product,null,false);
            if(product == null) {
                serviceLogger.error("The product with item number "+itemNumber+" does not exist");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.NOT_FOUND).withMessage("The product with item number "+itemNumber).build()).build();
            }
        } catch (Exception e) {
            serviceLogger.error("There was an error querying the item with item number "+itemNumber+" with exception "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
        //Process the image.
        final byte[] image;
        try {
            image = ByteStreams.toByteArray(imageInputStream);
        } catch (Exception e) {
            serviceLogger.error("There was an error converting the image to a byte array with exception "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
        try {
            IImage productImage = new Image(image,product);
            productImage.setFileName(fileName);
            //Check to see if this image exists for this product.
            IImage productImageFromDB = getDao().loadSingleFiltered(productImage,null,false);
            if(productImageFromDB != null) {
                serviceLogger.info("The image "+productImage.getFileName()+" exists for the product with item number "+product.getItemNumber());
                return Response.ok().entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(productImage)).build();
            }
            //Create the product image.
            productImage = getDao().save(productImage);
            return Response.ok().entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(productImage)).build();
        } catch (Exception e) {
            serviceLogger.error("There was an error querying for the product image with the exception "+e.getLocalizedMessage());
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
    }

}
