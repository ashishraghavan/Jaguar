package com.jaguar.om.impl;

import com.jaguar.om.GenericDaoException;
import com.jaguar.om.IBaseDAO;
import com.jaguar.om.ICommonObject;
import com.jaguar.om.enums.EOrder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.util.*;

@Repository
@SuppressWarnings("deprecation")
public class BaseDAO implements IBaseDAO {

    @PersistenceContext(unitName = "ObjectModel")
    public EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    public IBaseDAO getDao() {
        return this;
    }

    public Session getSession() {
        return (Session)this.entityManager.getDelegate();
    }

    @SuppressWarnings("unchecked")
    public <T> T loadEager(Class<T> objectClass, Object id, String[] relationships) {
        final Criteria criteria = getSession().createCriteria(objectClass);
        criteria.add(Restrictions.idEq(id));
        for(String relationShip : relationships) {
            criteria.setFetchMode(relationShip,FetchMode.JOIN);
        }
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return (T)criteria.uniqueResult();
    }

    public <T> T loadEager(Class<T> objectClass, Object id) {
        return loadEager(objectClass,id,null);
    }

    public <T> T load(Class<T> objectClass, Object id) {
        return getEntityManager().getReference(objectClass,id);
    }

    @SuppressWarnings("unchecked")
    public <T> Map<String, Object> load(Class<T> objectClass, Set<String> fields, Object id) {
        final Criteria criteria = getSession().createCriteria(objectClass);
        criteria.add(Restrictions.idEq(id));
        criteria.setCacheable(true);
        final ProjectionList projectionList = Projections.projectionList();
        for(String field : fields) {
            projectionList.add(Projections.property(field),field);
        }
        criteria.setProjection(projectionList);
        criteria.setMaxResults(1);
        criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        final List<Map<String,Object>> results = criteria.list();
        if(results.size() == 1) {
            return results.get(0);
        } else {
            return null;
        }
    }

    public <T> List<T> loadFiltered(T filterObject, boolean cacheable, Map<String, EOrder> sortCriteria) throws GenericDaoException {
        return loadFiltered(filterObject,null,null,cacheable,sortCriteria,null,true,null,null);
    }

    public <T> List<T> loadFiltered(T filterObject, boolean cacheable) throws GenericDaoException {
        return loadFiltered(filterObject,null,null,cacheable,null,null,true,null,null);
    }

    public <T> List<T> loadFiltered(T filterObject, Integer size) throws GenericDaoException {
        return loadFiltered(filterObject,null,null,false,null,null,true,null,size);
    }

    public <T> List<T> loadFiltered(T filterObject, String[] beanProperties, boolean cacheable, Integer size) throws GenericDaoException {
        return loadFiltered(filterObject,beanProperties,null,cacheable,null,null,true,null,size);
    }

    public <T> List<T> loadFiltered(T filterObject, String[] beanProperties, boolean cacheable, Integer startIndex, Integer size) throws GenericDaoException {
        return loadFiltered(filterObject,beanProperties,null,cacheable,null,null,true,startIndex,size);
    }

    public <T> List<T> loadFiltered(T filterObject, String[] beanProperties, boolean cacheable, Map<String, EOrder> sortCriteria, Integer startIndex, Integer size) throws GenericDaoException {
        return loadFiltered(filterObject,beanProperties,null,cacheable,sortCriteria,null,true,startIndex,size);
    }


    public <T> T loadSingleFiltered(T filterObject, String[] properties, boolean cacheable) throws GenericDaoException {
        final List<T> resultList = loadFiltered(filterObject,properties,cacheable);
        if(resultList != null && resultList.size() > 1) {
            throw new GenericDaoException("Multiple rows exist for "+filterObject);
        }
        if(resultList != null && resultList.size() == 1) {
            return resultList.get(0);
        }
        return null;
    }

