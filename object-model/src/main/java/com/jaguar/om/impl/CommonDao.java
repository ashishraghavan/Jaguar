package com.jaguar.om.impl;

import com.jaguar.om.ICommonDao;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component("dao")
@Transactional
public class CommonDao extends BaseDAO implements ICommonDao {
    private EntityManager entityManager;

    @PersistenceContext(unitName = "ObjectModel")
    public void setEntityManager(EntityManager entityManager) {
        this. entityManager = entityManager;
    }

    public EntityManager getEntityManager(){
        return entityManager;
    }
}
