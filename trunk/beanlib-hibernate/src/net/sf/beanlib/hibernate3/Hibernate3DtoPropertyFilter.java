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

import static net.sf.beanlib.utils.ClassUtils.immutable;
import static net.sf.beanlib.utils.ClassUtils.isJavaPackage;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.sf.beanlib.CollectionPropertyName;
import net.sf.beanlib.hibernate.UnEnhancer;
import net.sf.beanlib.spi.PropertyFilter;

/**
 * Used by {@link Hibernate3DtoCopier} to dynamically expand the set of 
 * application entity classes as necessary,
 * including those classes discovered from the element type of collection properties.
 * 
 * @author Joe D. Velopar
 */
class Hibernate3DtoPropertyFilter implements PropertyFilter {
    // the minimal set of entity bean to be populated; 
    // or null if all entity bean are to be populated.
    private final Set<Class<?>> entityBeanClassSet;
    // the minimal set of Set fields to be populated; 
    // or null if all set fields are to be populated.
    private final Set<CollectionPropertyName> collectionPropertyNameSet;
    
    private Hibernate3DtoCopier applicationBeanCopier;
    
    Hibernate3DtoPropertyFilter(
            Set<Class<?>> entityBeanClassSet, 
            Set<CollectionPropertyName> collectionPropertyNameSet)
    {
        this.entityBeanClassSet = entityBeanClassSet;
        this.collectionPropertyNameSet = collectionPropertyNameSet;
    }

    Hibernate3DtoPropertyFilter(Set<CollectionPropertyName> collectionPropertyNameSet)
    {
        this.entityBeanClassSet = Collections.emptySet();
        this.collectionPropertyNameSet = collectionPropertyNameSet;
    }
    
    Hibernate3DtoPropertyFilter() {
        this(null, null);
    }

    // TODO: assert this must be invoked after construction
    Hibernate3DtoPropertyFilter init(Hibernate3DtoCopier applicationBeanCopier) 
    {
        this.applicationBeanCopier = applicationBeanCopier;
        return this;
    }
    
    /**
     * @see net.sf.beanlib.spi.PropertyFilter#propagate(java.lang.String, java.lang.reflect.Method)
     * 
     * @param propertyName property name.
     * @param readerMethod reader method of the property.
     * @return true if the property population should take place; false otherwise. 
     */
    public boolean propagate(String propertyName, Method readerMethod) 
    {
        Class<?> returnType = UnEnhancer.unenhanceClass(readerMethod.getReturnType());
        
        if (immutable(returnType))
            return true;
        
        if (returnType.isArray()) {
            if (immutable(returnType.getComponentType()))
                return true;
        }
        
        if (entityBeanClassSet == null) {
            // All entity bean to be populated.
            if (collectionPropertyNameSet == null) {
                // all fields to be populated
                return true;
            }
            return checkCollectionProperty(propertyName, readerMethod);
        }
        // Only a selected set of entity bean to be populated.
        if (isJavaPackage(returnType)) {
            // Not an entity bean.
            if (collectionPropertyNameSet == null) {
                // All Collection/Map properties to be populated.
                return true;
            }
            return checkCollectionProperty(propertyName, readerMethod);
        }
        // An entity bean.
        boolean goAhead = entityBeanClassSet.contains(returnType) 
                       || applicationBeanCopier.isApplicationClass(returnType);
        Class<?> superClass = returnType;
        
        for (;;) {
            if (goAhead)
                return true;
            // check if it's ancestor is specified in entityBeanClassSet
            superClass = superClass.getSuperclass();
            
            if (superClass == null)
                break;        // not specified in entityBeanClassSet
            goAhead = entityBeanClassSet.contains(superClass) 
                   || applicationBeanCopier.isApplicationClass(superClass);
        }
        return goAhead;
    }
    
    private boolean checkCollectionProperty(String propertyName, Method readerMethod) 
    {
        // Only a specified set of Collection/Map properties needs to be populated
        Class<?> returnType = UnEnhancer.unenhanceClass(readerMethod.getReturnType());
        
        if (Collection.class.isAssignableFrom(returnType) 
        ||    Map.class.isAssignableFrom(returnType)) 
        {
            // A Collection/Map property
            if (collectionPropertyNameSet.contains(
                    new CollectionPropertyName(
                            UnEnhancer.unenhanceClass(
                                    readerMethod.getDeclaringClass()), propertyName))) 
            {
                return true; 
            }
            // Collection/Map property not included.
            return false;
        }
        // Not a Collection/Map property.
        return true;
    }
}