    public <T> List<T> loadFiltered(T filterObject, String[] properties, boolean cacheable) throws GenericDaoException {
        return loadFiltered(filterObject,properties,null,cacheable,null,null,true,null,null);
    }

    public <T> List<T> loadFiltered(T filterObject, String[] beanProperties, boolean cacheable, Map<String, EOrder> sortCriteria) throws GenericDaoException {
        return loadFiltered(filterObject,beanProperties,null,cacheable,sortCriteria,null,true,null,null);
    }

    public <T> List<T> loadFilteredByDate(T filterObject, String[] properties, boolean cacheable, Map<String, List<Date>> dateRange) throws GenericDaoException {
        return loadFiltered(filterObject,properties,null,cacheable,null,dateRange,true,null,null);
    }

    public <T> List<Map<String, Object>> loadFields(T filterObject, Set<String> returnFields, String[] beanProperties, boolean cacheable) throws GenericDaoException {
        return loadFields(filterObject,returnFields,beanProperties,cacheable,null,null,null);
    }

    public <T> List<Map<String, Object>> loadFields(T filterObject, Set<String> returnFields, String[] beanProperties, boolean cacheable, Map<String, EOrder> sortCriteria) throws GenericDaoException {
        return loadFields(filterObject,returnFields,beanProperties,cacheable,sortCriteria,null,null);
    }

    @SuppressWarnings("unchecked")
    public <T> List<Map<String, Object>> loadFields(T filterObject, Set<String> returnFields, String[] beanProperties, boolean cacheable, Map<String, EOrder> sortCriteria, Integer startIndex, Integer size) throws GenericDaoException {
        final Example example = Example.create(filterObject).excludeZeroes();
        if(null != beanProperties && beanProperties.length > 0) {
            for(String property : beanProperties) {
                example.excludeProperty(property);
            }
        }

        final Criteria criteria = getSession().createCriteria(filterObject.getClass()).add(example);
        criteria.setCacheable(cacheable);
        if(sortCriteria != null && !sortCriteria.isEmpty()) {
            for(String sortingProperty : sortCriteria.keySet()) {
                if(sortCriteria.get(sortingProperty).getValue() == EOrder.ASCENDING.getValue()) {
                    criteria.addOrder(Order.asc(sortingProperty));
                } else {
                    criteria.addOrder(Order.desc(sortingProperty));
                }
            }
        }
        try {
            addPropertyCriteria(null,criteria,criteria,filterObject,beanProperties);
        } catch (Exception e) {
            throw new GenericDaoException(e.getCause());
        }
        final ProjectionList projectionList = Projections.projectionList();
        for(String returnField : returnFields) {
            projectionList.add(Projections.property(returnField));
        }
        criteria.setProjection(projectionList);
        criteria.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        if(startIndex >= 0) {
            criteria.setFirstResult(startIndex);
        }
        if(size > 0) {
            criteria.setMaxResults(size);
        }
        return (List<Map<String,Object>>)criteria.list();
    }

