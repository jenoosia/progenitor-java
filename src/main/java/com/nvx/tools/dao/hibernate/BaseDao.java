package com.nvx.tools.dao.hibernate;

import java.io.Serializable;
import java.util.List;

import com.googlecode.genericdao.dao.hibernate.GenericDAO;

/**
 * Interface from where the Project DAO interfaces must extend.
 * 
 * @author Jensen
 *
 * @param <T>
 * @param <ID>
 */
public interface BaseDao<T, ID extends Serializable> extends GenericDAO<T, ID> {
    
    public int removeAll();
    public boolean exists(String propertyName, Object value);
    public int count();
    public int count(String propertyName, Object value);
    public T findUniqueByProperty(String propertyName, Object value);
    public List<T> findByProperty(String propertyName, Object value);
    public List<T> findByExample(T exampleObj, boolean excludeZeroes, String[] excludeProps);
    public List<T> findAll();
}
