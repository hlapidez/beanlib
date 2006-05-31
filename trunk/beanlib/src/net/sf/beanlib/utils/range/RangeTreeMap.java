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

package net.sf.beanlib.utils.range;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;

/**
 * Range Tree Map that supports retrieval by using a point that falls within
 * the range of a key which implements {@link net.sf.beanlib.utils.range.RangeBoundable}.
 * 
 * @author Joe D. Velopar
 */
public class RangeTreeMap<T,K extends RangeBoundable<T>,V> 
        extends ExtensibleTreeMap<K, V>
        implements SortedRangeMap<T,K,V>
{
    private static final long serialVersionUID = 1L;
    
    public RangeTreeMap() {
    }

    public RangeTreeMap(Comparator<? super K> c) {
        super(c);
    }

    public RangeTreeMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public RangeTreeMap(SortedMap<K, ? extends V> m) {
        super(m);
    }

    public V getByPoint(Comparable<T> point) {
        NodeEntry<K, V> p = getEntryByPoint(point);
        return p == null ? null : p.value;
    }
    
    private NodeEntry<K, V> getEntryByPoint(Comparable<T> point) 
    {
        NodeEntry<K, V> p = root;
        
        while (p != null) {
            int cmp = comparePointToRange(point, p.key);
            
            if (cmp == 0)
                return p;
            p = cmp < 0 ? p.left : p.right;
        }
        return null;
    }

    public boolean containsKeyPoint(Comparable<T> point) {
        return getEntryByPoint(point) != null;
    }
    
    /**
     * Compares a point to a range.
     * 
     * @param point given point
     * @param range given range
     * 
     * @return zero if the given point is within the given range (inclusive); 
     * less than zero if the point is below the range; or
     * greater than zero if the point is above the range.
     */
    private int comparePointToRange(Comparable<T> point, RangeBoundable<T> range) 
    {
        if (point.compareTo(range.getUpperBound()) <= 0) { 
            if (point.compareTo(range.getLowerBound()) >= 0)
                return 0;
            return -1;
        }
        return 1;
    }
}
