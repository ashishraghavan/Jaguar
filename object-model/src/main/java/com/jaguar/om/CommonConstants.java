package com.jaguar.om;


public class CommonConstants {
    protected static final String AUTHORIZATION = "Authorization";
    protected static final String APP_COOKIE_NAME = "jaguar_cookie";
    protected static final String INTERNAL_SERVER_ERROR_MSG = "The server encountered an error while processing this message";
    protected static final String OAUTH2_FLOW = "oauth2_flow";
    protected static final String SCOPES = "scopes";
    protected static final String REDIRECT_URI = "redirect_uri";
    protected static final String AUTHORIZATION_CODE = "authorization_code";
    protected static final String CLIENT_ID = "client_id";
    protected static final String DEVICE_UID = "device_uid";
    protected static final String ERROR_REASON = "error_reason";
    protected static final String EMAIL_VERIFICATION_CODE = "code";
    protected static final String EMAIL_VERIFICATION_ROLE = "role";

    //Prompt values for OAUTH2
    protected static final String PROMPT_LOGIN = "login";
    protected static final String PROMPT_NONE = "none";
    protected static final String PROMPT_CONSENT = "consent";
    protected static final String PROMPT_SELECT_ACCOUNT = "select_account";

    protected static final String VERIFICATION_EMAIL = "Please verify yourself by clicking on the following link. \n\n" +
            "%s";
    protected static final String DEVICE_ADDITION_LINK = "You have signed on using the device %s. Click %s for details of this device.";
    protected static final String NEW_DEVICE_ADDED = "Review Sign-In";
    //Default start
    protected static final int DEFAULT_START = 0;
    //Default size
    protected static final int DEFAULT_SIZE = 20;
}
