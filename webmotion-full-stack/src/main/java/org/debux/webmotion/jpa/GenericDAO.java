/*
 * #%L
 * WebMotion full stack
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2011 - 2012 Debux
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package org.debux.webmotion.jpa;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.debux.webmotion.server.WebMotionException;

/**
 *
 * @author julien
 */
public class GenericDAO {
    
    protected BeanUtilsBean beanUtil;
    protected ConvertUtilsBean convertUtils;

    protected EntityManager manager;
    protected String entityName;
    protected Class<? extends IdentifiableEntity> entityClass;
    
    public GenericDAO(EntityManager manager, String entityName) {
        beanUtil = BeanUtilsBean.getInstance();
        convertUtils = beanUtil.getConvertUtils();
        
        try {
            entityClass = (Class<? extends IdentifiableEntity>) Class.forName(entityName);
        } catch (ClassNotFoundException cnfe) {
            throw new WebMotionException("Invalid class name", cnfe);
        }
    }
    
    public void create(Map<String, String[]> parameters) {
        IdentifiableEntity entity = extract(parameters);
        manager.persist(entity);
    }
            
    public void update(Map<String, String[]> parameters) {
        IdentifiableEntity entity = extract(parameters);
        manager.merge(entity);
    }
    
    public void delete(String id) {
        IdentifiableEntity entity = find(id);
        manager.remove(entity);
    }
    
    protected IdentifiableEntity extract(Map<String, String[]> parameters) {
        try {
            IdentifiableEntity entity = entityClass.newInstance();

            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {

                String name = field.getName();
                Class<?> type = field.getType();
                String[] values = parameters.get(name);

                if (type.isAnnotationPresent(javax.persistence.Entity.class)) {
                    for (String value : values) {
                        Object reference = manager.find(type, value);
                        beanUtil.setProperty(entity, name, reference);
                    }

                } else {
                    Object converted = convertUtils.convert(values, type);
                    beanUtil.setProperty(entity, name, converted);
                }
            }

            return entity;
            
        } catch (InstantiationException ie) {
            throw new WebMotionException("Not default constructor", ie);
        } catch (IllegalAccessException iae) {
            throw new WebMotionException("Error during create instance", iae);
        } catch (InvocationTargetException ite) {
            throw new WebMotionException("Error during set field on instance", ite);
        }
    }
    
    protected IdentifiableEntity find(String id) {
        IdentifiableEntity entity = manager.find(entityClass, id);
        manager.detach(entity);
        return entity;
    }
    
    protected List query(String name) {
        Query query = manager.createNamedQuery(name);
        return query.getResultList();
    }
    
}
