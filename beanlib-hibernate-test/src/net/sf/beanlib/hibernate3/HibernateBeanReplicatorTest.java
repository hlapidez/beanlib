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

import junit.framework.TestCase;
import net.sf.beanlib.Bar;
import net.sf.beanlib.BeanPopulator;
import net.sf.beanlib.Foo;
import net.sf.beanlib.Type1;
import net.sf.beanlib.Type2;
import net.sf.beanlib.api.DetailedBeanPopulatable;
import net.sf.beanlib.hibernate.HibernateBeanReplicator;

/**
 * @author Joe D. Velopar
 */
public class HibernateBeanReplicatorTest extends TestCase {
	public void testDeepCopy() {
		Foo foo = new Foo();
		foo.setBoo(true);
		foo.setString("from");
		
		HibernateBeanReplicator replicator = new Hibernate3BeanReplicator();
		
		Object to = replicator.deepCopy(foo);
		assertFalse(foo == to);
		assertEquals(foo, to);

		Object to2 = replicator.deepCopy(foo);
		assertFalse(to == to2);
		assertEquals(to, to2);
	}
	public void testDeepCopy2() {
		Bar bar = new Bar();
		bar.setBoo(true);
		bar.setString("from");
		bar.setBarString("barString");
		bar.setBar(bar);
		
		HibernateBeanReplicator replicator = new Hibernate3BeanReplicator();
		
		Bar to = (Bar)replicator.deepCopy(bar);
		assertEquals(bar.getBarString(), to.getBarString());
		assertEquals(bar.getString(), to.getString());
		assertTrue(bar == bar.getBar());
		assertTrue(to.getBar() == to);
		assertFalse(to.getBar() == bar);
		
		Bar to2 = (Bar)replicator.deepCopy(bar);
		assertEquals(bar.getBarString(), to2.getBarString());
		assertEquals(bar.getString(), to2.getString());
		assertTrue(bar == bar.getBar());
		assertTrue(to2.getBar() == to2);
		assertFalse(to2.getBar() == bar);

	}
	public void testShallowCopy() {
		Foo foo = new Foo();
		foo.setBoo(true);
		foo.setString("from");
		
		HibernateBeanReplicator replicator = new Hibernate3BeanReplicator();
		
		Object to = replicator.shallowCopy(foo);
		assertFalse(foo == to);
		assertEquals(foo, to);
		
		Object to2 = replicator.shallowCopy(foo);
		assertFalse(to == to2);
		assertEquals(foo, to2);
	}
	public void testShallowCopy2() {
		Bar bar = new Bar();
		bar.setBoo(true);
		bar.setString("from");
		bar.setBarString("barString");
		bar.setBar(bar);
		
		Bar to = (Bar)new Hibernate3BeanReplicator().shallowCopy(bar);
		assertEquals(bar.getBarString(), to.getBarString());
		assertEquals(bar.getString(), to.getString());
		assertTrue(bar == bar.getBar());
		assertNull(to.getBar());
	}
	
	public void testDeepCopyProtected() {
		Foo foo = new Foo("foo");
		foo.setBoo(true);
		foo.setString("from");
		
//		Foo to = (Foo)new HibernateBeanReplicator(foo).initDebug(true).deepCopy();
//		assertEquals(foo.getString(), to.getString());
//		assertFalse(foo.getProtectedSetString().equals(to.getProtectedSetString()));
//		assertNull(to.getProtectedSetString());
		
		Foo to = (Foo)new Hibernate3BeanReplicator().initDebug(false).deepCopy(foo);
		assertEquals(foo.getString(), to.getString());
		assertEquals(foo.getProtectedSetString(), to.getProtectedSetString());
		assertNotNull(to.getProtectedSetString());
	}
	public void testDeepCopyProtected2() {
		Bar bar = new Bar("bar");
		bar.setBoo(true);
		bar.setString("from");
		bar.setBarString("barString");
		bar.setBar(bar);
		
//		Bar to = (Bar)new HibernateBeanReplicator(bar).initDebug(false).deepCopy();
//		assertEquals(bar.getBarString(), to.getBarString());
//		assertEquals(bar.getString(), to.getString());
//		assertTrue(bar == bar.getBar());
//		assertTrue(to.getBar() == to);
//		assertFalse(to.getBar() == bar);
//		assertFalse(bar.getProtectedSetString().equals(to.getProtectedSetString()));
//		assertNull(to.getProtectedSetString());
		
		Bar to = (Bar)new Hibernate3BeanReplicator().initDebug(false).deepCopy(bar);
		assertEquals(bar.getBarString(), to.getBarString());
		assertEquals(bar.getString(), to.getString());
		assertTrue(bar == bar.getBar());
		assertTrue(to.getBar() == to);
		assertFalse(to.getBar() == bar);
		assertTrue(bar.getProtectedSetString().equals(to.getProtectedSetString()));
		assertNotNull(to.getProtectedSetString());
	}
	
	public void testShallowCopyProtected() {
		Foo foo = new Foo("foo");
		foo.setBoo(true);
		foo.setString("from");
		
//		Foo to = (Foo)new HibernateBeanReplicator(foo).initDebug(false).shallowCopy();
//		assertEquals(foo.getString(), to.getString());
//		assertNull(to.getProtectedSetString());
		
		Foo to = (Foo)new Hibernate3BeanReplicator().initDebug(false).shallowCopy(foo);
		assertEquals(foo.getString(), to.getString());
		assertNotNull(to.getProtectedSetString());
		assertEquals(foo.getProtectedSetString(), to.getProtectedSetString());
	}
	
	public void testShallowCopyProtected2() {
		Bar bar = new Bar("bar");
		bar.setBoo(true);
		bar.setString("from");
		bar.setBarString("barString");
		bar.setBar(bar);
		
//		Bar to = (Bar)new HibernateBeanReplicator(bar).initDebug(false).shallowCopy();
//		assertEquals(bar.getBarString(), to.getBarString());
//		assertEquals(bar.getString(), to.getString());
//		assertTrue(bar == bar.getBar());
//		assertNull(to.getProtectedSetString());
//		assertNull(to.getBar());
		
		Bar to = (Bar)new Hibernate3BeanReplicator().initDebug(false).shallowCopy(bar);
		assertEquals(bar.getBarString(), to.getBarString());
		assertEquals(bar.getString(), to.getString());
		assertTrue(bar == bar.getBar());
		assertNotNull(to.getProtectedSetString());
		assertEquals(bar.getProtectedSetString(), to.getProtectedSetString());
		assertNull(to.getBar());
	}
	public void testDeepCopyRegardless() {
		Type1 t1 = new Type1();
		t1.setF1("f1 of type1");
		t1.setF2("f2 of type1");
		Type2 type = new Type2();
		type.setF1("f1 of typ2");
		type.setF2("f2 of typ2");
		t1.setType(type);
		
		Type2 t2 = new Type2();
		new BeanPopulator(t1, t2)
			.initDetailedBeanPopulatable(DetailedBeanPopulatable.ALWAYS_POPULATE)
			.initTransformer(new Hibernate3BeanTransformer())
			.populate();
		assertEquals(t1.getF1(), t2.getF1());
		assertEquals(t1.getF2(), t2.getF2());
		assertEquals(t1.getType().getF1(), t2.getType().getF1());
		assertEquals(t1.getType().getF2(), t2.getType().getF2());
	}

}
