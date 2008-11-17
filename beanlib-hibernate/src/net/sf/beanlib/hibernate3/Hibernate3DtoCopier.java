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
import net.sf.beanlib.hibernate.UnEnhancer;
import net.sf.beanlib.provider.collector.ProtectedSetterMethodCollector;
import net.sf.beanlib.spi.PropertyFilter;
import net.sf.beanlib.spi.DetailedPropertyFilter;

import org.apache.commons.lang.ArrayUtils;

/**
 * Convenient class for converting application specific 
 * Hibernate 3 persistence object to DTO.
 * 
 * @author Joe D. Velopar
 */
public class Hibernate3DtoCopier
{
//    /**
//     * Factory for {@link Hibernate3DtoCopier}.
//     * 
//     * @author Joe D. Velopar
//     */
//    public static class Factory 
//    {
//        public static Hibernate3DtoCopier getInstance(
//                String applicationPackagePrefix, Class<?> applicationSampleClass) 
//        {
//            return new Hibernate3DtoCopier().init(applicationPackagePrefix, applicationSampleClass);
//        }
//
//        public static Hibernate3DtoCopier getInstance(String applicationPackagePrefix) {
//            return new Hibernate3DtoCopier().init(applicationPackagePrefix, null);
//        }
//    }
////    private static final Logger log = Logger.getLogger(Hibernate3DtoCopier.class);
    private final String applicationPackagePrefix; 

    /** Must be constructed only via the factory. */
	public Hibernate3DtoCopier() {
	    this.applicationPackagePrefix = "#";    // By default no application package is specified.
	}
	
	public Hibernate3DtoCopier(String applicationPackagePrefix) {
	    this.applicationPackagePrefix = applicationPackagePrefix;
	}
	
    public Hibernate3DtoCopier(String applicationPackagePrefix, Class<?> applicationSampleClass) {
        this.applicationPackagePrefix = applicationPackagePrefix;
        
        if (applicationSampleClass != null) {
            String thisPackageName = org.apache.commons.lang.ClassUtils.getPackageName(applicationSampleClass);
                    
            if (!thisPackageName.startsWith(applicationPackagePrefix)) {
                throw new IllegalStateException(
                    "The specified application package prefix " + applicationPackagePrefix 
                    + " is not consistent with the given sample application class " + applicationSampleClass);
            }
        }
    }
    
    protected Hibernate3BeanReplicator createHibernateBeanReplicator() {
        return new Hibernate3BeanReplicator();
    }
//    /** 
//     * Used to specify the application package prefix, with a sample application class for verification purposes. 
//     */
//    protected Hibernate3DtoCopier init(String applicationPackagePrefix, Class<?> applicationSampleClass) {
//        this.applicationPackagePrefix = applicationPackagePrefix;
//        
//        if (applicationSampleClass != null) {
//            String thisPackageName = org.apache.commons.lang.ClassUtils.getPackageName(applicationSampleClass);
//                    
//            if (!thisPackageName.startsWith(applicationPackagePrefix)) {
//                throw new IllegalStateException(
//                    "The specified application package prefix " + applicationPackagePrefix 
//                    + " is not consistent with the given sample application class " + applicationSampleClass);
//            }
//        }
//        return this;
//    }
    
    /** Returns a DTO by deep cloning the given Hibernate bean. */
    public <T> T hibernate2dtoFully(Object entityBean) {
        return (T)(entityBean == null 
                   ? null 
                   : createHibernateBeanReplicator()
                            .initPropertyFilter(new Hibernate3DtoPropertyFilter()
                                                    .init(this))
                            .copy(entityBean));
    }
    
    /** Returns a list of DTO's by deep cloning the given collection of Hibernate beans. */
    public List<?> hibernate2dtoFully(Collection<?> hibernateBeans) {
        if (hibernateBeans == null)
            return null;
        List<Object> list = new ArrayList<Object>(hibernateBeans.size());
        HibernateBeanReplicator replicator = createHibernateBeanReplicator()
                                                .initPropertyFilter(new Hibernate3DtoPropertyFilter()
                                                                         .init(this));
        
        for (Object obj : hibernateBeans)
            list.add(replicator.deepCopy(obj));
        return list;
    }
    
    /** 
     * Returns a DTO by cloning portion of the object graph of the given Hibernate bean.
     * @param entityBean given Hibernate Bean
     */
    public <T> T hibernate2dto(Object entityBean) 
    {
        return (T)hibernate2dto(UnEnhancer.getActualClass(entityBean), entityBean);
    }
    
