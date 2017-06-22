package com.jaguar.common;


import com.jaguar.om.IBaseDAO;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommonService {
    protected final Logger serviceLogger = Logger.getRootLogger();

    @Autowired
    protected IBaseDAO dao;

    public void setDao(IBaseDAO dao) {
        this.dao = dao;
    }

    protected IBaseDAO getDao() {
        return this.dao;
    }
}
