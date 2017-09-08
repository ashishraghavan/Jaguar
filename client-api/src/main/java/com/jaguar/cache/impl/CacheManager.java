package com.jaguar.cache.impl;


import com.jaguar.cache.ICacheManager;
import com.jaguar.om.IApplication;
import com.jaguar.om.IUser;
import jersey.repackaged.com.google.common.cache.*;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

@Component
public class CacheManager implements ICacheManager {

    private static final Logger cacheLogger = Logger.getLogger(CacheManager.class.getSimpleName());
    private final long APP_SESSION_DURATION = 15;
    //Application tokens are usually a day long.
    private final long USER_APP_DURATION = 1;
    private final long TOKEN_DURATION = 1;
    private final long REFRESH_TOKEN_DURATION = 1;
    private final long AUTHORIZATION_CODE_DURATION = 10; //minutes
    private final Calendar calendar = Calendar.getInstance();
    //Here key would the app session identifier (random) and value will be the application against which this was obtained.
    protected final Cache<String,IApplication> appSessionCache = CacheBuilder
            .newBuilder().expireAfterWrite(APP_SESSION_DURATION, TimeUnit.DAYS).recordStats().build();
    //This is the token cache.
    protected final Cache<IUser,IApplication> userApplicationCache = CacheBuilder
            .newBuilder().expireAfterWrite(USER_APP_DURATION,TimeUnit.DAYS).recordStats().build();
    protected final Cache<String,IUser> tokenCache = CacheBuilder
            .newBuilder().expireAfterWrite(TOKEN_DURATION,TimeUnit.DAYS).recordStats().build();
    protected final Cache<String,IUser> refreshTokenCache = CacheBuilder
            .newBuilder().expireAfterWrite(REFRESH_TOKEN_DURATION,TimeUnit.DAYS).recordStats().build();
    protected final Cache<IUser,String> userAuthorizationCache = CacheBuilder
            .newBuilder().expireAfterWrite(AUTHORIZATION_CODE_DURATION,TimeUnit.MINUTES).recordStats().build();


    @Override
    public Cache<String, IApplication> getAppCache() {
        return appSessionCache;
    }

    @Override
    public Cache<IUser, IApplication> getUserApplicationCache() {
        return userApplicationCache;
    }

    @Override
    public Cache<String, IUser> getTokenCache() {
        return tokenCache;
    }

    @Override
    public Cache<String, IUser> getRefreshTokenCache() {
        return refreshTokenCache;
    }

    @Override
    public Cache<IUser, String> getUserAuthorizationCache() {
        return null;
    }

    @SuppressWarnings("unused")
    private final class AppSessionExpiryListener implements RemovalListener<String,IApplication> {

        @Override
        public void onRemoval(RemovalNotification<String, IApplication> removalNotification) {
            if(removalNotification.getValue() != null) {
                cacheLogger.info("The application with the client id "+removalNotification.getValue().getClientId()
                        + " and key "+removalNotification.getKey()+ " was removed at "+calendar.getTime());
            }
        }
    }
}
