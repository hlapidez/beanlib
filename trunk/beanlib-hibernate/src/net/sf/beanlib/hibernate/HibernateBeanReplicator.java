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
import net.sf.beanlib.provider.BeanPopulator;
import net.sf.beanlib.provider.collector.ProtectedSetterMethodCollector;
import net.sf.beanlib.spi.BeanMethodCollector;
import net.sf.beanlib.spi.BeanMethodFinder;
import net.sf.beanlib.spi.BeanPopulationExceptionHandler;
import net.sf.beanlib.spi.BeanPopulatorBaseConfig;
import net.sf.beanlib.spi.BeanPopulatorBaseSpi;
import net.sf.beanlib.spi.BeanSourceHandler;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.ChainedCustomBeanTransformer;
import net.sf.beanlib.spi.CustomBeanTransformerSpi;
import net.sf.beanlib.spi.DetailedPropertyFilter;
import net.sf.beanlib.spi.PropertyFilter;

/**
 * Hibernate Bean Replicator.
 * <p> 
 * This class can be used to conveniently replicate Hibernate objects 
 * that follow the JavaBean getter/setter convention.
 *   
 * The replication is typically recursive in that 
 * the whole object graph of the input object is replicated into an equivalent output object graph, 
 * resolving circular references, and eager fetching proxied instances as necessary.
 * 
 * However, the exact behavior of the replication process including<ul>
 * <li>to what extent the input object graph should be traversed and/or replicated; and </li>
 * <li>whether proxied instances should be eagerly fetched or not</li>
 * </ul> 
 * can be controlled and/or supplemented by the client code via various options:
 * <p>
 * <ol>
 * <li>All the configurable options of {@link BeanPopulatorBaseSpi} are available, as
 * the replication of JavaBean properties inevitably involves bean population.</li>
 * <p>
 * <li>The set of entity bean classes for matching properties that will be replicated;</li>
 * <p>
 * <li>The set of collection and map properties that will be replicated;</li>
 * <p>
 * <li>A {@link net.sf.beanlib.spi.PropertyFilter vetoer} used to veto the propagation of a property</li>
 * <p>
 * <li>For anything else that the existing implementation fails to transform, client can provide
 * one or multiple custom transformer factories via  
 * {@link #initCustomTransformerFactory(net.sf.beanlib.spi.CustomBeanTransformerSpi.Factory...)}.
 * </li>
 * </ol>
 * <p>
 * This was originally the base class for both the Hibernate 2.x and Hibernate 3.x
 * replicators, but now Hibernate 2 is no longer supported.
 * 
 * @see CustomBeanTransformerSpi
 * 
 * @see net.sf.beanlib.hibernate3.Hibernate3BeanReplicator 
 *  
 * @author Joe D. Velopar
 */
@NotThreadSafe
public abstract class HibernateBeanReplicator implements BeanPopulatorBaseSpi 
{
    /** Used to do the heavy lifting of Hibernate object transformation and replication. */
    private final BeanTransformerSpi hibernateBeanTransformer;

