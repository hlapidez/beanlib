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
package net.sf.beanlib.utils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import net.sf.beanlib.utils.range.SortedRangeMap;

/**
 * Collection related utility methods.
 * 
 * @author Joe D. Velopar
 */
public class CollectionUtils 
{
    private CollectionUtils() {
    }
    /**
     * Returns a synchronized (thread-safe) sorted range map backed by the specified
     * sorted range map.  In order to guarantee serial access, it is critical that
     * <strong>all</strong> access to the backing sorted range map is accomplished
     * through the returned sorted range map (or its views).<p>
     *
     * It is imperative that the user manually synchronize on the returned
     * sorted range map when iterating over any of its collection views, or the
     * collections views of any of its <tt>subMap</tt>, <tt>headMap</tt> or
     * <tt>tailMap</tt> views.
     * <pre>
     *  SortedRangeMap m = CollectionUtils.synchronizedSortedRangeMap(new RangeTreeMap());
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized(m) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * or:
     * <pre>
     *  SortedRangeMap m = CollectionUtils.synchronizedSortedRangeMap(new RangeTreeMap());
     *  SortedMap m2 = m.subMap(foo, bar);
     *      ...
     *  Set s2 = m2.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized(m) {  // Synchronizing on m, not m2 or s2!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p>The returned sorted range map will be serializable if the specified
     * sorted range map is serializable.
     *
     * @param  m the sorted range map to be "wrapped" in a synchronized sorted range map.
     * @return a synchronized view of the specified sorted range map.
     */
    public static <T, K, V> SortedRangeMap<T, K, V> synchronizedSortedRangeMap(
            SortedRangeMap<T, K, V> m) 
    {
        return new SynchronizedSortedRangeMap<T, K, V>(m);
    }

    /** A thread-safe SortedRangeMap. */
    static class SynchronizedSortedRangeMap<T, K, V> 
            extends SynchronizedSortedMap<K, V> 
            implements SortedRangeMap<T, K, V> 
    {
        private static final long serialVersionUID = 1L;
        
        private SortedRangeMap<T, K, V> srm;

        SynchronizedSortedRangeMap(SortedRangeMap<T, K, V> srm) {
            super(srm);
            this.srm = srm;
        }

        SynchronizedSortedRangeMap(SortedRangeMap<T, K, V>  srm, Object mutex) {
            super(srm, mutex);
            this.srm = srm;
        }

        public V getByPoint(Comparable<T> point) {
            synchronized(mutex) {
                return srm.getByPoint(point);
            }
        }
        
        public boolean containsKeyPoint(Comparable<T> point) {
            synchronized(mutex) {
                return srm.containsKeyPoint(point);
            }
        }
    }

    /**
     * Basically cloned from and identification to java.util.Collections.SynchronizedMap.
     * 
     * @author Joe D. Velopar
     */
    private static class SynchronizedMap<K, V> implements Map<K, V>,
            Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability
        private static final long serialVersionUID = 1978198479659022715L;

        private Map<K, V> m; // Backing Map

        Object mutex; // Object on which to synchronize

        SynchronizedMap(Map<K, V> m) {
            if (m == null)
                throw new NullPointerException();
            this.m = m;
            mutex = this;
        }

        SynchronizedMap(Map<K, V> m, Object mutex) {
            this.m = m;
            this.mutex = mutex;
        }

        public int size() {
            synchronized (mutex) {
                return m.size();
            }
        }

        public boolean isEmpty() {
            synchronized (mutex) {
                return m.isEmpty();
            }
        }

        public boolean containsKey(Object key) {
            synchronized (mutex) {
                return m.containsKey(key);
            }
        }

        public boolean containsValue(Object value) {
            synchronized (mutex) {
                return m.containsValue(value);
            }
        }

        public V get(Object key) {
            synchronized (mutex) {
                return m.get(key);
            }
        }

        public V put(K key, V value) {
            synchronized (mutex) {
                return m.put(key, value);
            }
        }

        public V remove(Object key) {
            synchronized (mutex) {
                return m.remove(key);
            }
        }

        public void putAll(Map<? extends K, ? extends V> map) {
            synchronized (mutex) {
                m.putAll(map);
            }
        }

        public void clear() {
            synchronized (mutex) {
                m.clear();
            }
        }

        private transient Set<K> keySet = null;

        private transient Set<Map.Entry<K, V>> entrySet = null;

        private transient Collection<V> values = null;

        public Set<K> keySet() {
            synchronized (mutex) {
                if (keySet == null)
                    keySet = new SynchronizedSet<K>(m.keySet(), mutex);
                return keySet;
            }
        }

        public Set<Map.Entry<K, V>> entrySet() {
            synchronized (mutex) {
                if (entrySet == null)
                    entrySet = new SynchronizedSet<Map.Entry<K, V>>(
                            (Set<Map.Entry<K, V>>) m.entrySet(), mutex);
                return entrySet;
            }
        }

