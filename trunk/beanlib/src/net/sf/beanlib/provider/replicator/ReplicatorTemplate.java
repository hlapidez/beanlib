/*
 * Copyright 2007 The Apache Software Foundation.
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
package net.sf.beanlib.provider.replicator;

import static net.sf.beanlib.utils.ClassUtils.immutable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Blob;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import net.sf.beanlib.BeanlibException;
import net.sf.beanlib.PropertyInfo;
import net.sf.beanlib.spi.BeanTransformerSpi;

import org.apache.log4j.Logger;

/**
 * A useful base class for the replicator implementations.
 * 
 * @author Joe D. Velopar
 */
public abstract class ReplicatorTemplate
{
    protected final Logger log = Logger.getLogger(getClass());
    private final BeanTransformerSpi beanTransformer;
    
    protected ReplicatorTemplate(BeanTransformerSpi beanTransformer) {
        this.beanTransformer = beanTransformer;
    }

    protected ReplicatorTemplate() {
        this.beanTransformer = (BeanTransformerSpi)this;
    }
    
    protected Object replicate(Object from)
    {
        if (from == null)
            return null;
        try {
            return replicate(from, from.getClass());
        } catch (SecurityException e) {
            throw new BeanlibException(e);
        }
    }
    /**
     * Recursively replicate, if necessary, the given objects.
     * Currently an object is replicated if it is an instance
     * of Collection, Map, Timestamp, Date, Blob, Hibernate entity, 
     * JavaBean, or an array.
     */
    @SuppressWarnings("unchecked")
    protected <T> T replicate(Object from, Class<T> toClass) 
        throws SecurityException 
    {
        if (from == null) 
        {
            return toClass.isPrimitive()
                 ? ImmutableReplicator.getDefaultPrimitiveValue(toClass)
                 : null;
        }
        T to = (T)beanTransformer.getClonedMap().get(from);
        
        if (to != null)
            return to;    // already transformed.
        // Immutable e.g. String, Enum, primitvies, BigDecimal, etc.
        if (immutable(from.getClass()))
            return beanTransformer.getImmutableReplicatable()
                                  .replicateImmutable(from, toClass);
        // Collection
        if (from instanceof Collection)
            return beanTransformer.getCollectionReplicatable()
                                  .replicateCollection((Collection<?>)from, toClass);
        // Array
        if (from.getClass().isArray())
            return beanTransformer.getArrayReplicatable()
                                  .replicateArray(from, toClass);
        // Map
        if (from instanceof Map)
            return (T)beanTransformer.getMapReplicatable()
                                     .replicateMap((Map)from, toClass);
        // Date or Timestamp
        if (from instanceof Date)
            return beanTransformer.getDateReplicatable()
                                  .replicateDate((Date)from, toClass);
        // Blob
        if (from instanceof Blob)
            return beanTransformer.getBlobReplicatable()
                                  .replicateBlob((Blob)from, toClass);
        // Other objects
        return replicateByBeanReplicatable(from, toClass);
    }
    
    protected <T> T replicateByBeanReplicatable(Object from, Class<T> toClass)
    {
        return beanTransformer.getBeanReplicatable()
                              .replicateBean(from, toClass);
    }
    
//    /** 
//     * Creates a non cglib enhanced instance of the given object.
//     */
    protected final Object createToInstance(Object from) 
        throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException 
    {
        return createToInstance(from, from.getClass());
    }
    
//    /** 
//     * Creates a non cglib enhanced instance of the given class, which could itself be the class of a cglib enhanced object.
//     */
//    @SuppressWarnings("unchecked")
//    protected <T> T createToInstance(Class<T> toClass) 
//        throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException 
//    {
//        //      if (Enhancer.isEnhanced(toClass)) {
//        //      // figure out the pre-enhanced class
//        //      toClass = (Class<T>)toClass.getSuperclass();
//        //  }
//        return newInstanceAsPrivileged(toClass);
//    }
    
