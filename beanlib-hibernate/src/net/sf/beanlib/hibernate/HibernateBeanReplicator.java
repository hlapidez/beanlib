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

import net.jcip.annotations.NotThreadSafe;
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
 * Hibernate Bean Replicator.  
 * This was originally the base class for both the Hibernate 2.x and Hibernate 3.x
 * replicators, but now Hibernate 2 is no longer supported.
 * 
 * @see net.sf.beanlib.hibernate3.Hibernate3BeanReplicator 
 *  
 * @author Joe D. Velopar
 */
@NotThreadSafe
public class HibernateBeanReplicator 
{
//    private final Log log = LogFactory.getLog(this.getClass());
    
    /** Used to do the heavy lifting of Hibernate object transformation and replication. */
    private final BeanTransformerSpi hibernateBeanTransformer;

    /**
     * The set of entity bean classes for matching properties that will be replicated.
     * Null means all whereas empty means none.
     */
    private Set<Class<?>> entityBeanClassSet;

    /**
     * The set of collection and map properties that will be replicated.
     * Null means all whereas empty means none.
     */
    private Set<? extends CollectionPropertyName> collectionPropertyNameSet;
    
    /** Used to control what properties get propagated across and what get skipped. */
    private BeanPopulatable beanPopulatable; 
    
    /**
     * In the context of using 
     * {@link HibernateBeanPopulatableSupport}, which is the default,
     * for controlling the propagation of properties, 
     * a vetoer can be used to further veto the propagation of specific properties.
     * 
     * @see HibernateBeanPopulatableSupport
     */
    private BeanPopulatable vetoer; 

    /**
     * You probably want to construct a 
     * {@link net.sf.beanlib.hibernate3.Hibernate3BeanReplicator Hibernate3BeanReplicator}
     * directly instead of this ?
     */
    public HibernateBeanReplicator(BeanTransformerSpi hibernateBeanTransformer) 
    {
        if (hibernateBeanTransformer == null)
            throw new IllegalArgumentException("Argument hibernateBeanTransformer must not be null");
        this.hibernateBeanTransformer = hibernateBeanTransformer;
    }

    /** 
     * Returns a copy of the given object.
     * 
     * The exact behavior of the copy depends on how the replicator has been configured 
     * via the init* methods.
     * <p>
     * In the case when none of the init* methods is invoked, this method behaves
     * very similar to {@link HibernateBeanReplicator#deepCopy(Object)}, except only public
     * setter methods will be used for property propagation, 
     * instead of both the public and protected setter
     * methods as it would be the case in invoking the deep copy method.
     * 
     * @param <T> type of the given object.
     * @param from given object.
     */
    public final <T> T copy(T from) {
        return (T)(from == null 
                         ? null 
                         : copy(from, UnEnhancer.getActualClass(from)));
    }

    /** 
     * Returns an instance of the given class with values copied from the given object.
     * 
     * The exact behavior of the copy depends on how the replicator has been configured 
     * via the init* methods.
     * <p>
     * In the case when none of the init* methods is invoked, this method behaves
     * very similar to {@link HibernateBeanReplicator#deepCopy(Object, Class)}, except only public
     * setter methods will be used for property propagation, 
     * instead of both the public and protected setter
     * methods as it would be the case in invoking the deep copy method.
     * 
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
            return hibernateBeanTransformer.transform(from, toClass, null);
        } finally {
            hibernateBeanTransformer.reset();
        }
    }
    
    /** 
     * Convenient method to deep copy the given object using the default behavior.
     * <p>
     * Notes:
     * <ol>
     * <li>
     * Use {@link HibernateBeanReplicator#copy(Object)} instead of this method 
     * if you want to plug in a different 
     * {@link DetailedBeanPopulatable} or {@link BeanMethodCollector}.
     * </li>
     * <li>This method will cause both the public and protected setter methods 
     * to be invoked for property propagation.
     * </li>
     * </ol>
     * 
     * @param <T> from object type.
     * @param from given object to be copied.
     * @return a deep clone of the from object.
     */
    public final <T> T deepCopy(T from) {
        return (T)(from == null 
                         ? null 
                         : deepCopy(from, UnEnhancer.getActualClass(from)));
    }

    /** 
     * Convenient method to deep copy the given object 
     * to an instance of the given class using the default behavior.
     * <p>
     * Notes:
     * <ol>
     * <li>
     * Use {@link HibernateBeanReplicator#copy(Object, Class)} instead of this method 
     * if you want to plug in a different 
     * {@link DetailedBeanPopulatable} or {@link BeanMethodCollector}.
     * </li>
     * <li>This method will cause both the public and protected setter methods 
     * to be invoked for property propagation.
     * </li>
     * </ol>
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
     * Shallow copy means skipping those properties that are of type collection, map 
     * or under a package that doesn't start with "java.".
     * <p>
     * Notes:
     * <ol>
     * <li>
     * Use {@link HibernateBeanReplicator#copy(Object)} instead of this method 
     * if you want to plug in a different 
     * {@link DetailedBeanPopulatable} or {@link BeanMethodCollector}.
     * </li>
     * <li>This method will cause both the public and protected setter methods 
     * to be invoked for property propagation.
     * </li>
     * </ol>
     * 
     * @see HibernateBeanPopulatableSupport
     * 
     * @param <T> from object type.
     * @param from given object to be copied.
     * @return a shallow clone of the from object.
     */
    public final <T> T shallowCopy(T from) {
        return (T)(from == null 
                         ? null 
                         : shallowCopy(from, UnEnhancer.getActualClass(from)));
    }

