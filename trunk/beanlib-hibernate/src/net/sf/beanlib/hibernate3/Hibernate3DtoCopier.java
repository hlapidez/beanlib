/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.beanlib.hibernate3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.beanlib.CollectionPropertyName;
import net.sf.beanlib.hibernate.HibernateBeanReplicator;
import net.sf.beanlib.hibernate.HibernatePropertyFilter;
import net.sf.beanlib.hibernate.UnEnhancer;
import net.sf.beanlib.provider.collector.ProtectedSetterMethodCollector;
import net.sf.beanlib.spi.DetailedPropertyFilter;
import net.sf.beanlib.spi.PropertyFilter;

import org.apache.commons.lang.ArrayUtils;

/**
 * Hibernate 3 Data Transfer Object Copier.
 * <p>
 * This class provides a simplified (but limited) API for the common use cases
 * to conveniently replicate Hibernate objects 
 * that follow the JavaBean getter/setter convention.
 * <p>
 * Those application specific Hibernate objects would typically 
 * have the same package prefix.
 * <p>  
 * The replication is typically recursive in that 
 * the whole object graph of the input object is replicated into an equivalent output object graph, 
 * resolving circular references, and eager fetching proxied instances as necessary.
 * 
 * However, the exact behavior of the replication process can be controlled 
 * and/or supplemented by the client code via 3 main options:
 * <p>
 * <ol>
 * <li>Specifying an application package prefix: 
 * property with a type of an entity bean class with package name that matchs the prefix will be replicated;</li>
 * <li>Specifying a set of entity bean classes: property with a type of an entity bean class 
 * that is part of the set will be replicated;</li>
 * <li>Specifying a set of collection and map properties that will be replicated;</li>
 * </ul>
 * </ol>
 * 
 * For more advanced options and more control, 
 * consider using {@link Hibernate3BeanReplicator} directly.
 * 
 * @see Hibernate3BeanReplicator
 * 
 * @author Joe D. Velopar
 */
public class Hibernate3DtoCopier
{
    /** 
     * An entity bean under a package with a name 
     * that matches this prefix will be included for replication,
     * eagerly fetched if necessary.
     */ 
    private final String applicationPackagePrefix; 

    /** Constructs with application package prefix disabled. */
    public Hibernate3DtoCopier() {
        this.applicationPackagePrefix = "#";    // By default no application package is specified.
    }
	
    /** 
     * Constructs with an application package prefix.
     * @see #applicationPackagePrefix
     */
    public Hibernate3DtoCopier(String applicationPackagePrefix) {
        this.applicationPackagePrefix = applicationPackagePrefix;
    }
	
    /** 
     * Constructs with an application package prefix, 
     * and a sample application class in a package with such prefix 
     * for sanity checking purposes.
     * 
     * @see #applicationPackagePrefix
     * 
     * @throws IllegalArgumentException if the given application package prefix 
     * does not match the package of the given sample application class.
     */
    public Hibernate3DtoCopier(String applicationPackagePrefix, Class<?> applicationSampleClass) 
    {
        this.applicationPackagePrefix = applicationPackagePrefix;
        
        if (applicationSampleClass != null) {
            String thisPackageName = org.apache.commons.lang.ClassUtils.getPackageName(applicationSampleClass);
                    
            if (!thisPackageName.startsWith(applicationPackagePrefix)) {
                throw new IllegalArgumentException(
                    "The specified application package prefix " + applicationPackagePrefix 
                    + " is not consistent with the given sample application class " + applicationSampleClass);
            }
        }
    }
    
    protected Hibernate3BeanReplicator createHibernateBeanReplicator() {
        return new Hibernate3BeanReplicator();
    }
    
    /** Returns a DTO by deep cloning the given Hibernate bean. */
    public <T> T hibernate2dtoFully(Object entityBean) {
        return (T)(entityBean == null 
                   ? null 
                   : createHibernateBeanReplicator()
                            .initPropertyFilter(new HibernatePropertyFilter(applicationPackagePrefix))
                            .copy(entityBean));
    }
    