    @SuppressWarnings("unchecked")
    protected <T> T createToInstance(Object from, Class<T> toClass)
        throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException 
    {
        Class<T> targetClass = chooseClass(from.getClass(), toClass);
        return newInstanceAsPrivileged(targetClass);
    }
    
    @SuppressWarnings("unchecked")
    protected final <T> Class<T> chooseClass(Class<?> fromClass, Class<T> toClass) 
    {
        if (!toClass.isAssignableFrom(fromClass) 
        ||  Modifier.isAbstract(fromClass.getModifiers()))
            return toClass;
        return (Class<T>)fromClass;
    }
    
    protected <T> T transform(Object in, Class<T> toClass, PropertyInfo propertyInfo) {
        return beanTransformer.transform(in, toClass, propertyInfo);
    }
    
    protected void populateBean(Object fromMember, Object toMember) {
        beanTransformer.getBeanPopulatorSpiFactory()
                       .newBeanPopulator(fromMember, toMember)
                       .initBeanPopulatorBaseConfig(
                                    beanTransformer.getBeanPopulatorBaseConfig())
                       .initTransformer(beanTransformer)
                       .populate();
    }
    
    
    protected Object createToInstanceWithComparator(Object from, Comparator comparator)
        throws SecurityException, NoSuchMethodException
    {
        return createToInstanceWithComparator(from.getClass(), comparator);
    }

    private <T> T createToInstanceWithComparator(Class<T> toClass, Comparator comparator) 
        throws SecurityException, NoSuchMethodException
    {
//        Class toClass = fromClass;
        
//        if (Enhancer.isEnhanced(toClass)) {
//            // figure out the pre-enhanced class
//            toClass = toClass.getSuperclass();
//        }
        return newInstanceWithComparatorAsPrivileged(toClass, comparator);
    }


    /** 
     * Creates a new instance of the given class via the no-arg constructor,
     * invoking the constructor as a privileged action if it is protected or private.
     * 
     * @param c given class
     * @return a new instance of the given class via the no-arg constructor
     */ 
    protected final <T> T newInstanceAsPrivileged(Class<T> c) 
        throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        final Constructor constructor = c.getDeclaredConstructor();
        
        if (Modifier.isPublic(constructor.getModifiers()))
            return c.newInstance();
        return c.cast(AccessController.doPrivileged(
            new PrivilegedAction<Object>() {
                public Object run() {
                    constructor.setAccessible(true);
                    try {
                        return constructor.newInstance();
                    } catch (IllegalAccessException e) {
                        throw new BeanlibException(e);
                    } catch (InvocationTargetException e) {
                        throw new BeanlibException(e.getTargetException());
                    } catch (InstantiationException e) {
                        throw new BeanlibException(e);
                    }
                }
        }));
    }

    private <T> T newInstanceWithComparatorAsPrivileged(Class<T> c, final Comparator comparator) 
        throws SecurityException, NoSuchMethodException
    {
        final Constructor constructor = c.getDeclaredConstructor(Comparator.class);
        
        if (Modifier.isPublic(constructor.getModifiers())) {
            try {
                return c.cast(constructor.newInstance(comparator));
            } catch (InstantiationException e) {
                throw new BeanlibException(e);
            } catch (IllegalAccessException e) {
                throw new BeanlibException(e);
            } catch (InvocationTargetException e) {
                throw new BeanlibException(e.getTargetException());
            }
        }
        return c.cast(AccessController.doPrivileged(
            new PrivilegedAction<Object>() {
                public Object run() {
                    constructor.setAccessible(true);
                    try {
                        return  constructor.newInstance(comparator);
                    } catch (IllegalAccessException e) {
                        throw new BeanlibException(e);
                    } catch (InvocationTargetException e) {
                        throw new BeanlibException(e.getTargetException());
                    } catch (InstantiationException e) {
                        throw new BeanlibException(e);
                    }
                }
        }));
    }

    protected Object getTargetCloned(Object from)
    {
        return beanTransformer.getClonedMap().get(from);
    }

    protected Object putTargetCloned(Object from, Object to)
    {
        return beanTransformer.getClonedMap().put(from, to);
    }
}
