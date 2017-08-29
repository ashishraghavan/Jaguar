package com.jaguar.jersey.provider;


import com.jaguar.cache.ICacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaseFilter {
    private ICacheManager cacheManager;
    protected static final String AUTHORIZATION = "Authorization";


    @Autowired
    public void setCacheManager(final ICacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public ICacheManager getCacheManager() {
        return this.cacheManager;
    }
}
