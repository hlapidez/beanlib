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
package net.sf.beanlib.provider.collector;

import static org.junit.Assert.assertTrue;
import junit.framework.JUnit4TestAdapter;
import net.sf.beanlib.hibernate.HibernateBeanReplicator;
import net.sf.beanlib.hibernate3.Hibernate3BeanReplicator;

import org.junit.Test;

public class OrderedMethodCollectorTest {
    
    @Test
    public void test() {
        A a1 = new A("a1");
        A a2 = new A();
        a1.addA(a1);
        a1.addA(a2);
        assertTrue(2 == a1.getAset().size());
        
        HibernateBeanReplicator replicator = new Hibernate3BeanReplicator()
                                                .initSetterMethodCollector(new OrderedMethodCollector());
        A a1clone = replicator.copy(a1);
        assertTrue(a1clone.getAset().size() == 2);
    }
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(OrderedMethodCollectorTest.class);
    }
}