    /** Returns a list of DTO's by deep cloning the given collection of Hibernate beans. */
    public List<?> hibernate2dtoFully(Collection<?> hibernateBeans) {
        if (hibernateBeans == null)
            return null;
        List<Object> list = new ArrayList<Object>(hibernateBeans.size());
        HibernateBeanReplicator replicator = createHibernateBeanReplicator()
                                                .initPropertyFilter(new HibernatePropertyFilter(applicationPackagePrefix));
        
        for (Object obj : hibernateBeans)
            list.add(replicator.deepCopy(obj));
        return list;
    }
    
    /** 
     * Returns a DTO by cloning portion of the object graph of the given Hibernate bean,
     * excluding all collection and map properties, and including only those properties
     * with package names that match the application package prefix.
     *
     * @param entityBean Hibernate entity bean to be cloned
     * 
     * @see #applicationPackagePrefix
     */
    public <T> T hibernate2dto(Object entityBean) 
    {
        return (T)hibernate2dto(UnEnhancer.getActualClass(entityBean), entityBean);
    }
    
    /** 
     * Returns a DTO of the specified target entity type
     * by cloning portion of the object graph of the given Hibernate bean,
     * excluding all collection and map properties, and including only those properties
     * with package names that match the application package prefix.
     *
     * @param targetEntityType target entity type
     * @param entityBean Hibernate entity bean to be cloned
     * 
     * @see #applicationPackagePrefix
     */
    public <E,T> E hibernate2dto(Class<E> targetEntityType, T entityBean) 
    {
        if (entityBean == null)
            return null;
        return copy(targetEntityType, entityBean, ArrayUtils.EMPTY_CLASS_ARRAY);
    }
    
    /** 
     * Returns a DTO by cloning portion of the object graph of the given Hibernate entity bean.
     * 
     * @param entityBean given Hibernate entity bean to be cloned
     * @param interestedEntityTypes properties of these types will be included for cloning
     * @param collectionPropertyNames collection and map properties to be included in the cloning
     */
    public <T> T hibernate2dto(T entityBean, 
        Class<?>[] interestedEntityTypes, CollectionPropertyName[] collectionPropertyNames) 
    {
        return (T)hibernate2dto(
                    UnEnhancer.getActualClass(entityBean), 
                    entityBean, interestedEntityTypes, collectionPropertyNames);
    }
    
    /** 
     * Returns a DTO of the specified target entity type
     * by cloning portion of the object graph of the given Hibernate entity bean.
     * 
     * @param targetEntityType target entity type
     * @param entityBean given Hibernate entity bean to be cloned
     * @param interestedEntityTypes properties of these types will be included for cloning
     * @param collectionPropertyNames collection and map properties to be included in the cloning
     */
    public <E, T> E hibernate2dto(Class<E> targetEntityType, T entityBean,
        Class<?>[] interestedEntityTypes, CollectionPropertyName[] collectionPropertyNames) 
    {
        if (entityBean == null)
            return null;
        return copy(targetEntityType, entityBean, interestedEntityTypes, collectionPropertyNames);
    }
    
    /** 
     * Returns a list of DTO's by cloning portion of the object graph 
     * of the given collection of Hibernate entity beans,
     * excluding all collection and map properties, and including only those properties
     * with package names that match the application package prefix.
     * 
     * @param hibernateBeans given collection of Hibernate Beans.
     */
    public List<?> hibernate2dto(Collection<?> hibernateBeans) 
    {
        if (hibernateBeans == null)
            return null;
            
        List<Object> list = new ArrayList<Object>(hibernateBeans.size());
        
        for (Object entityBean : hibernateBeans) {
            Object to = copy(entityBean, ArrayUtils.EMPTY_CLASS_ARRAY);
            list.add(to);
        }
        return list;
    }
    
