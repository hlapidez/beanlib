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
import net.sf.beanlib.spi.BeanPopulatable;

/**
 * The default implemenation to determine if a Hibernate JavaBean property should be populated.
 * Each population decision can be vetoed by plugging in a custom {@link net.sf.beanlib.spi.BeanPopulatable vetoer}.
 *
 * @author Joe D. Velopar
 */
public class HibernateBeanPopulatableSupport implements BeanPopulatable 
{
    // the set of entity bean to be populated; 
    // or null if all entity bean are to be populated.
    private Set<Class<?>> entityBeanClassSet;
    // the set of Collection fields to be populated; 
    // or null if all Collection fields are to be populated.
    private Set<? extends CollectionPropertyName> collectionPropertyNameSet;
    
    /** Used to veto the propagation of a JavaBean property. */
    private BeanPopulatable vetoer;
    
    public HibernateBeanPopulatableSupport(Set<Class<?>> entityBeanClassSet, 
        Set<? extends CollectionPropertyName> collectionPropertyNameSet, BeanPopulatable vetoer)
    {
        this.entityBeanClassSet = entityBeanClassSet;
        this.collectionPropertyNameSet = collectionPropertyNameSet;
        this.vetoer = vetoer;
    }
    /**
     * @see net.sf.beanlib.spi.BeanPopulatable#shouldPopulate(String, Method)
     * 
     * @param propertyName property name.
     * @param readerMethod reader method of the property.
     * @return true if the property population should take place; false otherwise. 
     */
    public boolean shouldPopulate(String propertyName, Method readerMethod) 
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
            Class returnType = UnEnhancer.unenhanceClass(readerMethod.getReturnType());
            
            if (immutable(returnType))
            {
                return vetoer == null ? true : vetoer.shouldPopulate(propertyName, readerMethod);
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
                Class superClass = returnType;
                
                for (;;) {
                    if (goAhead)
                        return vetoer == null ? true : vetoer.shouldPopulate(propertyName, readerMethod);
                    // check if it's ancestor is specified in entityBeanClassSet
                    superClass = superClass.getSuperclass();
                    
                    if (superClass == null || superClass == Object.class)
                        break;        // not specified in entityBeanClassSet
                    goAhead = entityBeanClassSet.contains(superClass);
                }
            }
        }
        if (goAhead)
            return vetoer == null ? true : vetoer.shouldPopulate(propertyName, readerMethod);
        return false;
    }
//    /** Returns the given array as a set. */
//    private Set toSet(Object[] array) {
//        return array == null ? null : new HashSet(Arrays.asList(array));
//    }
}
