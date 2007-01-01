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
package net.sf.beanlib.support.replicator;

import static net.sf.beanlib.utils.ClassUtils.isJavaPackage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import net.sf.beanlib.BeanlibException;
import net.sf.beanlib.spi.BeanTransformableSpi;
import net.sf.beanlib.spi.replicator.CollectionReplicatable;

/**
 * @author Joe D. Velopar
 */
public class CollectionReplicator extends ReplicatorTemplate implements CollectionReplicatable 
{
    public static final Factory factory = new Factory();
    
    public static class Factory implements CollectionReplicatable.Factory {
        private Factory() {}
        
        public CollectionReplicatable newReplicatable(BeanTransformableSpi beanTransformer) {
            return new CollectionReplicator(beanTransformer);
        }
    }
    
    // must be invoked as the first method on this object
    private CollectionReplicator(BeanTransformableSpi beanTransformer) 
    {
        super(beanTransformer);
    }
    
    public <V,T> T replicateCollection(Collection<V> from, Class<T> toClass)
    {
        Collection<Object> toCollection;
        try {
            toCollection = this.createToCollection(from);
            putTargetCloned(from, toCollection);
            Collection fromCollection = from;
    //        hibernateInitialize(fromCollection);
            // recursively populate member objects.
            for (Object fromMember : fromCollection) {
                Object toMember = replicate(fromMember);
                toCollection.add(toMember);
            }
        } catch (SecurityException e) {
            throw new BeanlibException(e);
        } catch (InstantiationException e) {
            throw new BeanlibException(e);
        } catch (IllegalAccessException e) {
            throw new BeanlibException(e);
        } catch (NoSuchMethodException e) {
            throw new BeanlibException(e);
        } catch (InvocationTargetException e) {
            throw new BeanlibException(e.getTargetException());
        }
        return toClass.cast(toCollection);
    }

    // Use the same comparator or otherwise ClassCastException.
    // http://sourceforge.net/forum/forum.php?thread_id=1462253&forum_id=470286
    // Thanks to Jam Flava for finding this bug.
    @SuppressWarnings("unchecked")
    private Collection<Object> createToCollection(Collection<?> from) 
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
            
//            if (isHibernatePackage(fromClass))
//                return new TreeSet<Object>(toComparator);
            Constructor constructor = fromClass.getConstructor(Comparator.class);
            Object[] initargs = {toComparator};
            return (Collection<Object>) constructor.newInstance(initargs);
        }
        if (from instanceof Set) {
//            if (isHibernatePackage(fromClass))
//                return new HashSet<Object>();
            return (Collection<Object>)fromClass.newInstance();
        }
        if (from instanceof List) {
//            if (isHibernatePackage(fromClass))
//                return new ArrayList<Object>(from.size());
            return (Collection<Object>)fromClass.newInstance();
        }
        // don't know what collection, so use list
        log.warn("Don't know what collection object:" + fromClass + ", so assume List.");
        return new ArrayList<Object>(from.size());
    }
    
    @SuppressWarnings("unchecked")
    private Collection<Object> createToInstanceAsCollection(Collection<?> from) 
        throws InstantiationException, IllegalAccessException, NoSuchMethodException
    {
        return (Collection<Object>)createToInstance(from);
    }
    @SuppressWarnings("unchecked")
    private SortedSet<Object> createToSortedSetWithComparator(SortedSet from, Comparator comparator) 
        throws NoSuchMethodException, SecurityException
    {
        return (SortedSet<Object>)createToInstanceWithComparator(from, comparator);
    }
    
    /** Returns a replicated comparator of the given sorted set, or null if there is no comparator. */
    protected Comparator createToComparator(SortedSet fromSortedSet)
    {
        Comparator fromComparator = fromSortedSet.comparator();
        Comparator toComparator = fromComparator == null 
                                ? null 
                                : replicateByBeanReplicatable(fromComparator, Comparator.class)
                                ;
        return toComparator;
    }
}
