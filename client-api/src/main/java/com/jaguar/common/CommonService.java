package com.jaguar.common;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jaguar.cache.ICacheManager;
import com.jaguar.om.IBaseDAO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonService {
    protected final Logger serviceLogger = Logger.getRootLogger();
    protected IBaseDAO dao;
    //Create a GSON object so that we don't keep re-creating a new GSON object in sub classes.
    protected final Gson gson = new Gson();
    protected ICacheManager cacheManager;
    private final String classNameHashCodeTemplate = "Classname/hashcode : %s / %d";
    protected final String APP_COOKIE_NAME = "jaguar_cookie";
    protected final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
            .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,false).configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE,false);

    @Autowired
    public void setDao(IBaseDAO dao) {
        this.dao = dao;
        serviceLogger.info("Injection of IBaseDao finished with details: "+this.dao.getEntityManager().toString());
        serviceLogger.info("Classname/hashcode : "+this.getClass().getName() + "/"+super.hashCode());
        serviceLogger.info(String.format(classNameHashCodeTemplate, this.dao.getClass().getSimpleName(), this.dao.hashCode()));
    }

    @Autowired
    public void setCacheManager(ICacheManager cacheManager) {
        this.cacheManager = cacheManager;
        serviceLogger.info(String.format(classNameHashCodeTemplate, this.cacheManager.getClass().getSimpleName(), this.cacheManager.hashCode()));
    }

    protected IBaseDAO getDao() {
        return this.dao;
    }

    protected ICacheManager getCacheManager() {
        return cacheManager;
    }
}
