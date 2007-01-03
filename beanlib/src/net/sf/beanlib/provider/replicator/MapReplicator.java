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

import static net.sf.beanlib.utils.ClassUtils.isJavaPackage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.beanlib.BeanlibException;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.replicator.MapReplicatorSpi;

/**
 * Default implementation of {@link net.sf.beanlib.spi.replicator.MapReplicatorSpi}.
 * 
 * @author Joe D. Velopar
 */
public class MapReplicator extends ReplicatorTemplate implements MapReplicatorSpi
{
    private static final Factory factory = new Factory();
    
    /**
     * Factory for {@link MapReplicator}
     * 
     * @author Joe D. Velopar
     */
    private static class Factory implements MapReplicatorSpi.Factory {
        private Factory() {}
        
        public MapReplicator newReplicatable(BeanTransformerSpi beanTransformer) {
            return new MapReplicator(beanTransformer);
        }
    }

    public static MapReplicator newReplicatable(BeanTransformerSpi beanTransformer) {
        return factory.newReplicatable(beanTransformer);
    }

    protected MapReplicator(BeanTransformerSpi beanTransformer) 
    {
        super(beanTransformer);
    }
    
    public <K,V,T> T replicateMap(Map<K,V> from, Class<T> toClass)
    {
        if (!toClass.isAssignableFrom(from.getClass()))
            return null;
        Map<Object, Object> toMap;
        try {
            toMap = createToMap(from);
        } catch (SecurityException e) {
            throw new BeanlibException(e);
        } catch (InstantiationException e) {
            throw new BeanlibException(e);
        } catch (IllegalAccessException e) {
            throw new BeanlibException(e);
        } catch (NoSuchMethodException e) {
            throw new BeanlibException(e);
        }
        putTargetCloned(from, toMap);
        Map fromMap = from;
        // recursively populate member objects.
        for (Iterator itr=fromMap.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry fromEntry = (Map.Entry)itr.next();
            Object fromKey = fromEntry.getKey();
            Object fromValue = fromEntry.getValue();
            Object toKey = replicate(fromKey);
            Object toValue = replicate(fromValue);
            toMap.put(toKey, toValue);
        }
        return toClass.cast(toMap);
    }
    
    private Map<Object,Object> createToMap(Map<?,?> from) 
        throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException 
    {
        Class fromClass = from.getClass();
        
        if (isJavaPackage(fromClass)) {
            if (from instanceof SortedMap) {
                SortedMap fromSortedMap = (SortedMap<?,?>)from;
                Comparator<Object> toComparator = createToComparator(fromSortedMap);
                
                if (toComparator != null)
                    return this.createToSortedMapWithComparator(fromSortedMap, toComparator);
            }
            return createToInstanceAsMap(from);
        }
        if (from instanceof SortedMap) {
            SortedMap fromSortedMap = (SortedMap<?,?>)from;
            Comparator<Object> toComparator = createToComparator(fromSortedMap);
            return new TreeMap<Object,Object>(toComparator);
        }
        return new HashMap<Object,Object>();
    }
    
    @SuppressWarnings("unchecked")
    private Map<Object, Object> createToInstanceAsMap(Map<?, ?> from) 
        throws InstantiationException, IllegalAccessException, NoSuchMethodException
    {
        return (Map<Object,Object>)createToInstance(from);
    }
    
    /** Returns a replicated comparator of the given sorted map, or null if there is no comparator. */
    @SuppressWarnings("unchecked")
    private Comparator<Object> createToComparator(SortedMap fromSortedMap)
    {
        Comparator fromComparator = fromSortedMap.comparator();
        Comparator toComparator = fromComparator == null 
                                ? null 
                                : replicateByBeanReplicatable(fromComparator, Comparator.class)
                                ;
        return toComparator;
    }

    @SuppressWarnings("unchecked")
    private SortedMap<Object,Object> createToSortedMapWithComparator(SortedMap from, Comparator comparator) 
        throws NoSuchMethodException, SecurityException
    {
        return (SortedMap<Object,Object>)createToInstanceWithComparator(from, comparator);
    }
}
