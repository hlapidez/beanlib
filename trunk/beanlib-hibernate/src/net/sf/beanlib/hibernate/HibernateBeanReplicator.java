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
package net.sf.beanlib.hibernate;

import java.util.Collections;
import java.util.Set;

import net.sf.beanlib.CollectionPropertyName;
import net.sf.beanlib.provider.collector.ProtectedSetterMethodCollector;
import net.sf.beanlib.spi.BeanMethodCollector;
import net.sf.beanlib.spi.BeanMethodFinder;
import net.sf.beanlib.spi.BeanPopulatable;
import net.sf.beanlib.spi.BeanSourceHandler;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.CustomBeanTransformerSpi;
import net.sf.beanlib.spi.DetailedBeanPopulatable;

/**
 * Hibernate Bean Replicator.  Not thread safe.
 *  
 * @author Joe D. Velopar
 */
public class HibernateBeanReplicator 
{
//    private final Log log = LogFactory.getLog(this.getClass());
    
    private final BeanTransformerSpi hibernateBeanTransformer;

    private Set<Class> entityBeanClassSet;
    private Set<? extends CollectionPropertyName> collectionPropertyNameSet;
    
    private BeanPopulatable beanPopulatable; 
    private BeanPopulatable vetoer; 

    public HibernateBeanReplicator(BeanTransformerSpi hibernateBeanTransformer) 
    {
        if (hibernateBeanTransformer == null)
            throw new IllegalArgumentException("Argument hibernateBeanTransformer must not be null");
        this.hibernateBeanTransformer = hibernateBeanTransformer;
    }

    /** 
     * Returns a copy of the given object.
     * @param <T> type of the given object.
     * @param from given object.
     */
    public final <T> T copy(T from) {
        return (T)(from == null ? null : copy(from, from.getClass()));
    }

    /** 
     * Returns an instance of the given class with values copied from the given object.
     * @param <T> type of the given  object.
     * @param from given object.
     * @param toClass target class of the returned object.
     */
    public final <T> T copy(Object from, Class<T> toClass) {
        if (from == null)
            return null;
        if (this.beanPopulatable == null)
            this.beanPopulatable = new HibernateBeanPopulatableSupport(entityBeanClassSet, collectionPropertyNameSet, vetoer);
        hibernateBeanTransformer.initBeanPopulatable(beanPopulatable);
        try {
            return hibernateBeanTransformer.transform(from, toClass);
        } finally {
            hibernateBeanTransformer.reset();
        }
    }
    
    /** 
     * Convenient method to deep copy the given object using the default behavior.
     * Do not use this method if you want to plug in a different 
     * {@link DetailedBeanPopulatable} or {@link BeanMethodCollector}.
     * 
     * @param <T> from object type.
     * @param from given object to be copied.
     * @return a deep clone of the from object.
     */
    public final <T> T deepCopy(T from) {
        return (T)(from == null ? null : deepCopy(from, from.getClass()));
    }

    /** 
     * Convenient method to deep copy the given object 
     * to an instance of the given class using the default behavior.
     * Do not use this method if you want to plug in a different 
     * {@link DetailedBeanPopulatable} or {@link BeanMethodCollector}.
     * 
     * @param <T> to object type.
     * @param from given object to be copied.
     * @param toClass target class of the returned object.
     * @return an instance of the given class with values deeply copied from the given object.
     */
    public final <T> T deepCopy(Object from, Class<T> toClass) {
        this.entityBeanClassSet = null;
        this.collectionPropertyNameSet = null;
        this.setDefaultBehavior();
        return this.copy(from, toClass);
    }
    
    /** 
     * Convenient method to shallow copy the given object using the default behavior.
     * Do not use this method if you want to plug in a different 
     * {@link DetailedBeanPopulatable} or {@link BeanMethodCollector}.
     * 
     * @param <T> from object type.
     * @param from given object to be copied.
     * @return a shallow clone of the from object.
     */
    public final <T> T shallowCopy(T from) {
        return (T)(from == null ? null : shallowCopy(from, from.getClass()));
    }

