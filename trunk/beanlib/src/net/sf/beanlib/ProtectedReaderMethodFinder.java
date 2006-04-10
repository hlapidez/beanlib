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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.beanlib.api.BeanMethodFinder;

/**
 * Supports finding JavaBean reader method, either public or protected.
 *   
 * @author Joe D. Velopar
 */
public class ProtectedReaderMethodFinder implements BeanMethodFinder {
	public static final ProtectedReaderMethodFinder inst = new ProtectedReaderMethodFinder();
	
	private ProtectedReaderMethodFinder() {}
	
	public Method find(final String propertyName, Object bean) {
		String s= propertyName;
		
		if (Character.isLowerCase(propertyName.charAt(0))) {
			s = propertyName.substring(0, 1).toUpperCase();
			
			if (propertyName.length() > 1)
				s += propertyName.substring(1);
		}
		Class beanClass = bean.getClass();
		
		while (beanClass != Object.class) {
			Method m = null;
			try {
				// Find the public member method of the class or interface,
				// recursively on super classes and interfaces as necessary.
				m = beanClass.getDeclaredMethod("get" + s);
				
				if (found(m))
					return m;
			} catch (NoSuchMethodException ignore) {
			}
			try {
				m = beanClass.getDeclaredMethod("is" + s);

				if (found(m))
					return m;
			} catch (NoSuchMethodException ignore) {
			}
			// climb to the super class and repeat
			beanClass = beanClass.getSuperclass();
		}
		return null;
	}
	private boolean found(Method m) {
		final int modifier = m.getModifiers();
		return !Modifier.isStatic(modifier) && !Modifier.isPrivate(modifier);
	}
}