    /** 
     * Convenient method to shallow copy the given object 
     * to an instance of the given class using the default behavior.
     * Shallow copy means skipping those properties that are of type collection, map 
     * or under a package that doesn't start with "java.".
     * <p>
     * Notes:
     * <ol>
     * <li>
     * Use {@link HibernateBeanReplicator#copy(Object, Class)} instead of this method 
     * if you want to plug in a different 
     * {@link DetailedBeanPopulatable} or {@link BeanMethodCollector}.
     * </li>
     * <li>This method will cause both the public and protected setter methods 
     * to be invoked for property propagation.
     * </li>
     * </ol>
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
        this.hibernateBeanTransformer.initDetailedBeanPopulatable(null);
        this.hibernateBeanTransformer.initSetterMethodCollector(ProtectedSetterMethodCollector.inst);        
    }

    /** 
     * Initializes with a custom transformer factory.
     */
    public final HibernateBeanReplicator initCustomTransformerFactory(CustomBeanTransformerSpi.Factory customTransformerFactory) {
        this.hibernateBeanTransformer.initCustomTransformerFactory(customTransformerFactory);
        return this;
    }

    /**
     * Returns the set of entity bean classes for matching properties that will be replicated.
     * Null means all whereas empty means none.
     */
    public final Set<Class<?>> getEntityBeanClassSet() {
        return entityBeanClassSet;
    }

    /**
     * Used to specify the set of entity beans to be populated.
     * 
     *  @param entityBeanClassSet the set of entity beans to be populated;
     *  or null if all entity bean are to be populated.
     *  @return the current HibernateBeanReplicator instance for command chaining.
     */
    public final HibernateBeanReplicator initEntityBeanClassSet(Set<Class<?>> entityBeanClassSet) {
        this.entityBeanClassSet = entityBeanClassSet;
        return this;
    }

    public final Set<? extends CollectionPropertyName> getCollectionPropertyNameSet() {
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
    
    /**
     * Returns the vetoer in the context of using 
     * {@link HibernateBeanPopulatableSupport}, which is the default,
     * for controlling the propagation of properties. 
     * A vetoer is used to further veto the propagation of specific properties.
     * <p>
     * Irrelevant if {@link HibernateBeanPopulatableSupport} is not used.
     * 
     * @see HibernateBeanPopulatableSupport
     */
    public final BeanPopulatable getVetoer() {
        return vetoer;
    }

    /**
     * This method is only relevant if {@link HibernateBeanPopulatableSupport}, which is the default,
     * is used for controlling the propagation of properties.
     * 
     * @param vetoer can be used to further veto the propagation of specific properties.
     * 
     * @see HibernateBeanPopulatableSupport
     */
    public final HibernateBeanReplicator initVetoer(BeanPopulatable vetoer) {
        this.vetoer = vetoer;
        return this;
    }

    // ========================== Bean Population configuration ========================== 

    /**
     * Returns the populator that is used to control 
     * what properties get propagated across and what get skipped.
     */
    public final BeanPopulatable getBeanPopulatable() {
        return beanPopulatable;
    }
    
    /**
     * Note this method is only applicable if either the {@link #copy(Object)} 
     * or {@link #copy(Object, Class)} is directly invoked, 
     * and is ignored otherwise (ie ignored if deep or shallow copy is invoked instead).
     * 
     * @param beanPopulatable is similar to {@link DetailedBeanPopulatable} but with a simpler API
     * that is used to control whether a specific JavaBean property should be propagated
     * from a source bean to a target bean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public final HibernateBeanReplicator initBeanPopulatable(BeanPopulatable beanPopulatable) {
        this.beanPopulatable = beanPopulatable;
        return this;
    }

    /**
     * Note this method is only applicable if either the {@link #copy(Object)} 
     * or {@link #copy(Object, Class)} is directly invoked, 
     * and is ignored otherwise (ie ignored if deep or shallow copy is invoked instead).
     * 
     * @param detailedBeanPopulatable is used to control whether a specific JavaBean property
     * should be propagated from the source bean to the target bean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public final HibernateBeanReplicator initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable) 
    {
        this.hibernateBeanTransformer.initDetailedBeanPopulatable(detailedBeanPopulatable);
        return this;
    }
    
    /**
     * @param beanSourceHandler can be used to act as a call-back 
     * (to produce whatever side-effects deemed necessary)
     * after the property value has been retrieved from the source bean, 
     * but before being propagated across to the target bean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public final HibernateBeanReplicator initBeanSourceHandler(BeanSourceHandler beanSourceHandler) {
        this.hibernateBeanTransformer.initBeanSourceHandler(beanSourceHandler);
        return this;
    }

    /**
     * Used to control whether debug messages should be logged.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public final HibernateBeanReplicator initDebug(boolean debug) {
        this.hibernateBeanTransformer.initDebug(debug);
        return this;
    }

    /**
     * @param readerMethodFinder can be used to find the property getter methods of a source JavaBean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public final HibernateBeanReplicator initReaderMethodFinder(BeanMethodFinder readerMethodFinder) {
        this.hibernateBeanTransformer.initReaderMethodFinder(readerMethodFinder);
        return this;
    }

    /**
     * Note this method is only applicable if either the {@link #copy(Object)} 
     * or {@link #copy(Object, Class)} is directly invoked, 
     * and is ignored otherwise (ie ignored if deep or shallow copy is invoked instead).
     * 
     * @param setterMethodCollector can be used to collect the property setter methods 
     * of a target JavaBean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public final HibernateBeanReplicator initSetterMethodCollector(
                                            BeanMethodCollector setterMethodCollector) 
    {
        this.hibernateBeanTransformer.initSetterMethodCollector(setterMethodCollector);
        return this;
    }
}