    public <T> T loadSingleFiltered(T filterObject, String[] properties, String[] eagerRelationships, boolean cacheable) throws GenericDaoException {
        final List<T> resultList = loadFiltered(filterObject,properties,eagerRelationships,cacheable);
        if(resultList != null && resultList.size() > 1) {
            throw new GenericDaoException("Multiple rows returned from filtered call for "+filterObject.getClass().getSimpleName());
        }
        if(resultList != null && resultList.size() == 1) {
            return resultList.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> loadByFields(Class<T> filterObject, Map<String, Object> fields, boolean cacheable, String sortField, EOrder sortOrder) throws GenericDaoException {
        Criteria criteria = getSession().createCriteria(filterObject.getClass());
        final Iterator<Map.Entry<String,Object>> iterator = fields.entrySet().iterator();
        int k = 0;
        while(iterator.hasNext()) {
            Map.Entry<String,Object> entry = iterator.next();
            final String key = entry.getKey();
            if(!key.contains(".")) {
                criteria = criteria.add(Restrictions.eq(key,entry.getValue()));
            } else {
                String[] keys = key.split("\\.");
                Criteria nestedCriteria = null;
                String prevChar = null;
                for (int i=0; i<keys.length-1; i++) {
                    if (nestedCriteria == null) {
                        nestedCriteria = criteria.createAlias(keys[i], keys[i].substring(0,i+1 ) + k);
                    }
                    else {
                        nestedCriteria = nestedCriteria.createAlias(prevChar + "." + keys[i], keys[i].substring(0,i+1) + k);
                    }
                    prevChar = keys[i].substring(0,i+1 ) + k;
                }
                if (nestedCriteria != null) {
                    nestedCriteria.add(Restrictions.eq(prevChar + "." + keys[keys.length - 1], entry.getValue()));
                }
            }
            k++;
        }
        if( cacheable)
        {
            criteria.setCacheable( true);
        }

        //Support an optional sort field
        if( sortField != null)
        {
            if( sortOrder == EOrder.ASCENDING)
            {
                criteria = criteria.addOrder( Order.asc( sortField));
            }
            else
            {
                criteria = criteria.addOrder( Order.desc( sortField));
            }
        }

        return criteria.list();
    }

    public <T> long getSize(T filterObject) throws GenericDaoException {
        return getSize(filterObject,null);
    }

    public <T> long getSize(T filterObject, String[] properties) throws GenericDaoException {
        Example example = Example.create(filterObject).excludeZeroes();
        if (null != properties && properties.length > 0) {
            for (String property : properties){
                example.excludeProperty(property);
            }
        }
        Criteria criteria = getSession().createCriteria(filterObject.getClass());
        try{
            addPropertyCriteria(null, criteria, criteria, filterObject, properties);
        }
        catch (Exception e) {
            throw new GenericDaoException("Failed to build query for " + filterObject, e);
        }
        criteria.add(example);
        criteria.setProjection(Projections.rowCount());
        return (Long)(criteria.list().get(0));
    }

    public <T> long getSizeByQuery(T filterObject, String[] properties, Map<String, Object> searchCriteria) throws GenericDaoException {
        Example example = Example.create(filterObject).excludeZeroes();
        if (null != properties && properties.length > 0) {
            for (String property : properties){
                example.excludeProperty(property);
            }
        }
        Criteria criteria = getSession().createCriteria(filterObject.getClass());
        try{
            addPropertyCriteria(null, criteria, criteria, filterObject, properties);
        }
        catch (Exception e) {
            throw new GenericDaoException("Failed to build query for " + filterObject, e);
        }
        //Set search criteria.
        for (String key : searchCriteria.keySet()) {
            if (searchCriteria.get(key) instanceof java.lang.Enum || searchCriteria.get(key) instanceof CommonObject) {
                criteria.add(Restrictions.eq(key, searchCriteria.get(key)));
            } else if(searchCriteria.get(key) instanceof List) {
                criteria.add(Restrictions.in(key, (List)searchCriteria.get(key)));
            } else if (searchCriteria.get(key) instanceof java.lang.Long) {
                criteria.add(Restrictions.eq(key, searchCriteria.get(key)));
            } else {
                criteria.add(Restrictions.like(key, "%" + searchCriteria.get(key) + "%").ignoreCase());
            }
        }
        criteria.add(example);
        criteria.setProjection(Projections.rowCount());
        return (Long)criteria.list().get(0);
    }

    public <T extends CommonObject> int deleteAllBefore(Class<T> clazz, Date date) {
        final StringBuilder sb = new StringBuilder("delete from ");
        sb.append(clazz.getSimpleName())
                .append(" where creationDate < :date ");
        final Query query = getEntityManager().createQuery(sb.toString());
        query.setParameter("date", date);
        return query.executeUpdate();
    }

    public <T> List<T> loadFiltered(T filterObject, String[] properties, String[] relationships, boolean cacheable) throws GenericDaoException {
        return loadFiltered(filterObject,properties,relationships,cacheable,null,null,true,null,null);
    }

    public <T> List<T> loadFiltered(T filterObject, String[] properties, String[] relationships, boolean cacheable, Integer startIndex, Integer size) throws GenericDaoException {
        return loadFiltered(filterObject,properties,relationships,cacheable,null,null,true,startIndex,size);
    }

    public <T> List<T> search(Class<T> objectClass, Map<String, Object> searchCriteria, boolean isCasesensitive) throws GenericDaoException {
        return search(objectClass,searchCriteria,isCasesensitive,null,null);
    }

    public <T> List<T> search(Class<T> objectClass, Map<String, Object> searchCriteria, boolean isCasesensitive, Integer startIndex, Integer size) throws GenericDaoException {
        return search(objectClass,searchCriteria,null,isCasesensitive,startIndex,size);
    }

    public <T> List<T> search(Class<T> objectClass, Map<String, Object> searchCriteria, String[] relationships, boolean isCasesensitive) throws GenericDaoException {
        return search(objectClass,searchCriteria,null,isCasesensitive,null,null);
    }

    @Transactional(readOnly = false,propagation = Propagation.REQUIRED)
    public <T> T save(T filterObject) throws GenericDaoException {
        return this.entityManager.merge(filterObject);
    }

    @Transactional(readOnly = false,propagation = Propagation.REQUIRED)
    public <T> void remove(T filterObject) throws GenericDaoException {
        this.entityManager.refresh(filterObject);
        this.entityManager.remove(filterObject);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = false,propagation = Propagation.REQUIRED)
    public <T> List<T> search(final Class<T> filterClass, final Map<String, Object> searchCriteria,final String[] relationShips,
                              final boolean isCaseSensitive,final Integer startIndex,final Integer size) throws GenericDaoException{
        final Criteria criteria = getSession().createCriteria(filterClass);
        if(searchCriteria == null) {
            return Collections.emptyList();
        }

        for(String key : searchCriteria.keySet()) {
            if(searchCriteria.get(key) != null) {
                final Object criteriaObj = searchCriteria.get(key);
                if(criteriaObj instanceof List) {
                    criteria.add(Restrictions.in(key, (List) criteriaObj));
                    continue;
                }
                if(criteriaObj instanceof Enum || criteriaObj instanceof CommonObject || criteriaObj instanceof Long) {
                    criteria.add(Restrictions.eq(key,criteriaObj));
                    continue;
                }
                if(!isCaseSensitive) {
                    criteria.add(Restrictions.like(key,"%"+criteriaObj+"%").ignoreCase());
                } else {
                    criteria.add(Restrictions.like(key,"%"+criteriaObj+"%"));
                }
            }
        }
        if(relationShips != null) {
            for(String relationShip : relationShips) {
                criteria.setFetchMode(relationShip, FetchMode.JOIN);
            }
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        }
        if(startIndex != null && startIndex >= 0) {
            criteria.setFirstResult(startIndex);
        }
        if(size != null && startIndex != null && size > startIndex) {
            criteria.setMaxResults(size);
        }
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> loadFiltered(T filterObject, String[] properties, String[] relationShips,
                                    boolean cacheable, Map<String, EOrder> sortCriteria, Map<String, List<Date>> dateRange,
                                    boolean recursiveFilter, Integer startIndex, Integer size) throws GenericDaoException {
        final Example example = Example.create(filterObject).excludeZeroes();
        if(null != properties && properties.length > 0) {
            for(String property : properties) {
                example.excludeProperty(property);
            }
        }
        final Criteria criteria = getSession().createCriteria(filterObject.getClass());
        if(cacheable) {
            criteria.setCacheable(true);
        }
        if(recursiveFilter) {
            try {
                addPropertyCriteria(null,criteria,criteria,filterObject,properties);
            } catch (Exception e) {
                throw new GenericDaoException(e.getCause());
            }
        }
        if(relationShips != null) {
            for(String relationShip : relationShips) {
                criteria.setFetchMode(relationShip,FetchMode.JOIN);
            }
        } else {
            criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        }
        criteria.add(example);
        if(null != sortCriteria && !sortCriteria.isEmpty()) {
            for(String sortKey : sortCriteria.keySet()) {
                if(sortCriteria.get(sortKey).getValue() == EOrder.ASCENDING.getValue()) {
                    criteria.addOrder(Order.asc(sortKey));
                } else {
                    criteria.addOrder(Order.desc(sortKey));
                }
            }
        }
        if(null != dateRange && !dateRange.isEmpty()) {
            for(String range : dateRange.keySet()) {
                final List<Date> dateList = dateRange.get(range);
                if(dateList.size() == 2) {
                    criteria.add(Restrictions.between(range,dateList.get(0),dateList.get(1)));
                }
            }
        }
        if(null != size && size > 0) {
            criteria.setMaxResults(size);
        }
        if(null != startIndex && startIndex >=0) {
            criteria.setFirstResult(startIndex);
        }
        return criteria.list();
    }

    private <T> Long getLongIdForObject(final T filterObject) throws NoSuchFieldException, IllegalAccessException {
        final Field idField = FieldUtils.getField(filterObject.getClass(), "id", true);
        Object idValue = idField.get(filterObject);
        final Long id;
        if(idValue instanceof Long) {
            if(((Long)idValue) == 0L) {
                id = null;
            } else {
                id = (Long)idValue;
            }
        } else {
            id = null;
        }
        return id;
    }

    private <T> void addPropertyCriteria(final String prefix,final Criteria rootCriteria,final Criteria parentCriteria,
                                         final T filterObject,final String[] properties) throws GenericDaoException, NoSuchFieldException, IllegalAccessException {
        Long idValue = getLongIdForObject(filterObject);
        if(idValue == null) {
            final Field[] fields = filterObject.getClass().getFields();
            for(Field field : fields) {
                if(ICommonObject.class.isAssignableFrom(field.getType())) {
                    if(field.get(filterObject) == null) {
                        continue;
                    }
                    if(!StringUtils.hasLength(prefix)) {
                        Long propId = getLongIdForObject(field.get(filterObject));
                        Criteria criteria;
                        if(propId != null) {
                            criteria = parentCriteria.createCriteria(field.getName()).setFetchMode(field.getName(),FetchMode.JOIN).add(Restrictions.eq("id",propId));
                        } else {
                            Example exampleSubCriteria = Example.create(field.get(filterObject)).excludeZeroes();
                            if(ArrayUtils.contains(properties,field.getName())) {
                                exampleSubCriteria.excludeProperty(field.getName());
                            }
                            criteria = rootCriteria.createCriteria(field.getName()).add(exampleSubCriteria);
                        }
                        addPropertyCriteria(field.getName(),rootCriteria,criteria,field.get(filterObject),properties);
                    } else {
                        Long propId = getLongIdForObject(field.get(filterObject));
                        Criteria criteria;
                        if(propId != null) {
                            criteria = parentCriteria.createCriteria(field.getName()).setFetchMode(field.getName(),FetchMode.JOIN).add(Restrictions.eq("id",propId));
                        } else {
                            Example exampleSubCriteria = Example.create(field.get(filterObject)).excludeZeroes();
                            if(ArrayUtils.contains(properties,prefix + "." +field.getName())) {
                                exampleSubCriteria.excludeProperty(prefix + "." +field.getName());
                            }
                            criteria = rootCriteria.createCriteria(prefix + "." +field.getName()).add(exampleSubCriteria);
                        }
                        addPropertyCriteria(prefix + "."+field.getName(),rootCriteria,criteria,field.get(filterObject),properties);
                    }
                }
            }
        }
    }


}
