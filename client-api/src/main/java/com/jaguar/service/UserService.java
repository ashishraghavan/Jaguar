package com.jaguar.service;

import com.jaguar.common.CommonService;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.*;
import com.jaguar.om.enums.ApplicationType;
import com.jaguar.om.impl.*;
import com.jaguar.om.notification.Email;
import com.jaguar.om.notification.EmailManager;
import org.apache.commons.lang3.math.NumberUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.testng.util.Strings;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.UUID;

@Path("/user")
@Component
public class UserService extends CommonService {

    private IEmailManager emailManager;

    @Autowired
    public void setEmailManager(IEmailManager emailManager) {
        this.emailManager = emailManager;
    }
    /**
     * Does the new user registration.
     * @param username The email for this user
     * @param password The password in plain text for this user.
     * @param roleName The role that this user is registering for. Atleast one role should be selected.
     * @param firstName The first name.
     * @param lastName The last name.
     * @param phone The phone number [is optional]
     * @param deviceUid The unique device id of the user. Has to be generated from the client side.
     * @param model The model of the device.
     * @param api THe ANDROID api version of the device. Minimum supported version is from 15.
     * @param notificationServiceId The notification service id of the device if available.
     * @return {@link HttpStatus#OK} is the user registered successfully, {@link HttpStatus#BAD_REQUEST} with the reason.
     *
     * curl -v -X POST -F "username=ashish13687@gmail.com" -F "password=12345" -F "role=seller" -F "first_name=Ashish" -F "last_name=Raghavan"
     *                 -F "phone=4082216275" -F "device_uid=iOS6sPlus-A1687" -F "model=iPhone6sPlus" -F "client_id="
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @PermitAll
    public Response registerUser(final @FormDataParam("username") String username,
                                 final @FormDataParam("password") String password,
                                 final @FormDataParam("role") String roleName,
                                 final @FormDataParam("first_name") String firstName,
                                 final @FormDataParam("last_name") String lastName,
                                 final @FormDataParam("phone") String phone,
                                 final @FormDataParam("device_uid") String deviceUid,
                                 final @FormDataParam("model") String model,
                                 final @FormDataParam("client_id") String clientIdStr,
                                 final @FormDataParam("api") String api,
                                 final @FormDataParam("notification_service_id") String notificationServiceId,
                                 final @Context ContainerRequestContext requestContext) {

        //These are the basic validation fields.
        if(Strings.isNullOrEmpty(username)) {
            serviceLogger.error("Username parameter not specified");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("username").build()).build();
        }

        if(Strings.isNullOrEmpty(password)) {
            serviceLogger.error("Password parameter not specified");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("password").build()).build();
        }

        if(Strings.isNullOrEmpty(roleName)) {
            serviceLogger.error("Role parameter not specified");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("role").build()).build();
        }

        if(Strings.isNullOrEmpty(deviceUid)) {
            serviceLogger.error("device_uid parameter not specified");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("device_uid").build()).build();
        }

        if(Strings.isNullOrEmpty(clientIdStr)) {
            serviceLogger.error("client_id parameter not specified");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("client_id").build()).build();
        }

        if(!NumberUtils.isCreatable(clientIdStr)) {
            serviceLogger.error("The client id is not a number");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.EXCEPTION).withMessage("The client id is not a number").build()).build();
        }

        //Get the application now.
        final int clientId = Integer.parseInt(clientIdStr);
        IApplication application = new Application(clientId);
        try {
            application = getDao().loadSingleFiltered(application,null,false);
            if(application == null) {
                serviceLogger.error("There is no application with the client id "+clientIdStr);
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.INVALID_ARGUMENT).withMessage("Client id -"+clientIdStr).build()).build();
            }
            if(application.getApplicationType() == ApplicationType.MOBILE_APP) {
                if(Strings.isNullOrEmpty(model)) {
                    serviceLogger.error("The device model parameter not specified");
                    return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                            .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("model").build()).build();
                }
                if(Strings.isNullOrEmpty(api)) {
                    serviceLogger.error("The api version parameter has not been specified");
                    return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                            .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("The argument api").build()).build();
                }
                //Check if api is in the correct format.
                if(!NumberUtils.isCreatable(api)) {
                    serviceLogger.error("The api version parameter is not a number");
                    return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                            .withErrorCode(ErrorMessage.INVALID_ARGUMENT).withMessage("api","123...").build()).build();
                }
            }
            //Start the process of creating the user.
            //Check if this user exists.
            //A user is always scoped to an account.
            IUser user = new User(application.getAccount(),username);
            IUser userFromDB = getDao().loadSingleFiltered(user,null,false);
            //TODO load the devices field too with this query.
            boolean isUserPresent = false;
            if(userFromDB != null) {
                serviceLogger.info("User with email "+username+" already present for account "+application.getAccount().getAccountName()+", maybe this user is trying to add a device to this username?");
                user = userFromDB;
                isUserPresent = true;
            }
            //First create the device.
            IDevice device = new Device(application.getAccount(),deviceUid);
            device.setActive(true);
            IDevice deviceFromDB = getDao().loadSingleFiltered(device,null,false);
            //If the user and device are present, we should not continue.
            if(deviceFromDB != null && isUserPresent) {
                serviceLogger.info("We are creating duplicate mapping for user -> device. This is an error");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.FREE_FORM).withMessage("The user "+username+" is already mapped to the device " +
                                "with id "+deviceUid).build()).build();
            }
            device.setModel(model);
            device.setApiVersion(Integer.parseInt(api));
            //Set this notification service id on the device
            //for sending notifications to this device.
            if(!Strings.isNullOrEmpty(notificationServiceId)) {
                device.setNotificationServiceId(notificationServiceId);
            }
            //We have to check if this user is registering a new device.
            //If the user does not exist, then we are sure that a DeviceUser entry would not
            //exist.
            IDeviceUser deviceUser = new DeviceUser(device,user);
            deviceUser.setActive(false);
            IDeviceUser deviceUserFromDb = getDao().loadSingleFiltered(deviceUser,null,false);
            //If there is an entry in the DeviceUser table, it means this user has already registered using this device.
            //If we can find the user, then there most definitely needs to be an entry in the DeviceUser table as a user
            //cannot register without a device.
            if(deviceUserFromDb != null) {
                serviceLogger.error("The user "+user.getEmail()+" has already registered on the device with uid "+device.getDeviceUId());
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.EXCEPTION).withMessage("The user "+user.getEmail()+" has already registered on the device with uid "
                                +device.getDeviceUId()).build()).build();
            }
            //Before attempting to save the DeviceUser, we have to save the User and the Device,
            //since the device_id and user_id are being used to get the DeviceUser.
            //Set user fields if the user was not already present.
            if(!isUserPresent) {
                serviceLogger.info("Starting to create user");
                if(!Strings.isNullOrEmpty(firstName)) {
                    user.setFirstName(firstName);
                }
                if(!Strings.isNullOrEmpty(lastName)) {
                    user.setLastName(lastName);
                }
                if(!Strings.isNullOrEmpty(phone)) {
                    user.setPhoneNumber(phone);
                }
                final String fullName = (Strings.isNullOrEmpty(user.getFirstName()) ? "" : user.getFirstName().trim()) +
                        (Strings.isNullOrEmpty(user.getLastName()) ? "" : user.getLastName().trim());
                user.setName(fullName);
                user.setPassword(password);
                //Set the user active to false until the user confirms by using either the phone or email
                //method of verification.
                user.setActive(false);
                user = getDao().save(user);
            }
            //Set the role for this user.
            //Check if this role exists first
            IRole role = new Role(roleName);
            role = getDao().loadSingleFiltered(role,null,false);
            if(role == null) {
                serviceLogger.error("The role "+roleName+" is invalid");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.FREE_FORM).withMessage("The role "+roleName+" is invalid").build()).build();
            }
            //Now check if this user role exists.
            IUserRole userRole = new UserRole(user,role);
            //We don't want an inactive role coming up during search.
            userRole.setActive(true);
            IUserRole userRoleFromDB = getDao().loadSingleFiltered(userRole,null,false);
            if(userRoleFromDB == null) {
                serviceLogger.info("Creating a UserRole object because it does not exist in the DB.");
                getDao().save(userRole);
            }
            //Now do all operations for the device.
            device.setActive(false);
            device.setUser(user);
            device = getDao().save(device);
            //Save a record for the DeviceUser table.
            //make sure we set the latest device for the DeviceUser. If we don't do this, we end up using the transient database entity.
            deviceUser.setDevice(device);
            //make sure we set the latest user for the DeviceUser.
            deviceUser.setUser(user);
            //save the device user combination but with active flag set to false.
            getDao().save(deviceUser);
            //If the user has a phone number, use that for verification. If not, use email.
            //For now, we are using only email to send verification links. Once, the SMS
            //gateway is setup, we will start sending verification codes to phone numbers provided.
            //Generate a link first.
            final String verificationCode = UUID.randomUUID().toString();
            //Add this code to the cache <code,email>
            //This code will be valid only for 10 minutes. i.e the user has to click this
            //verification link within 10 minutes after they register.
            final String verificationUri = requestContext.getUriInfo().getBaseUri()
                    + "user/verify?email="+user.getEmail()+ "&" +"code=" + verificationCode + "&" + "device_uid="+device.getDeviceUId() + "&" + "role="+roleName;
            final Email email = EmailManager.emailBuilder().subject("Verify your registration")
                    .body(String.format(VERIFICATION_EMAIL,verificationUri)).to(user.getEmail()).build();
            emailManager.sendEmail(email);
            //Only after we have sent the email, we add the code to the in memory cache.
            getCacheManager().getUserVerificationCache().put(verificationCode,user);
            return Response.ok(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user)).build();
        } catch (Exception e) {
            serviceLogger.error("There was error querying the application using the client id "+clientIdStr+" with exception "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR)).build();
        }
    }

    @Path("/verify")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_HTML})
    @Transactional
    @PermitAll
    public Response verifyUserRegistration(final @QueryParam("email") String email,
                                           final @QueryParam("code") String code,
                                           final @QueryParam("device_uid") String deviceUId,
                                           final @QueryParam("role") String roleName) {
        //Sanity checks. Any missing query parameter will result in an internal server error.
        if(Strings.isNullOrEmpty(email)) {
            serviceLogger.error("The parameter email was not specified for this request. This is most likely due to an internal server error during the registration call or a programming error.");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("email").build()).build();
        }

        if(Strings.isNullOrEmpty(code)) {
            serviceLogger.error("The parameter code was not specified for this request. This is most likely due to an internal server error during the registration call or a programming error");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("code").build()).build();
        }

        if(Strings.isNullOrEmpty(deviceUId)) {
            serviceLogger.error("The parameter deviceUId was not specified for this request. This is most likely due to an internal server error during the registration call or a programming error");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("device_uid").build()).build();
        }

        if(Strings.isNullOrEmpty(roleName)) {
            serviceLogger.error("The parameter role was not specified for this request. This is most likely due to an internal server error during the registration call or a programming error");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("role").build()).build();
        }

        //Check if the authorization code generated during the previous step is still valid.
        IUser verifiedUser = getCacheManager().getUserVerificationCache().getIfPresent(code);
        if(verifiedUser == null) {
            serviceLogger.error("The authorization code has expired.");
            return Response.status(HttpStatus.UNAUTHORIZED.value()).entity(ErrorMessage.builder().withErrorCode(ErrorMessage.LINK_EXPIRED).build()).build();
        }
        try {
            //Save the user first.
            verifiedUser.setActive(true);
            verifiedUser = getDao().save(verifiedUser);
            //Check if the device user from the DeviceUser table is present.
            IDevice device = new Device(deviceUId,verifiedUser);
            device.setActive(false);
            //Save the device now.
            device = getDao().loadSingleFiltered(device,null,false);
            if(device == null) {
                serviceLogger.error("The device id from the query parameter is not correct. This should not have happened!");
                return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).withMessage(INTERNAL_SERVER_ERROR_MSG).build()).build();
            }
            //save the device
            device.setActive(true);
            device = getDao().save(device);
            IDeviceUser deviceUser = new DeviceUser(device,verifiedUser);
            deviceUser.setActive(false);
            deviceUser = getDao().loadSingleFiltered(deviceUser,null,false);
            if(deviceUser == null) {
                serviceLogger.error("The device user entry was not found in the DeviceUser table. This should not have happened!");
                return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).withMessage(INTERNAL_SERVER_ERROR_MSG).build()).build();
            }
            //finally save the device user.
            deviceUser.setActive(true);
            deviceUser = getDao().save(deviceUser);
            return Response.ok().entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(deviceUser)).build();
        } catch (Exception e) {
            serviceLogger.error("An error occurred while querying for either DeviceUser or User with exception message "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
    }

    /***
     * This API allows the client application to resend the email verification link if it has expired.
     * @param userName The email/username of the client as a query param.
     * @param deviceId The {@link Device#deviceUid} of the user as a query param.
     * @return {@link Response#ok()} with the link sent to the email of this user, an error otherwise.
     * curl -v "http://localhost:8080/api/user/resendlink?email=ashishraghavan13687@gmail.com&device_uid=iOS6sPlus-A1687&client_id=1095369"
     */
    //TODO : Write logic to prevent more than 3 requests with 15 minutes.
    @Path("/resendlink")
    @Produces({MediaType.APPLICATION_JSON,MediaType.TEXT_HTML})
    @Transactional
    @PermitAll
    public Response resendEmailVerificationLink(final @QueryParam("email") String userName,
                                                final @QueryParam("device_uid") String deviceId,
                                                final @QueryParam("client_id") String clientIdStr,
                                                final @QueryParam("role") String roleName,
                                                final @Context ContainerRequestContext requestContext) {
        if(Strings.isNullOrEmpty(userName)) {
            serviceLogger.error("No email parameter was received in the query. This should not have happened!");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("email").build()).build();
        }
        if(Strings.isNullOrEmpty(deviceId)) {
            serviceLogger.error("No device id was received in the query. This should not have happened!");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("device_uid").build()).build();
        }
        if(Strings.isNullOrEmpty(clientIdStr) || !NumberUtils.isCreatable(clientIdStr)) {
            serviceLogger.error("No client id was received in the query or the format for the client id was incorrect. This should not have happened!");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.ARGUMENT_REQUIRED).withMessage("client_id").build()).build();
        }
        final URI uri = requestContext.getUriInfo().getBaseUri();
        try {
            IApplication application = new Application(Integer.parseInt(clientIdStr));
            application = getDao().loadSingleFiltered(application,null,false);
            if(application == null) {
                serviceLogger.error("The application with the client id "+clientIdStr+" does not exist");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder().withErrorCode(ErrorMessage.EXCEPTION)
                        .withMessage("There is no application with the client id "+clientIdStr).build()).build();
            }
            //We need to query only for user's that this application belongs to.
            IUser user = new User(application.getAccount(),userName);
            user.setActive(false);
            user = getDao().loadSingleFiltered(user,null,false);
            if(user == null) {
                serviceLogger.error("There is not user with the user name "+userName+ " for the account ");
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder().withErrorCode(ErrorMessage.EXCEPTION)
                        .withMessage("There is no user with the username "+userName).build()).build();
            }
            IDevice device = new Device(application.getAccount(),deviceId);
            device.setActive(false);
            device = getDao().loadSingleFiltered(device,null,false);
            if(device == null) {
                serviceLogger.error("There is no device with the device uid "+deviceId+" for the account "+application.getAccount().getAccountName());
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder().withErrorCode(ErrorMessage.EXCEPTION)
                        .withMessage("There is no device with the device id "+deviceId+" and with the account "+application.getAccount().getAccountName()).build()).build();
            }
            //Now get the DeviceUser.
            IDeviceUser deviceUser = new DeviceUser(device,user);
            deviceUser.setActive(false);
            deviceUser = getDao().loadSingleFiltered(deviceUser,null,false);
            if(deviceUser == null) {
                serviceLogger.error("There  is no combination of the device "+deviceId+" and the user "+userName);
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder().withErrorCode(ErrorMessage.EXCEPTION)
                        .withMessage("There is no combination of the device with id "+deviceId+" and the user "+userName).build()).build();
            }
            //Verify if the user does belong to the role requested.
            final IRole role  = getRoleByName(roleName);
            if(role == null) {
                return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.NOT_FOUND).withMessage(roleName).build()).build();
            }
            final String verificationCode = UUID.randomUUID().toString();
            //Add this code to the cache <code,email>
            //This code will be valid only for 10 minutes. i.e the user has to click this
            //verification link within 10 minutes after they register.
            final String verificationUri = uri
                    + "user/verify?email="+user.getEmail()+ "&" +"code=" + verificationCode + "&" + "device_uid="+device.getDeviceUId() + "&" + "role=" +roleName;
            final Email email = EmailManager.emailBuilder().subject("Verify your registration")
                    .body(String.format(VERIFICATION_EMAIL,verificationUri)).to(user.getEmail()).build();
            emailManager.sendEmail(email);
            //Only after we have sent the email, we add the code to the in memory cache.
            getCacheManager().getUserVerificationCache().put(verificationCode,user);
            return Response.ok().entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user)).build();
        } catch (Exception e) {
            serviceLogger.error("There was an error querying for the device, user or the device user with exception "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .entity(ErrorMessage.builder().withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
    }

    @GET
    @Path("{email}")
    @Transactional
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserDetails(@PathParam("email") final String userName, @Context ContainerRequestContext requestContext) {
        //Get the current authorized user if present.
        final IUser user = (IUser)(requestContext.getSecurityContext().getUserPrincipal());
        if(user == null) {
            serviceLogger.error("The user object obtained from the getUserPrincipal method call was null and unexpected. This was most likely because of an internal server error.");
            return Response.status(HttpStatus.BAD_REQUEST.value()).entity(ErrorMessage.builder().withErrorCode(ErrorMessage.NOT_AUTHORIZED).build()).build();
        }
        //return the user details as is.
        try {
            return Response.ok().entity(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user)).build();
        } catch (Exception e) {
            serviceLogger.error("An error occurred while serializing using the object mapper with exception message "+e.getLocalizedMessage());
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).entity(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.INTERNAL_SERVER_ERROR).build()).build();
        }
    }
}