    /** 
     * Convenient method to shallow copy the given object 
     * to an instance of the given class using the default behavior.
     * Do not use this method if you want to plug in a different 
     * {@link DetailedBeanPopulatable} or {@link BeanMethodCollector}.
     * 
     * @param <T> to object type.
     * @param from given object to be copied.
     * @return an instance of the given class with values shallow copied from the given object.
     */
    public final <T> T shallowCopy(Object from, Class<T> toClass) {
        this.entityBeanClassSet = Collections.emptySet();
        this.collectionPropertyNameSet = Collections.emptySet();
        this.setDefaultBehavior();
        return this.copy(from, toClass);
    }
    
    private void setDefaultBehavior() {
        this.beanPopulatable = null;
//        this.hibernateBeanTransformer = null;
        this.hibernateBeanTransformer.initDetailedBeanPopulatable(null);
        this.hibernateBeanTransformer.initSetterMethodCollector(ProtectedSetterMethodCollector.inst);        
    }

    public final BeanPopulatable getBeanPopulatable() {
        return beanPopulatable;
    }

    public final HibernateBeanReplicator initBeanPopulatable(BeanPopulatable beanPopulatable) {
        this.beanPopulatable = beanPopulatable;
        return this;
    }

    public final HibernateBeanReplicator initCustomTransformer(CustomBeanTransformerSpi.Factory customTransformerFactory) {
        this.hibernateBeanTransformer.initCustomTransformer(customTransformerFactory);
        return this;
    }

    public final HibernateBeanReplicator initBeanSourceHandler(BeanSourceHandler beanSourceHandler) {
        this.hibernateBeanTransformer.initBeanSourceHandler(beanSourceHandler);
        return this;
    }

    public final HibernateBeanReplicator initDebug(boolean debug) {
        this.hibernateBeanTransformer.initDebug(debug);
        return this;
    }

    public final HibernateBeanReplicator initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable) 
    {
        this.hibernateBeanTransformer.initDetailedBeanPopulatable(detailedBeanPopulatable);
        return this;
    }

    public final Set getEntityBeanClassSet() {
        return entityBeanClassSet;
    }

    /**
     * Used to specify the set of entity beans to be populated.
     * 
     *  @param entityBeanClassSet the set of entity beans to be populated;
     *  or null if all entity bean are to be populated.
     *  @return the current HibernateBeanReplicator instance for command chaining.
     */
    public final HibernateBeanReplicator initEntityBeanClassSet(Set<Class> entityBeanClassSet) {
        this.entityBeanClassSet = entityBeanClassSet;
        return this;
    }

    public final Set getCollectionPropertyNameSet() {
        return collectionPropertyNameSet;
    }

    /**
     * Used to specify the set of Collection fields to be populated.
     * 
     * @param collectionPropertyNameSet the set of Collection fields to be populated;
     * or null if all Collection fields are to be populated.
     * @return the current HibernateBeanReplicator instance for command chaining.
     */
    public final HibernateBeanReplicator initCollectionPropertyNameSet(Set<? extends CollectionPropertyName> collectionPropertyNameSet) 
    {
        this.collectionPropertyNameSet = collectionPropertyNameSet;
        return this;
    }

    public final BeanPopulatable getVetoer() {
        return vetoer;
    }

    public final HibernateBeanReplicator initVetoer(BeanPopulatable vetoer) {
        this.vetoer = vetoer;
        return this;
    }

    public final HibernateBeanReplicator setReaderMethodFinder(BeanMethodFinder readerMethodFinder) {
        this.hibernateBeanTransformer.initReaderMethodFinder(readerMethodFinder);
        return this;
    }

    public final HibernateBeanReplicator initSetterMethodCollector(BeanMethodCollector setterMethodFinder) {
        this.hibernateBeanTransformer.initSetterMethodCollector(setterMethodFinder);
        return this;
    }
}
