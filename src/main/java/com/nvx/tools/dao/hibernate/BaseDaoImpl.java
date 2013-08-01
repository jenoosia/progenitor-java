package com.nvx.tools.dao.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.NonUniqueResultException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.googlecode.genericdao.dao.hibernate.GenericDAOImpl;

/**
 * Base Hibernate DAO from which all project DAOs should extend.<br />
 * Note: This DAO implementation is not responsible for Transactions and related
 * actions such as commit, rollback, etc.
 * 
 * @author Jensen Ching
 *
 * @param <T> Bean object
 * @param <ID> Primary Key (ID) of the bean
 */
public class BaseDaoImpl<T, ID extends Serializable> extends GenericDAOImpl<T, ID> implements BaseDao<T, ID>{
    
    /**
     * Logger usable for all implementations.
     */
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    public BaseDaoImpl() {
        super();
    }
    
    @Autowired
    @Override
    public void setSessionFactory(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }
    
    /**
     * Deletes all the records for this DAO's related persistent Object.
     * 
     */
    public int removeAll() {
        try {

            final String entityName = persistentClass.getName();
            
            //TODO Place locks here
            
            LOG.debug("Removing all records from {}...", entityName);
            String qryString = "delete from " + entityName;
            Query qry = getSession().createQuery(qryString);
            
            int result = qry.executeUpdate();
            
            if (result == 0) {
                LOG.warn("No records removed while executing the following query: {}", qryString);
            } else if (result < 0) {
                LOG.error("Result of query is less than 0 (value is {}). Something is wrong.", result);
            } else {
                LOG.debug("Successfully removed records from {}. Total removed: {}", entityName, result);
            }
            
            return result;
        } catch (RuntimeException re) {
            LOG.error("Something went wrong while trying to remove all records.", re);
            throw re;
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> findByExample(T exampleObj, boolean excludeZeroes, String[] excludeProps) {
        try {

            Example example = Example.create(exampleObj);
            
            if (excludeZeroes) {
                example.excludeZeroes();
            }
            
            if (excludeProps != null) {
                for (String prop : excludeProps) {
                    example.excludeProperty(prop);
                }
            }
            
            return getSession().createCriteria(persistentClass).add(example).list();
        } catch (RuntimeException re) {
            LOG.error("Exception occurred while finding by example.", re);
            throw re;
        }
    }
    
    @Override
    public boolean exists(String propertyName, Object value) {
        int result = count(propertyName, value);
        if (result > 0) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public int count() {
        return _count(getSession().createCriteria(persistentClass));
    }
    
    @Override
    public int count(String propertyName, Object value) {
        if (StringUtils.isEmpty(propertyName)) {
            LOG.warn("Null property, cannot count anything, returning 0.");
            return 0;
        }
        
        Criteria criteria = getSession().createCriteria(persistentClass);
        criteria.add(Restrictions.eq(propertyName, value));
        
        return _count(criteria);
    }
    
    private int _count(Criteria criteria) {
        return ((Number)criteria.setProjection(Projections.rowCount()).uniqueResult()).intValue();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public T findUniqueByProperty(String propertyName, Object value) {
        if (StringUtils.isEmpty(propertyName) || value == null) {
            LOG.warn("Null property or value passed into findUniqueByProperty, null returned.");
            return null;
        }
        
        try {

            String qryString = "from " + persistentClass.getName() + " as model where model." +
                    propertyName + " = ?";
            Query qry = getSession().createQuery(qryString);
            qry.setParameter(0, value);
            return (T) qry.uniqueResult();
        } catch (NonUniqueResultException nure) {
            LOG.warn("Find unique returned a non-unique result. Returning null. (Property : {}, Value : {})",
                    propertyName, value);
            return null; //May be better to throw an exception here?
        } catch (RuntimeException re) {
            LOG.error("Exception occurred while finding unique object by property.", re);
            throw re;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<T> findByProperty(String propertyName, Object value) {
        if (StringUtils.isEmpty(propertyName) || value == null) {
            LOG.warn("Null property or value passed into findByProperty, null returned.");
            return new ArrayList<T>();
        }
        
        try {

            String qryString = "from " + persistentClass.getName() + " as model where model." +
                    propertyName + " = ?";
            Query qry = getSession().createQuery(qryString);
            qry.setParameter(0, value);
            return qry.list();
        } catch (RuntimeException re) {
            LOG.error("Exception occurred while finding by property.", re);
            throw re;
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        try {

            String qryString = "from " + persistentClass.getName() + " as model";
            Query qry = getSession().createQuery(qryString);
            return qry.list();
        } catch (RuntimeException re) {
            LOG.error("Exception occurred while finding all.", re);
            throw re;
        }
    }
    
    public Query prepareQuery(String query, Object[] parameters) {
        Query queryObject = getSession().createQuery(query);
        if(parameters != null && parameters.length > 0) {
            for(int i=0; i<parameters.length;i++) {
                queryObject.setParameter(i, parameters[i]);
            }
        }
        return queryObject;
    }
    
    @SuppressWarnings("unchecked")
    public <K> List<K> executeGenericQuery(String query, Object[] parameters, Class<K> clazz) {
        final List<Object[]> objs = executeQuery(query, parameters);
        final List<K> result = new ArrayList<K>();
        
        for (Object o : objs) {
            result.add((K)o);
        }
        
        return result;
    }
    
    public List<Object[]> executeQuery(String query, Object[] parameters) {
        try {
            
            Query queryObject = prepareQuery(query, parameters);
            @SuppressWarnings("unchecked")
            List<Object[]> list = queryObject.list();
            if (list == null) {
                return new ArrayList<Object[]>();
            } else {
                return list;
            }
        } catch (RuntimeException re) {
            throw re;
        }
    }
    
    public SQLQuery prepareSqlQuery(String query, Map<String, Object> parameters) {
        SQLQuery queryObject = getSession().createSQLQuery(query);
        for (String key : parameters.keySet()) {
            queryObject.setParameter(key, parameters.get(key));
        }
        return queryObject;
    }
    
    public SQLQuery prepareSqlQuery(String query, List<Object> parameters) {
        SQLQuery queryObject = getSession().createSQLQuery(query);
        int i=0;
        for (Object para : parameters) {
            queryObject.setParameter(i, para);
            i++;
        }
        return queryObject;
    }
    
    @SuppressWarnings("unchecked")
    public <K> List<K> executeGenericPagedSelect(Integer start, Integer pageSize, Class<K> clazz, String orderBy, boolean asc) {
        final Criteria c = getSession().createCriteria(clazz);
        
        c.setFirstResult(start);
        c.setMaxResults(pageSize);
        c.setFetchSize(pageSize);
        
        if (StringUtils.isNotEmpty(orderBy)) {
            c.addOrder(asc ? Order.asc(orderBy) : Order.desc(orderBy));
        }
        
        List<Object[]> list = c.list();
        if (list == null) {
            return new ArrayList<K>();
        }
        
        return (List<K>) list;
    }
    
    public <K> List<K> executeGenericSqlQuery(String query, Class<K> clazz, Map<String, Object> params) {
        return executeGenericSqlQueryPaged(-1, -1, query, clazz, params);
    }

    @SuppressWarnings("unchecked")
    public <K> List<K> executeGenericSqlQueryPaged(Integer start, Integer pageSize,
            String query, Class<K> clazz, Map<String, Object> params) {
        SQLQuery qry = prepareSqlQuery(query, params);
        qry.addEntity(clazz);
        if (start > -1) {
            qry.setFirstResult(start);
            qry.setMaxResults(pageSize);
            qry.setFetchSize(pageSize);
        }
        
        List<Object[]> list = qry.list();
        if (list == null) {
            return new ArrayList<K>();
        }
        
        return (List<K>) list;
    }
    
    @SuppressWarnings("unchecked")
    public List<Object[]> executeSqlQueryPaged(Integer start, Integer pageSize,
            String query, List<Object> params) {
        SQLQuery qry = prepareSqlQuery(query, params);
        if (start > -1) {
            qry.setFirstResult(start);
            qry.setMaxResults(pageSize);
            qry.setFetchSize(pageSize);
        }
        
        List<Object[]> list = qry.list();
        if (list == null) {
            return new ArrayList<Object[]>();
        }
        
        return list;
    }
    
    @SuppressWarnings("unchecked")
    public List<Object> executeSqlQueryPagedSingleColumn(Integer start, Integer pageSize,
            String query, List<Object> params) {
        SQLQuery qry = prepareSqlQuery(query, params);
        if (start > -1) {
            qry.setFirstResult(start);
            qry.setMaxResults(pageSize);
            qry.setFetchSize(pageSize);
            
        }
        
        List<Object> list = qry.list();
        if (list == null) {
            return new ArrayList<Object>();
        }
        
        return list;
    }

    public Object executeSqlQueryScalar(String query, Map<String, Object> params) {
        SQLQuery qry = prepareSqlQuery(query, params);
        
        return qry.uniqueResult();
    }
    
    public Object executeSqlQueryScalar(String query, List<Object> params) {
        SQLQuery qry = prepareSqlQuery(query, params);
        
        return qry.uniqueResult();
    }
    //TODO Create methods for finding by multiple properties
}
