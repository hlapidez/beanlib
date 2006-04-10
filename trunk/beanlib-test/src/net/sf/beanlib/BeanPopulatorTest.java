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
package net.sf.beanlib;

import junit.framework.TestCase;

/**
 * @author Joe D. Velopar
 */
public class BeanPopulatorTest extends TestCase {
	public void test() {
		Foo from = new Foo("from");
		from.setBoo(true);
		from.setString("foo");
		Foo to = new Foo("to");
		assertFalse(from.equals(to));
		new BeanPopulator(from, to).initDebug(false).populate();
		assertEquals(from.getString(), to.getString());
		assertFalse(from.getProtectedSetString().equals(to.getProtectedSetString()));
	}
	
	public void test2() {
		Foo from = new Foo("from");
		from.setBoo(true);
		from.setString("foo");
		Foo to = new Foo("to");
		assertFalse(from.equals(to));
		new BeanPopulator(from, to).initDebug(false).populate();
		assertFalse(from.equals(to));
		assertFalse(from.getProtectedSetString().equals(to.getProtectedSetString()));
		assertEquals(from.getString(), to.getString());
		assertEquals(from.isBoo(), to.isBoo());
	}

	public void test4() {
		Bar from = new Bar("from");
		from.setBoo(true);
		from.setString("foo");
		from.setBarString("barString");
		from.setBar(from);
		Bar to = new Bar("to");
		new BeanPopulator(from, to).initDebug(false).populate();
		assertFalse(from.getProtectedSetString().equals(to.getProtectedSetString()));
		assertEquals(from.getString(), to.getString());
		assertEquals(from.isBoo(), to.isBoo());
		assertEquals(from.getBarString(), to.getBarString());
		assertSame(from.getBar(), to.getBar());
	}
	
	public void testProtected() {
		Foo from = new Foo("from");
		from.setBoo(true);
		from.setString("foo");
		Foo to = new Foo("to");
		assertFalse(from.equals(to));
		new BeanPopulator(from, to)
			.initDebug(false)
			.initSetterMethodCollector(ProtectedSetterMethodCollector.inst)
			.populate();
		assertEquals(from.getString(), to.getString());
		assertEquals(from.getProtectedSetString(), to.getProtectedSetString());
		assertEquals(from, to);
	}

	public void testProtected4() {
		Bar from = new Bar("from");
		from.setBoo(true);
		from.setString("foo");
		from.setBarString("barString");
		from.setBar(from);
		Bar to = new Bar("to");
		assertFalse(from.equals(to));
		new BeanPopulator(from, to)
			.initDebug(false)
			.initSetterMethodCollector(ProtectedSetterMethodCollector.inst)
			.populate();
		assertEquals(from.getProtectedSetString(), to.getProtectedSetString());
		assertEquals(from.getString(), to.getString());
		assertEquals(from.isBoo(), to.isBoo());
		assertEquals(from.getBarString(), to.getBarString());
		assertSame(from.getBar(), to.getBar());
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
			.populate();
		assertEquals(t1.getF1(), t2.getF1());
		assertEquals(t1.getF2(), t2.getF2());
		assertNotNull(t1.getType());
		assertNull(t2.getType());
	}
}
