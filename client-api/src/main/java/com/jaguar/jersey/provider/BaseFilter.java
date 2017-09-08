package com.jaguar.jersey.provider;


import com.jaguar.cache.ICacheManager;
import com.jaguar.om.CommonConstants;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaseFilter extends CommonConstants {
    private ICacheManager cacheManager;
    protected final Logger filterLogger = Logger.getLogger(this.getClass().getSimpleName());

    @Autowired
    public void setCacheManager(final ICacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public ICacheManager getCacheManager() {
        return this.cacheManager;
    }
}
