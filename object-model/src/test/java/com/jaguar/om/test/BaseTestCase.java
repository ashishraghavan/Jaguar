package com.jaguar.om.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.jaguar.om.IBaseDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;

import java.util.ArrayList;
import java.util.HashMap;

@ContextConfiguration(locations = {"classpath:spring-config.xml"})
public abstract class BaseTestCase extends AbstractTransactionalTestNGSpringContextTests {
    protected static final String CLIENT_PORT = "18080";
    final String[] COMMON_EXCLUDE_PROPERTIES = new String[]{"creationDate","modificationDate","active"};
    protected final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
    private IBaseDAO dao;

    @Autowired
    public void setDao(IBaseDAO dao) {
        this.dao = dao;
    }

    IBaseDAO getDao() {
        return this.dao;
    }
    private static final TypeFactory typeFactory = TypeFactory.defaultInstance();
    protected static final MapType typeMapStringObject
            = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
    protected static final MapType typeMapStringString
            = typeFactory.constructMapType(HashMap.class, String.class, String.class);
    protected static CollectionType typeListMap = typeFactory.constructCollectionType(ArrayList.class,
            typeFactory.constructType(HashMap.class));
}
