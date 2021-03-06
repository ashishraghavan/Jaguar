package com.jaguar.common;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jaguar.cache.ICacheManager;
import com.jaguar.exception.ErrorMessage;
import com.jaguar.om.*;
import com.jaguar.om.impl.Role;
import com.jaguar.om.impl.UserRole;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.testng.util.Strings;

import javax.ws.rs.container.ContainerRequestContext;
import java.net.URI;
import java.util.regex.Pattern;

@Component
public class CommonService extends CommonConstants {
    protected final Logger serviceLogger = Logger.getRootLogger();
    private IBaseDAO dao;
    private IProductDAO productDAO;
    private ICacheManager cacheManager;
    private final String classNameHashCodeTemplate = "Classname/hashcode : %s / %d";
    protected static final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,false);
    protected static final String[] USER_IGNORE_PROPERTIES = new String[]{"active"};
    @SuppressWarnings("unused")
    protected static final String[] DEVICE_IGNORE_PROPERTIES = USER_IGNORE_PROPERTIES;
    @SuppressWarnings("unused")
    protected static final String[] DEVICE_USER_IGNORE_PROPERTIES = USER_IGNORE_PROPERTIES;
    protected static final String context = "/client/api";
    protected static final String files = "/files";
    private static final String X_FORWARDED_PROTO = "X_FORWARDED_PROTO";
    private static final String phoneNumberRegex = "^((\\(\\d{3}\\) ?)|(\\d{3}-))?\\d{3}-\\d{4}$";
    private static final Pattern pattern = Pattern.compile(phoneNumberRegex);

    @Autowired
    public void setDao(IBaseDAO dao) {
        this.dao = dao;
        serviceLogger.info("Injection of IBaseDao finished with details: "+this.dao.getEntityManager().toString());
        serviceLogger.info("Classname/hashcode : "+this.getClass().getName() + "/"+super.hashCode());
        serviceLogger.info(String.format(classNameHashCodeTemplate, this.dao.getClass().getSimpleName(), this.dao.hashCode()));
    }

    @Autowired
    public void setProductDAO(IProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Autowired
    public void setCacheManager(ICacheManager cacheManager) {
        this.cacheManager = cacheManager;
        serviceLogger.info(String.format(classNameHashCodeTemplate, this.cacheManager.getClass().getSimpleName(), this.cacheManager.hashCode()));
    }

    protected IBaseDAO getDao() {
        return this.dao;
    }

    protected IProductDAO getProductDAO() {
        return this.productDAO;
    }

    protected ICacheManager getCacheManager() {
        return cacheManager;
    }

    protected String wrapExceptionForEntity(final Exception exception) {
        final String errorMessage = "An unknown error occurred";
        try {
            if(exception == null || Strings.isNullOrEmpty(exception.getMessage())) {
                serviceLogger.error("Couldn't build an entity out of a null exception object");
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ErrorMessage.builder()
                        .withErrorCode(ErrorMessage.FREE_FORM).withMessage(errorMessage).build());
            }
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ErrorMessage.builder()
                    .withErrorCode(ErrorMessage.FREE_FORM).withMessage(exception.getLocalizedMessage()).build());
        } catch (Exception e) {
            serviceLogger.error("An exception occurred while sending exception details!");
            return errorMessage;
        }
    }

    protected String getAuthTokenFromHeaders(final String authValue) {
        if(Strings.isNullOrEmpty(authValue)) {
             serviceLogger.error("Auth token was null for this request");
            return null;
        }
        //Bearer 3456.....
        final String[] authTokenized = authValue.trim().split(" ");
        if(authTokenized.length != 2) {
            serviceLogger.error("Auth token is not of the correct format. Expected header string to be Authorization:Bearer xxxx-xxxx-xxxx-xxxx, but found "+ authValue);
            return null;
        }
        return authTokenized[1];
    }

    @Transactional
    protected IUserRole getUserRole(final IUser user,final IRole role) {
        if(user == null || role == null) {
            serviceLogger.error("User & Role required to be non-null for this request");
            return null;
        }
        try {
            final IUserRole userRole = new UserRole(user,role);
            return getDao().loadSingleFiltered(userRole,null,false);
        } catch (Exception e) {
            serviceLogger.error("There was an error querying for UserRole with the message "+e.getLocalizedMessage());
            return null;
        }
    }

    @Transactional
    protected IRole getRoleByName(final String roleName) {
        if(Strings.isNullOrEmpty(roleName)) {
            serviceLogger.error("Null/empty role name.");
            return null;
        }
        final IRole role = new Role(roleName);
        try {
            return getDao().loadSingleFiltered(role,null,false);
        } catch (Exception e) {
            serviceLogger.error("There was an error querying the role by name "+roleName+" with the exception "+e.getLocalizedMessage());
            return null;
        }
    }

    @Transactional
    protected IUser getUser(final IUser user) {
        if(user == null) {
            throw new NullPointerException("User object expected to be non-null");
        }
        IUser userFromDb;
        try {
            userFromDb = getDao().loadSingleFiltered(user,null,false);
            if(userFromDb == null) {
                serviceLogger.info("There is no user with the email "+user.getEmail()+", the user search returned null");
                return null;
            }
            return userFromDb;
        } catch (Exception e) {
            serviceLogger.error("There was an error querying the user object with exception "+e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Verifies if a phone number is a US number.
     * @param phoneNumber The incoming phone number to be verified.
     * @return {@link Boolean#TRUE} if it is, {@link Boolean#FALSE} otherwise.
     */
    protected boolean validatePhoneNumber(final String phoneNumber) {
        return pattern.matcher(phoneNumber).matches();
    }

    protected String getUSPhoneNumber(String phoneNumber) {
        //Append a +1 when sending an SMS.
        return "+1" + phoneNumber;
    }

    /**
     * When using apache,the requests are proxied from an https scheme to an http scheme if
     * tomcat using http.
     * @param requestContext The request context being used
     * @return http or https depending on whether the header X_FORWARDED_PROTO is present.
     */
    protected String getScheme(final ContainerRequestContext requestContext) {
        if(requestContext == null) {
            throw new IllegalArgumentException("Request context expected to be non-null");
        }
        //Determine if the authority by checking the header X_FORWARDED_PROTO. If it is present, use that value.
        //If not, use the value from absolutePath.getAuthority();
        String scheme = "http";
        final String xForwardedProto = requestContext.getHeaderString(X_FORWARDED_PROTO);
        if(!Strings.isNullOrEmpty(xForwardedProto)) {
            scheme = xForwardedProto;
        }
        return scheme;
    }

    /**
     * Get the port if obtained from the absolute URI.
     * @param requestContext The request context being used.
     * @return The port number in String format or empty if port is not defined.
     */
    @SuppressWarnings("unused")
    protected String getPort(final ContainerRequestContext requestContext) {
        if(requestContext == null) {
            throw new IllegalStateException("Request context expected to be non-null for this method");
        }
        final URI absolutePath = requestContext.getUriInfo().getAbsolutePath();
        if(absolutePath.getPort() > 0) {
            return String.valueOf(absolutePath.getPort());
        }
        return "";
    }
}
