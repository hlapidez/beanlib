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

import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;
import net.sf.beanlib.utils.CollectionUtils;

import org.junit.Test;

/**
 * @author Joe D. Velopar
 */
public class RangeTreeMapTest extends TestCase {
    
    @Test
    public void test() 
    {
        this.testImpl(new RangeTreeMap<Integer,SimpleRange,String>());
        this.testImpl(CollectionUtils.synchronizedSortedRangeMap(new RangeTreeMap<Integer,SimpleRange,String>()));
    }
    
    private void testImpl(SortedRangeMap<Integer,SimpleRange,String> sortedRangeMap) 
    {
        sortedRangeMap.put(new SimpleRange(100, 300), "100-300");
        sortedRangeMap.put(new SimpleRange(400, 600), "400-600");
        
        assertNull(sortedRangeMap.getByPoint(1));
        assertFalse(sortedRangeMap.containsKeyPoint(1));
        assertNull(sortedRangeMap.getByPoint(99));
        assertFalse(sortedRangeMap.containsKeyPoint(99));
        assertNull(sortedRangeMap.getByPoint(301));
        assertFalse(sortedRangeMap.containsKeyPoint(301));
        assertNull(sortedRangeMap.getByPoint(399));
        assertFalse(sortedRangeMap.containsKeyPoint(399));
        assertNull(sortedRangeMap.getByPoint(601));
        assertFalse(sortedRangeMap.containsKeyPoint(601));
        
        assertEquals("100-300", sortedRangeMap.getByPoint(100));
        assertTrue(sortedRangeMap.containsKeyPoint(100));
        assertEquals("100-300", sortedRangeMap.getByPoint(300));
        assertTrue(sortedRangeMap.containsKeyPoint(300));
        assertEquals("100-300", sortedRangeMap.getByPoint(200));
        assertTrue(sortedRangeMap.containsKeyPoint(200));
        assertEquals("400-600", sortedRangeMap.getByPoint(400));
        assertTrue(sortedRangeMap.containsKeyPoint(400));
        assertEquals("400-600", sortedRangeMap.getByPoint(600));
        assertTrue(sortedRangeMap.containsKeyPoint(600));
        assertEquals("400-600", sortedRangeMap.getByPoint(500));
        assertTrue(sortedRangeMap.containsKeyPoint(500));
    }
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(RangeTreeMapTest.class);
    }

}
