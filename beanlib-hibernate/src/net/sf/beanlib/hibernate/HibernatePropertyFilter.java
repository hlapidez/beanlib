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

import static net.sf.beanlib.utils.ClassUtils.immutable;
import static net.sf.beanlib.utils.ClassUtils.isJavaPackage;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.sf.beanlib.CollectionPropertyName;
import net.sf.beanlib.spi.PropertyFilter;

/**
 * A default implementation used to determine if a Hibernate property 
 * that follows the JavaBean getter/setter convention should be propagated.
 * Each propagation decision can be controlled by specifying
 * <ul>
 * <li>The set of entity bean classes for matching properties that will be replicated;</li>
 * <li>The set of collection and map properties that will be replicated;</li>
 * <li>A {@link net.sf.beanlib.spi.PropertyFilter vetoer} used to veto the propagation of a property</li>
 * </ul>
 *
 * @author Joe D. Velopar
 */
public class HibernatePropertyFilter implements PropertyFilter 
{
    /**
     * The set of entity bean classes for matching properties that will be replicated, 
     * eagerly fetching if necessary.
     * Null means all whereas empty means none.
     */
    private Set<Class<?>> entityBeanClassSet;

    /**
     * The set of collection and map properties that will be replicated, 
     * eagerly fetching if necessary.
     * Null means all whereas empty means none.
     */
    private Set<? extends CollectionPropertyName> collectionPropertyNameSet;
    
    /** Used to veto the propagation of a property. */
    private PropertyFilter vetoer;
    
    /**
     * Constructs with the specified options of controlling what to be replicated and what not.
     * 
     * @param entityBeanClassSet
     * The set of entity bean classes for matching properties that will be replicated, 
     * eagerly fetching if necessary.
     * Null means all whereas empty means none.

     * @param collectionPropertyNameSet
     * The set of collection and map properties that will be replicated, 
     * eagerly fetching if necessary.
     * Null means all whereas empty means none.
     * 
     * @param vetoer used to veto the propagation of a JavaBean property.
     */
    public HibernatePropertyFilter(Set<Class<?>> entityBeanClassSet, 
        Set<? extends CollectionPropertyName> collectionPropertyNameSet, PropertyFilter vetoer)
    {
        this.entityBeanClassSet = entityBeanClassSet;
        this.collectionPropertyNameSet = collectionPropertyNameSet;
        this.vetoer = vetoer;
    }
    
    /**
     * Constructs with the default behavior of replicating all properties recursively.
     */
    public HibernatePropertyFilter() {}
    
    /**
     * Returns the configured set of entity bean classes for matching properties that will be replicated, 
     * eagerly fetching if necessary;
     * null if all entity bean classes are to be replicated; 
     * or empty if no entity bean class is to be replicated.
     */
    public Set<Class<?>> getEntityBeanClassSet() {
        return entityBeanClassSet;
    }

    /**
     * Used to configure the set of entity bean classes for matching properties that will be replicated, 
     * eagerly fetching if necessary.
     * 
     * @param entityBeanClassSet the set of entity bean classes for matching properties that will be replicated, 
     * eagerly fetching if necessary.
     * null if all entity bean classes are to be replicated; 
     * or empty if no entity bean class is to be replicated.
     * 
     * @return the current instance for method chaining purposes.
     */
    public HibernatePropertyFilter initEntityBeanClassSet(Set<Class<?>> entityBeanClassSet) {
        this.entityBeanClassSet = entityBeanClassSet;
        return this;
    }

    /**
     * Returns the configured set of collection and map properties that are to be replicated, 
     * eagerly fetching if necessary;
     * null if all collection and map properties are to be replicated; 
     * or empty if no collection nor map properties are to be replicated.
     */
    public Set<? extends CollectionPropertyName> getCollectionPropertyNameSet() {
        return collectionPropertyNameSet;
    }

    /**
     * Used to configure the set of collection and map properties that will be replicated, eagerly fetching if necessary.
     * 
     * @param collectionPropertyNameSet set of collection and map properties that will be replicated, 
     * eagerly fetching if necessary;
     * null if all collection and map properties are to be replicated;
     * or empty if no collection nor map properties are to be replicated.
     * 
     * @return the current instance for method chaining purposes.
     */
    public HibernatePropertyFilter initCollectionPropertyNameSet(
            Set<? extends CollectionPropertyName> collectionPropertyNameSet) 
    {
        this.collectionPropertyNameSet = collectionPropertyNameSet;
        return this;
    }

    /**
     * Returns the vetoer configured for vetoing the propagation of a property.
     */
    public PropertyFilter getVetoer() {
        return vetoer;
    }

    /**
     * Used to configure a vetoer for vetoing the propagation of a property.
     * 
     * @return the current instance for method chaining purposes.
     */
    public HibernatePropertyFilter initVetoer(PropertyFilter vetoer) {
        this.vetoer = vetoer;
        return this;
    }
    
    public boolean propagate(String propertyName, Method readerMethod) 
    {
        boolean goAhead = false;
                    
        if (entityBeanClassSet == null) {
            // All entity bean to be populated.
            if (collectionPropertyNameSet == null) {
                // all fields to be populated
                goAhead = true;
            }
            else {
                Class<?> unenhancedReturnType = UnEnhancer.unenhanceClass(readerMethod.getReturnType());
                // Only a subset of collection properties needs to be populated
                goAhead =  Collection.class.isAssignableFrom(unenhancedReturnType) 
                        || Map.class.isAssignableFrom(unenhancedReturnType)
                        ? collectionPropertyNameSet.contains(
                                new CollectionPropertyName(UnEnhancer.unenhanceClass(readerMethod.getDeclaringClass()), propertyName))
                        : true    // not a Collection property, so go ahead
                        ;
            }
        }
        else {
            // Only a subset of entity bean to be populated.
            Class<?> returnType = UnEnhancer.unenhanceClass(readerMethod.getReturnType());
            
            if (immutable(returnType))
            {
                return vetoer == null ? true : vetoer.propagate(propertyName, readerMethod);
            }
            
            if (isJavaPackage(returnType)) {
                // Not an entity bean.
                if (collectionPropertyNameSet == null) {
                    // All Collection properties to be populated.
                    goAhead = true;
                }
                else {
                    // Only a subset of collection properties to be populated.
                    goAhead =  Collection.class.isAssignableFrom(returnType)
                            || Map.class.isAssignableFrom(returnType)
                            ? collectionPropertyNameSet.contains(
                                    new CollectionPropertyName(
                                            UnEnhancer.unenhanceClass(
                                                    readerMethod.getDeclaringClass()), propertyName))
                            : true
                            ;
                }
            }
            else {
                // An entity bean.
                goAhead = entityBeanClassSet.contains(returnType);
                Class<?> superClass = returnType;
                
                for (;;) {
                    if (goAhead)
                        return vetoer == null ? true : vetoer.propagate(propertyName, readerMethod);
                    // check if it's ancestor is specified in entityBeanClassSet
                    superClass = superClass.getSuperclass();
                    
                    if (superClass == null || superClass == Object.class)
                        break;        // not specified in entityBeanClassSet
                    goAhead = entityBeanClassSet.contains(superClass);
                }
            }
        }
        if (goAhead)
            return vetoer == null ? true : vetoer.propagate(propertyName, readerMethod);
        return false;
    }
}
