package com.jaguar.om;


import com.jaguar.om.enums.EOrder;
import com.jaguar.om.impl.CommonObject;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IBaseDAO {

    void setEntityManager(EntityManager entityManager);
    EntityManager getEntityManager();

    Session getSession();

    public <T> T loadEager(Class<T> objectClass, Object id, String[] relationships);
    public <T> T loadEager(Class<T> objectClass, Object id);
    public <T> T load(Class<T> objectClass, Object id);
    public <T> Map<String, Object> load(Class<T> objectClass, Set<String> fields, Object id);
    public <T> List<T> loadFiltered(T filterObject, boolean cacheable, Map<String, EOrder> sortCriteria) throws GenericDaoException;
    public <T> List<T> loadFiltered(T filterObject, boolean cacheable) throws GenericDaoException;
    public <T> List<T> loadFiltered(T filterObject, Integer size) throws GenericDaoException;
    public <T> List<T> loadFiltered(T filterObject, String[] beanProperties, boolean cacheable, Integer size) throws GenericDaoException;
    public <T> List<T> loadFiltered(T filterObject, String[] beanProperties, boolean cacheable, Integer startIndex, Integer size) throws GenericDaoException;
    public <T> List<T> loadFiltered(T filterObject, String[] beanProperties, boolean cacheable,
                                    Map<String, EOrder> sortCriteria, Integer startIndex, Integer size) throws GenericDaoException;
    //get a single entity.
    <T> T loadSingleFiltered(T filterObject,String[] properties,boolean cacheable) throws GenericDaoException;
    //get a list of entities
    <T> List<T> loadFiltered(T filterObject,String[] properties,boolean cacheable) throws GenericDaoException;
    public <T> List<T> loadFiltered(T filterObject, String[] beanProperties, boolean cacheable, Map<String, EOrder> sortCriteria) throws GenericDaoException;
    public <T> List<T> loadFilteredByDate(T filterObject, String[] properties, boolean cacheable, Map<String, List<Date>> dateRange) throws GenericDaoException;
    public <T> List<Map<String, Object>> loadFields(T filterObject, Set<String> returnFields,
                                                    String[] beanProperties, boolean cacheable) throws GenericDaoException;
    public <T> List<Map<String, Object>> loadFields(T filterObject, Set<String> returnFields,
                                                    String[] beanProperties, boolean cacheable, Map<String, EOrder> sortCriteria) throws GenericDaoException;
    public <T> List<Map<String, Object>> loadFields(T filterObject, Set<String> returnFields,
                                                    String[] beanProperties, boolean cacheable, Map<String, EOrder> sortCriteria,
                                                    Integer startIndex, Integer size) throws GenericDaoException;
    public <T> T loadSingleFiltered(T filterObject, String[] properties, String[] eagerRelationships, boolean cacheable) throws GenericDaoException;
    public <T> List<T> loadByFields( Class<T> filterObject,
                                     Map<String,Object> fields,
                                     boolean cacheable,
                                     String sortField,
                                     EOrder sortOrder) throws GenericDaoException;
    public <T> long getSize(T filterObject) throws GenericDaoException;
    public <T> long getSize(T filterObject, String[] properties) throws GenericDaoException;
    public <T> long getSizeByQuery(T filterObject, String[] properties, Map<String,Object> searchCriteria) throws GenericDaoException;
    public <T extends CommonObject> int deleteAllBefore(Class<T> clazz, Date date);
    public <T> List<T> loadFiltered(T filterObject, String[] properties, String[] relationships, boolean cacheable) throws GenericDaoException;
    public <T> List<T> loadFiltered(T filterObject, String[] properties, String[] relationships, boolean cacheable, Integer startIndex, Integer size) throws GenericDaoException;
    public <T> List<T> search(Class<T> objectClass, Map<String, Object> searchCriteria, boolean isCasesensitive) throws GenericDaoException;
    public <T> List<T> search(Class<T> objectClass, Map<String, Object> searchCriteria, boolean isCasesensitive, Integer startIndex, Integer size) throws GenericDaoException;
    public <T> List<T> search(Class<T> objectClass, Map<String, Object> searchCriteria, String[] relationships, boolean isCasesensitive)throws GenericDaoException;
    //save an entity
    <T> T save(T filterObject) throws GenericDaoException;
    //remove an entity
    <T> void remove(T filterObject) throws GenericDaoException;

    <T> List<T> search(Class<T> filterClass, final Map<String, Object> searchCriteria,final String[] relationShips,
                       final boolean isCaseSensitive,final Integer startIndex,final Integer size) throws GenericDaoException;

    <T> List<T> loadFiltered(final T filterObject, final String[] properties,String[] relationShips,final boolean cacheable,final Map<String,EOrder> sortCriteria,
                             final Map<String,List<Date>> dateRange,final boolean recursiveFilter,final Integer startIndex,final Integer size) throws GenericDaoException;
}