        public Collection<V> values() {
            synchronized (mutex) {
                if (values == null)
                    values = new SynchronizedCollection<V>(m.values(), mutex);
                return values;
            }
        }

        @Override
        public boolean equals(Object o) {
            synchronized (mutex) {
                return m.equals(o);
            }
        }

        @Override
        public int hashCode() {
            synchronized (mutex) {
                return m.hashCode();
            }
        }

        @Override
        public String toString() {
            synchronized (mutex) {
                return m.toString();
            }
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            synchronized (mutex) {
                s.defaultWriteObject();
            }
        }
    }

    /**
     * Basically cloned from and identification to java.util.Collections.SynchronizedSortedMap.
     * 
     * @author Joe D. Velopar
     */
    static class SynchronizedSortedMap<K, V> extends SynchronizedMap<K, V>
            implements SortedMap<K, V> 
    {
        private static final long serialVersionUID = -8798146769416483793L;

        private SortedMap<K, V> sm;

        SynchronizedSortedMap(SortedMap<K, V> m) {
            super(m);
            sm = m;
        }

        SynchronizedSortedMap(SortedMap<K, V> m, Object mutex) {
            super(m, mutex);
            sm = m;
        }

        public Comparator<? super K> comparator() {
            synchronized (mutex) {
                return sm.comparator();
            }
        }

        public SortedMap<K, V> subMap(K fromKey, K toKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<K, V>(sm
                        .subMap(fromKey, toKey), mutex);
            }
        }

        public SortedMap<K, V> headMap(K toKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<K, V>(sm.headMap(toKey), mutex);
            }
        }

        public SortedMap<K, V> tailMap(K fromKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<K, V>(sm.tailMap(fromKey),
                        mutex);
            }
        }

        public K firstKey() {
            synchronized (mutex) {
                return sm.firstKey();
            }
        }

        public K lastKey() {
            synchronized (mutex) {
                return sm.lastKey();
            }
        }
    }

    /**
     * Basically cloned from and identification to java.util.Collections.SynchronizedCollection.
     * 
     * @author Joe D. Velopar
     */
    static class SynchronizedCollection<E> implements Collection<E>,
            Serializable {
        // use serialVersionUID from JDK 1.2.2 for interoperability
        private static final long serialVersionUID = 3053995032091335093L;

        Collection<E> c; // Backing Collection

        Object mutex; // Object on which to synchronize

        SynchronizedCollection(Collection<E> c) {
            if (c == null)
                throw new NullPointerException();
            this.c = c;
            mutex = this;
        }

        SynchronizedCollection(Collection<E> c, Object mutex) {
            this.c = c;
            this.mutex = mutex;
        }

        public int size() {
            synchronized (mutex) {
                return c.size();
            }
        }

        public boolean isEmpty() {
            synchronized (mutex) {
                return c.isEmpty();
            }
        }

        public boolean contains(Object o) {
            synchronized (mutex) {
                return c.contains(o);
            }
        }

        public Object[] toArray() {
            synchronized (mutex) {
                return c.toArray();
            }
        }

        public <T> T[] toArray(T[] a) {
            synchronized (mutex) {
                return c.toArray(a);
            }
        }

        public Iterator<E> iterator() {
            return c.iterator(); // Must be manually synched by user!
        }

        public boolean add(E o) {
            synchronized (mutex) {
                return c.add(o);
            }
        }

        public boolean remove(Object o) {
            synchronized (mutex) {
                return c.remove(o);
            }
        }

        public boolean containsAll(Collection<?> coll) {
            synchronized (mutex) {
                return c.containsAll(coll);
            }
        }

        public boolean addAll(Collection<? extends E> coll) {
            synchronized (mutex) {
                return c.addAll(coll);
            }
        }

        public boolean removeAll(Collection<?> coll) {
            synchronized (mutex) {
                return c.removeAll(coll);
            }
        }

        public boolean retainAll(Collection<?> coll) {
            synchronized (mutex) {
                return c.retainAll(coll);
            }
        }

        public void clear() {
            synchronized (mutex) {
                c.clear();
            }
        }

        @Override
        public String toString() {
            synchronized (mutex) {
                return c.toString();
            }
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            synchronized (mutex) {
                s.defaultWriteObject();
            }
        }
    }

    /**
     * Basically cloned from and identification to java.util.Collections.SynchronizedSet.
     * 
     * @author Joe D. Velopar
     */
    static class SynchronizedSet<E> extends SynchronizedCollection<E> 
            implements Set<E> 
    {
        private static final long serialVersionUID = 487447009682186044L;

        SynchronizedSet(Set<E> s) {
            super(s);
        }

        SynchronizedSet(Set<E> s, Object mutex) {
            super(s, mutex);
        }

        @Override
        public boolean equals(Object o) {
            synchronized (mutex) {
                return c.equals(o);
            }
        }

        @Override
        public int hashCode() {
            synchronized (mutex) {
                return c.hashCode();
            }
        }
    }
}
