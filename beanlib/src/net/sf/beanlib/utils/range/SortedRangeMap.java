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

import java.util.SortedMap;

/**
 * An extension of {@link java.util.SortedMap} that allows key with ranges
 * to be retrieved with a specified point.
 * 
 * @author Joe D. Velopar
 */
public interface SortedRangeMap<T,K,V> extends SortedMap<K,V> {
    /**
     * Returns the value to which this map maps a key with a range 
     * that the specified point falls within.
     * Returns <tt>null</tt> if the map contains no mapping for any key with such range.  
     * A return value of <tt>null</tt> does not <i>necessarily</i> indicate that the
     * map contains no mapping for a key with such range; it's also possible that the map
     * explicitly maps the key with such range to <tt>null</tt>.  The <tt>containsKeyPoint</tt>
     * operation may be used to distinguish these two cases.
     *
     * @param point the point that falls within the range of a key 
     * whose associated value is to be returned.
     * @return the value to which this map maps a key with a range 
     * that the specified point falls within, or
     * <tt>null</tt> if the map contains no mapping for any key with such range.
     *
     * @see #containsKeyPoint(Comparable)
     */
    public V getByPoint(Comparable<T> point);
    
    /**
     * Returns <tt>true</tt> if this map contains a mapping of a key 
     * with a range that the specified point falls within.
     *
     * @param point point to be tested.
     * @return <tt>true</tt> if this map contains a mapping of a key with a range 
     * that the specified point falls within.
     */
    public boolean containsKeyPoint(Comparable<T> point);
}
