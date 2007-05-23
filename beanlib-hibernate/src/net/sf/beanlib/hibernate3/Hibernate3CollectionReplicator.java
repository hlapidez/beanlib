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
package net.sf.beanlib.hibernate3;

import static net.sf.beanlib.utils.ClassUtils.isJavaPackage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.jcip.annotations.ThreadSafe;
import net.sf.beanlib.provider.replicator.CollectionReplicator;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.replicator.CollectionReplicatorSpi;
import net.sf.cglib.proxy.Enhancer;

import org.hibernate.Hibernate;

/**
 * Hibernate 3 specific Collection Replicator.
 * 
 * @author Joe D. Velopar
 */
public class Hibernate3CollectionReplicator extends CollectionReplicator {
    private static final Factory factory = new Factory();
    
    public static final Factory getFactory() {
        return factory;
    }
    
    /**
     * Factory for {@link Hibernate3CollectionReplicator}
     * 
     * @author Joe D. Velopar
     */
    @ThreadSafe
    public static class Factory implements CollectionReplicatorSpi.Factory {
        private Factory() {}
        
        public Hibernate3CollectionReplicator newCollectionReplicatable(BeanTransformerSpi beanTransformer) {
            return new Hibernate3CollectionReplicator(beanTransformer);
        }
    }
    
    public static Hibernate3CollectionReplicator newCollectionReplicatable(BeanTransformerSpi beanTransformer) {
        return factory.newCollectionReplicatable(beanTransformer);
    }
    
    protected Hibernate3CollectionReplicator(BeanTransformerSpi beanTransformer) 
    {
        super(beanTransformer);
    }
    
    @Override
    public <V,T> T replicateCollection(Collection<V> from, Class<T> toClass)
    {
        if (!Hibernate.isInitialized(from))
            Hibernate.initialize(from);
        return super.replicateCollection(from, toClass);
    }
    
    @Override
    protected Collection<Object> createToCollection(Collection<?> from) 
    throws InstantiationException, IllegalAccessException, SecurityException, 
            NoSuchMethodException, InvocationTargetException
    {
        Class fromClass = from.getClass();
        
        if (isJavaPackage(fromClass)) {
            if (from instanceof SortedSet) {
                SortedSet fromSortedSet = (SortedSet)from;
                Comparator toComparator = createToComparator(fromSortedSet);
                
                if (toComparator != null)
                    return this.createToSortedSetWithComparator(fromSortedSet, toComparator);
            }
            return createToInstanceAsCollection(from);
        }
        if (from instanceof SortedSet) {
            SortedSet fromSortedSet = (SortedSet)from;
            Comparator toComparator = createToComparator(fromSortedSet);
            
            if (isHibernatePackage(fromClass))
                return new TreeSet<Object>(toComparator);
            Constructor constructor = fromClass.getConstructor(Comparator.class);
            Object[] initargs = {toComparator};
            return (Collection<Object>) constructor.newInstance(initargs);
        }
        if (from instanceof Set) {
            if (isHibernatePackage(fromClass))
                return new HashSet<Object>();
            return (Collection<Object>)fromClass.newInstance();
        }
        if (from instanceof List) {
            if (isHibernatePackage(fromClass))
                return new ArrayList<Object>(from.size());
            return (Collection<Object>)fromClass.newInstance();
        }
        // don't know what collection, so use list
        log.warn("Don't know what collection object:" + fromClass + ", so assume List.");
        return new ArrayList<Object>(from.size());
    }
    
    /** Returns true if the given class has a package name that starts with "org.hibernate."; false otherwise. */
    private boolean isHibernatePackage(Class c) {
        Package p = c.getPackage();
        return p != null && p.getName().startsWith("org.hibernate.");
    }
    
    @Override
    protected <T> T createToInstance(Class<T> toClass) 
        throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException 
    {
        if (Enhancer.isEnhanced(toClass)) {
            // figure out the pre-enhanced class
            toClass = (Class<T>)toClass.getSuperclass();
        }
        return super.createToInstance(toClass);
    }
}