    /** 
     * Returns a list of DTO's of the specified target entity type
     * by cloning portion of the object graph of the given collection of Hibernate entity beans.
     *  
     * @param hibernateBeans given collection of Hibernate entity beans to be cloned
     * @param interestedEntityTypes properties of these types will be included for cloning
     * @param collectionPropertyNames collection and map properties to be included in the cloning
     */
    public <E> List<E> hibernate2dto(Class<E> targetEntityType, 
        Collection<?> hibernateBeans, Class<?>[] interestedEntityTypes, CollectionPropertyName[] collectionPropertyNames)
    {
        if (hibernateBeans == null)
            return null;
        List<E> list = new ArrayList<E>(hibernateBeans.size());
        
        for (Object entityBean : hibernateBeans) {
            E to = copy(targetEntityType, entityBean, interestedEntityTypes, collectionPropertyNames);
            list.add(to);
        }
        return list;
    }
    
    /** 
     * Returns a DTO by cloning the object graph excluding all collection and map properties.
     *  
     * @param from given entity bean to be cloned
     * @param interestedEntityTypes properties of these types will be included for cloning
     */
    private Object copy(Object from, Class<?>[] interestedEntityTypes) 
    {
        if (from == null)
            return null;
        return copy(UnEnhancer.getActualClass(from), 
                    from, interestedEntityTypes, CollectionPropertyName.EMPTY_ARRAY);
    }
    
    /** 
     * Returns a DTO of the specified target entity type 
     * by cloning the object graph excluding all collection and map properties.
     *  
     * @param targetEntityType target entity type
     * @param from given entity bean to be cloned
     * @param interestedEntityTypes properties of these types will be included for cloning
     */
    private <E> E copy(Class<E> targetEntityType, Object from, Class<?>[] interestedEntityTypes) 
    {
        if (from == null)
            return null;
        return copy(targetEntityType, from, interestedEntityTypes, CollectionPropertyName.EMPTY_ARRAY);
    }

    /** 
     * Returns a DTO of the specified target entity type
     * by cloning portion of the object graph of the given Hibernate entity bean.
     *  
     * @param targetEntityType target entity type
     * @param from given Hibernate entity bean to be cloned
     * @param interestedEntityTypes properties of these types will be included for cloning
     * @param collectionPropertyNames collection and map properties to be included in the cloning
     */
    @SuppressWarnings("unchecked")
    private <E> E copy(Class<E> targetEntityType, Object from, 
        Class<?>[] interestedEntityTypes, CollectionPropertyName[] collectionPropertyNames)
    {
        if (from == null)
            return null;
        HibernateBeanReplicator replicator = createHibernateBeanReplicator();
        // Assumes all entity classes
        Set<Class<?>> entityBeanClassSet = null;
        
        if (interestedEntityTypes != null) {
            if (interestedEntityTypes.length == 0)
                // no other entity classes
                entityBeanClassSet = Collections.emptySet();
            else
                // entity classes explicitly specified
                entityBeanClassSet = new HashSet<Class<?>>(Arrays.asList(interestedEntityTypes));
        }
        // Assumes all Collection properties
        Set<CollectionPropertyName> collectionPropertyNameSet = null;
        
        if (collectionPropertyNames != null) {
            if (collectionPropertyNames.length == 0)
                // No Collection properties.
                collectionPropertyNameSet = Collections.emptySet();
            else 
                // Collection properties explicitly specified. 
                collectionPropertyNameSet = new HashSet<CollectionPropertyName>(Arrays.asList(collectionPropertyNames));
        }
        PropertyFilter propertyFilter = new HibernatePropertyFilter(
                                            applicationPackagePrefix, entityBeanClassSet, collectionPropertyNameSet, null);
        replicator
          .initPropertyFilter(propertyFilter)
          .initDetailedPropertyFilter(DetailedPropertyFilter.ALWAYS_PROPAGATE)
          .initSetterMethodCollector(new ProtectedSetterMethodCollector())
          ;
        return (E)replicator.copy(from, 
                                  UnEnhancer.unenhanceClass(targetEntityType));
        
    }
}