    public <E,T> E hibernate2dto(Class<E> targetEntityType, T entityBean) 
    {
        if (entityBean == null)
            return null;
        return copy(targetEntityType, entityBean, ArrayUtils.EMPTY_CLASS_ARRAY);
    }
    
    /** 
     * Returns a DTO by cloning portion of the object graph of the given Hibernate bean.
     * @param entityBean given Hibernate Bean
     * @param collectionPropertyNames set properties to be included in the object graph
     */
    public <T> T hibernate2dto(T entityBean, 
        Class<?>[] interestedEntityTypes, CollectionPropertyName[] collectionPropertyNames) 
    {
        return (T)hibernate2dto(
                    UnEnhancer.getActualClass(entityBean), 
                    entityBean, interestedEntityTypes, collectionPropertyNames);
    }
    
    public <E, T> E hibernate2dto(Class<E> targetEntityType, T entityBean,
        Class<?>[] interestedEntityTypes, CollectionPropertyName[] collectionPropertyNames) 
    {
        if (entityBean == null)
            return null;
        return copy(targetEntityType, entityBean, interestedEntityTypes, collectionPropertyNames);
    }
    
    /** 
     * Returns a list of DTO's by cloning portion of the object graph of the given collection of Hibernate beans. 
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
     * Returns a list of DTO's by cloning portion of the object graph of the given collection of Hibernate beans. 
     * @param hibernateBeans given collection of Hibernate Beans.
     */
    public <E> List<E> hibernate2dto(Class<E> targetEntityType, 
        Collection<?> hibernateBeans, Class<?>[] interestedEntityTypes, CollectionPropertyName[] collectionPropertyNameArray)
    {
        if (hibernateBeans == null)
            return null;
        List<E> list = new ArrayList<E>(hibernateBeans.size());
        
        for (Object entityBean : hibernateBeans) {
            E to = copy(targetEntityType, entityBean, interestedEntityTypes, collectionPropertyNameArray);
            list.add(to);
        }
        return list;
    }
    
    /** Returns a DTO by cloning the object graph excluding all collection and map properties. */
    private Object copy(Object from, Class<?>[] entityBeanClassArray) 
    {
        if (from == null)
            return null;
        return copy(UnEnhancer.getActualClass(from), 
                    from, entityBeanClassArray, CollectionPropertyName.EMPTY_ARRAY);
    }
    
    private <E> E copy(Class<E> targetEntityType, Object from, Class<?>[] entityBeanClassArray) 
    {
        if (from == null)
            return null;
        return copy(targetEntityType, from, entityBeanClassArray, CollectionPropertyName.EMPTY_ARRAY);
    }

    @SuppressWarnings("unchecked")
    private <E> E copy(Class<E> targetEntityType, Object from, 
        Class<?>[] entityBeanClassArray, CollectionPropertyName[] collectionPropertyNameArray)
    {
        if (from == null)
            return null;
        HibernateBeanReplicator replicator = createHibernateBeanReplicator();
        // Assumes all entity classes
        Set<Class<?>> entityBeanClassSet = null;
        
        if (entityBeanClassArray != null) {
            if (entityBeanClassArray.length == 0)
                // no other entity classes
                entityBeanClassSet = Collections.emptySet();
            else
                // entity classes explicitely specified
                entityBeanClassSet = new HashSet<Class<?>>(Arrays.asList(entityBeanClassArray));
        }
        // Assumes all Collection properties
        Set<CollectionPropertyName> collectionPropertyNameSet = null;
        
        if (collectionPropertyNameArray != null) {
            if (collectionPropertyNameArray.length == 0)
                // No Collection properties.
                collectionPropertyNameSet = Collections.emptySet();
            else 
                // Collection properties explicitly specified. 
                collectionPropertyNameSet = new HashSet<CollectionPropertyName>(Arrays.asList(collectionPropertyNameArray));
        }
        PropertyFilter propertyFilter = new Hibernate3DtoPropertyFilter(entityBeanClassSet, collectionPropertyNameSet)
                                            .init(this);
        replicator
          .initPropertyFilter(propertyFilter)
          .initDetailedPropertyFilter(DetailedPropertyFilter.ALWAYS_PROPAGATE)
          .initSetterMethodCollector(new ProtectedSetterMethodCollector())
          ;
        return (E)replicator.copy(from, 
                                  UnEnhancer.unenhanceClass(targetEntityType));
        
    }
    
    /** Returns true iff c is an application class. */
    public boolean isApplicationClass(Class<?> c) {
        if (c == null)
            return false;
        String pn = org.apache.commons.lang.ClassUtils.getPackageName(c);
        // TODO: can pn ever be null ?
        return pn.startsWith(applicationPackagePrefix);
    }
}
