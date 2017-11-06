package com.jaguar.om;

import javax.persistence.EntityManager;

public interface ICommonDao extends IBaseDAO {
    @Override
    void setEntityManager(EntityManager entityManager);

    @Override
    EntityManager getEntityManager();
}