    /**
     * You probably want to construct a 
     * {@link net.sf.beanlib.hibernate3.Hibernate3BeanReplicator Hibernate3BeanReplicator}
     * directly instead of this ?
     */
    protected HibernateBeanReplicator(BeanTransformerSpi hibernateBeanTransformer) 
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
     * Use {@link HibernateBeanReplicator#copy(Object, Class)} instead of this method 
     * if you want to plug in a different (detailed) property filter or 
     * setter method finder.
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
     * if you want to plug in a different (detailed) property filter or 
     * setter method finder.
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
        hibernateBeanTransformer.initPropertyFilter(new HibernatePropertyFilter());
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
     * Use {@link HibernateBeanReplicator#copy(Object, Class)} instead of this method 
     * if you want to plug in a different (detailed) property filter or 
     * setter method finder.
     * </li>
     * <li>This method will cause both the public and protected setter methods 
     * to be invoked for property propagation.
     * </li>
     * </ol>
     * 
     * @see HibernatePropertyFilter
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
     * if you want to plug in a different (detailed) property filter or 
     * setter method finder.
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
        Set<? extends CollectionPropertyName> emptyCollectionPropertyNameSet = Collections.emptySet();
        Set<Class<?>> emptyEntityBeanClassSet = Collections.emptySet();
        
        hibernateBeanTransformer.initPropertyFilter(
                new HibernatePropertyFilter()
                    .initCollectionPropertyNameSet(emptyCollectionPropertyNameSet)
                    .initEntityBeanClassSet(emptyEntityBeanClassSet));
        this.setDefaultBehavior();
        return this.copy(from, toClass);
    }
    
    /** Configures the default behavior when either shallow or deep copy is invoked. */
    private void setDefaultBehavior() {
        this.hibernateBeanTransformer.initDetailedPropertyFilter(null);
        this.hibernateBeanTransformer.initSetterMethodCollector(new ProtectedSetterMethodCollector());        
    }

    /** 
     * Initializes with one or more custom bean transformer factories 
     * that will be chained together.
     * 
     * @see ChainedCustomBeanTransformer
     */
    public final HibernateBeanReplicator initCustomTransformerFactory(
            CustomBeanTransformerSpi.Factory ...customBeanTransformerFactories) 
    {
        if (customBeanTransformerFactories != null && customBeanTransformerFactories.length > 0) 
        {
            hibernateBeanTransformer.initCustomTransformerFactory(customBeanTransformerFactories.length == 1
                            ? customBeanTransformerFactories[0]
                            : new ChainedCustomBeanTransformer.Factory(customBeanTransformerFactories))
                            ;
        }
        else
            hibernateBeanTransformer.initCustomTransformerFactory(null);
        return this;
    }

    /**
     * Returns the set of entity bean classes for matching properties that will be replicated.
     * Only applicable if the current {@link PropertyFilter} is an instance of {@link HibernatePropertyFilter}.
     * Null means all whereas empty means none.
     * 
     * @throws UnsupportedOperationException 
     * if this method is invoked when the underlying bean transformer is not a {@link HibernatePropertyFilter}.
     */
    public final Set<Class<?>> getEntityBeanClassSet() throws UnsupportedOperationException
    {
        PropertyFilter s = this.hibernateBeanTransformer.getPropertyFilter();

        if (s instanceof HibernatePropertyFilter)
        {
            HibernatePropertyFilter hs = (HibernatePropertyFilter)s;
            return hs.getEntityBeanClassSet();
        }
        throw new UnsupportedOperationException("Method getEntityBeanClassSet is only supported if the bean transformer is " 
                + HibernatePropertyFilter.class.getSimpleName());
    }

    /**
     * Used to specify the set of entity beans to be populated.
     * 
     *  @param entityBeanClassSet the set of entity beans to be populated;
     *  or null if all entity bean are to be populated.
     *  @return the current HibernateBeanReplicator instance for command chaining.
     * 
     * @throws UnsupportedOperationException 
     * if this method is invoked when the underlying bean transformer is not a {@link HibernatePropertyFilter}.
     */
    public final HibernateBeanReplicator initEntityBeanClassSet(Set<Class<?>> entityBeanClassSet) throws UnsupportedOperationException
    {
        PropertyFilter s = this.hibernateBeanTransformer.getPropertyFilter();

        if (s instanceof HibernatePropertyFilter)
        {
            HibernatePropertyFilter hs = (HibernatePropertyFilter)s;
            hs.initEntityBeanClassSet(entityBeanClassSet);
            return this;
        }
        throw new UnsupportedOperationException("Method initEntityBeanClassSet is only supported if the bean transformer is " 
                + HibernatePropertyFilter.class.getSimpleName());
    }

    /**
     * Returns the set of collection and map properties that will be replicated.
     * Null means all whereas empty means none.
     * 
     * @throws UnsupportedOperationException 
     * if this method is invoked when the underlying bean transformer is not a {@link HibernatePropertyFilter}.
     */
    public final Set<? extends CollectionPropertyName> getCollectionPropertyNameSet() throws UnsupportedOperationException 
    {
        PropertyFilter s = this.hibernateBeanTransformer.getPropertyFilter();

        if (s instanceof HibernatePropertyFilter)
        {
            HibernatePropertyFilter hs = (HibernatePropertyFilter)s;
            return hs.getCollectionPropertyNameSet();
        }
        throw new UnsupportedOperationException("Method getCollectionPropertyNameSet is only supported if the bean transformer is " 
                + HibernatePropertyFilter.class.getSimpleName());
    }

    /**
     * Used to specify the set of Collection fields to be populated.
     * 
     * @param collectionPropertyNameSet the set of Collection fields to be populated;
     * or null if all Collection fields are to be populated.
     * @return the current HibernateBeanReplicator instance for command chaining.
     * 
     * @throws UnsupportedOperationException 
     * if this method is invoked when the underlying bean transformer is not a {@link HibernatePropertyFilter}.
     */
    public final HibernateBeanReplicator initCollectionPropertyNameSet(Set<? extends CollectionPropertyName> collectionPropertyNameSet)
        throws UnsupportedOperationException
    {
        PropertyFilter s = this.hibernateBeanTransformer.getPropertyFilter();

        if (s instanceof HibernatePropertyFilter)
        {
            HibernatePropertyFilter hs = (HibernatePropertyFilter)s;
            hs.initCollectionPropertyNameSet(collectionPropertyNameSet);
            return this;
        }
        throw new UnsupportedOperationException("Method initCollectionPropertyNameSet is only supported if the bean transformer is " 
                + HibernatePropertyFilter.class.getSimpleName());
    }
    
    /**
     * Returns the vetoer in the context of using 
     * {@link HibernatePropertyFilter}, which is the default,
     * for controlling the propagation of properties. 
     * A vetoer is used to further veto the propagation of specific properties.
     * <p>
     * Irrelevant if {@link HibernatePropertyFilter} is not used.
     * 
     * @see HibernatePropertyFilter
     * 
     * @throws UnsupportedOperationException 
     * if this method is invoked when the underlying bean transformer is not a {@link HibernatePropertyFilter}.
     */
    public final PropertyFilter getVetoer() throws UnsupportedOperationException
    {
        PropertyFilter s = this.hibernateBeanTransformer.getPropertyFilter();

        if (s instanceof HibernatePropertyFilter)
        {
            HibernatePropertyFilter hs = (HibernatePropertyFilter)s;
            return hs.getVetoer();
        }
        throw new UnsupportedOperationException("Method getVetoer is only supported if the bean transformer is " 
                + HibernatePropertyFilter.class.getSimpleName());
    }

    /**
     * This method is only relevant if {@link HibernatePropertyFilter}, which is the default,
     * is used for controlling the propagation of properties.
     * 
     * @param vetoer can be used to further veto the propagation of specific properties.
     * 
     * @see HibernatePropertyFilter
     * 
     * @throws UnsupportedOperationException 
     * if this method is invoked when the underlying bean transformer is not a {@link HibernatePropertyFilter}.
     */
    public final HibernateBeanReplicator initVetoer(PropertyFilter vetoer) throws UnsupportedOperationException
    {
        PropertyFilter s = this.hibernateBeanTransformer.getPropertyFilter();

        if (s instanceof HibernatePropertyFilter)
        {
            HibernatePropertyFilter hs = (HibernatePropertyFilter)s;
            hs.initVetoer(vetoer);
            return this;
        }
        throw new UnsupportedOperationException("Method initVetoer is only supported if the bean transformer is " 
                + HibernatePropertyFilter.class.getSimpleName());
    }

    // ========================== Bean Population configuration ========================== 

    /**
     * Returns the populator that is used to control 
     * what properties get propagated across and what get skipped.
     * Note if the returned value is null, the default behavior will make use
     * of {@link HibernatePropertyFilter}.
     */
    public final PropertyFilter getPropertyFilter() {
        return hibernateBeanTransformer.getPropertyFilter();
    }
    
    /**
     * Note this method is only applicable if either the {@link #copy(Object)} 
     * or {@link #copy(Object, Class)} is directly invoked, 
     * and is ignored otherwise (ie ignored if deep or shallow copy is invoked instead).
     * 
     * @param propertyFilter is similar to {@link DetailedPropertyFilter} but with a simpler API
     * that is used to control whether a specific JavaBean property should be propagated
     * from a source bean to a target bean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public final HibernateBeanReplicator initPropertyFilter(PropertyFilter propertyFilter) {
        this.hibernateBeanTransformer.initPropertyFilter(propertyFilter);
        return this;
    }

    /**
     * Note this method is only applicable if either the {@link #copy(Object)} 
     * or {@link #copy(Object, Class)} is directly invoked, 
     * and is ignored otherwise (ie ignored if deep or shallow copy is invoked instead).
     * 
     * @param detailedPropertyFilter is used to control whether a specific JavaBean property
     * should be propagated from the source bean to the target bean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public final HibernateBeanReplicator initDetailedPropertyFilter(DetailedPropertyFilter detailedPropertyFilter) 
    {
        this.hibernateBeanTransformer.initDetailedPropertyFilter(detailedPropertyFilter);
        return this;
    }
    
    /**
     * Used to configure a call-back 
     * (to produce whatever side-effects deemed necessary) that is invoked
     * after the property value has been retrieved from the source bean, 
     * but before being propagated across to the target bean.
     * 
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
     * Used to configure a finder to find the property getter methods of a source JavaBean.
     * 
     * @param readerMethodFinder can be used to find the property getter methods of a source JavaBean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public final HibernateBeanReplicator initReaderMethodFinder(BeanMethodFinder readerMethodFinder) {
        this.hibernateBeanTransformer.initReaderMethodFinder(readerMethodFinder);
        return this;
    }

    /**
     * Used to configure a collector to collect the property setter methods of a target JavaBean.
     * <p>
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

    public HibernateBeanReplicator initBeanPopulationExceptionHandler(
            BeanPopulationExceptionHandler beanPopulationExceptionHandler) 
    {
        this.hibernateBeanTransformer.initBeanPopulationExceptionHandler(beanPopulationExceptionHandler);
        return this;
    }

    /**
     * Used to conveniently provide the bean population related configuration options as a single 
     * configuration object.
     * <p>
     * Note the detailedPropertyFilter and setterMethodCollector in the given config are 
     * only applicable if either the {@link #copy(Object)} 
     * or {@link #copy(Object, Class)} is directly invoked, 
     * and is ignored otherwise (ie ignored if deep or shallow copy is invoked instead).
     * 
     * @param baseConfig is used to conveniently group all the other initializable options into a single unit.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public BeanPopulatorBaseSpi initBeanPopulatorBaseConfig(
            BeanPopulatorBaseConfig baseConfig) 
    {
        this.hibernateBeanTransformer.initBeanPopulatorBaseConfig(baseConfig);
        return this;
    }

    public BeanPopulationExceptionHandler getBeanPopulationExceptionHandler() {
        return hibernateBeanTransformer.getBeanPopulationExceptionHandler();
    }

    /**
     * Notes if the returned base config is modified, a subsequent 
     * {@link BeanPopulator#initBeanPopulatorBaseConfig(BeanPopulatorBaseConfig)}
     * needs to be invoked to keep the configuration in sync.
     */
    public BeanPopulatorBaseConfig getBeanPopulatorBaseConfig() {
        return hibernateBeanTransformer.getBeanPopulatorBaseConfig();
    }

    public BeanSourceHandler getBeanSourceHandler() {
        return hibernateBeanTransformer.getBeanSourceHandler();
    }

    public boolean isDebug() {
        return hibernateBeanTransformer.isDebug();
    }

    public DetailedPropertyFilter getDetailedPropertyFilter() {
        return hibernateBeanTransformer.getDetailedPropertyFilter();
    }

    public BeanMethodFinder getReaderMethodFinder() {
        return hibernateBeanTransformer.getReaderMethodFinder();
    }

    public BeanMethodCollector getSetterMethodCollector() {
        return hibernateBeanTransformer.getSetterMethodCollector();
    }
}
