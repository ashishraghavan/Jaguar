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
    private final long APP_TOKEN_DURATION = 1;
    private final Calendar calendar = Calendar.getInstance();
    //Here key would the app session identifier (random) and value will be the application against which this was obtained.
    protected final Cache<String,IApplication> appSessionCache = CacheBuilder
            .newBuilder().expireAfterWrite(APP_SESSION_DURATION, TimeUnit.DAYS).recordStats().build();
    //This is the token cache.
    protected final Cache<IUser,IApplication> tokenCache = CacheBuilder
            .newBuilder().expireAfterWrite(APP_TOKEN_DURATION,TimeUnit.DAYS).recordStats().build();


    @Override
    public Cache<String, IApplication> getAppCache() {
        return appSessionCache;
    }

    @Override
    public Cache<IUser, IApplication> getTokenCache() {
        return tokenCache;
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
