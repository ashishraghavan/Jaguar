package com.jaguar.jersey.provider;


import com.jaguar.cache.ICacheManager;
import com.jaguar.om.CommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaseFilter extends CommonConstants {
    private ICacheManager cacheManager;

    @Autowired
    public void setCacheManager(final ICacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public ICacheManager getCacheManager() {
        return this.cacheManager;
    }
}
