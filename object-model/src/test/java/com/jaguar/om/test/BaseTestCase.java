package com.jaguar.om.test;

import com.jaguar.om.IBaseDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@ContextConfiguration(locations = {"classpath:spring-config.xml"})
public abstract class BaseTestCase extends AbstractTransactionalTestNGSpringContextTests {
    protected static final String CLIENT_PORT = "18080";
    private IBaseDAO dao;

    @Autowired
    public void setDao(IBaseDAO dao) {
        this.dao = dao;
    }

    protected IBaseDAO getDao() {
        return this.dao;
    }
}
