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

import java.util.Iterator;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Joe D. Velopar
 */
public class HibernateBeanReplicatorTestMap extends TestCase {
	private Log log = LogFactory.getLog(this.getClass());
	public void testDeepCopyMap() {
		FooWithMap fooMap = new FooWithMap(null);
		fooMap.addToMap("1", "a");
		fooMap.addToMap("2", "b");
		// Test recursive references
		fooMap.addToMap("3", fooMap);
		
		FooWithList fooList = new FooWithList();
		fooList.addToList("1");
		fooList.addToList("2");
		fooList.setFooWithList(fooList);
		// Test recursive references
		fooList.addToList(fooList);
		fooList.addToList(fooList.getList());
		fooMap.addToMap("4", fooList);
		FooWithMap toMap = (FooWithMap)new Hibernate3BeanReplicator().deepCopy(fooMap);

		assertFalse(fooMap.getMap() == toMap.getMap());
		
		Iterator itr1=fooMap.getMap().entrySet().iterator();
		Iterator itr2=toMap.getMap().entrySet().iterator();
		
		while (itr1.hasNext()) {
			Map.Entry n1 = (Map.Entry)itr1.next();
			Map.Entry n2 = (Map.Entry)itr2.next();
			log.debug("n1="+n1+", n2="+n2);
			
			if (n1.getKey()  instanceof String && n1.getValue() instanceof String) {
				assertEquals(n1, n2);
			}
		}
		assertFalse(itr2.hasNext());
	}
}
