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

import java.beans.Introspector;

/**
 * Spring-framework related utilities.
 * 
 * @author Joe D. Velopar
 */
public class SpringUtils {
	public static final SpringUtils inst = new SpringUtils();
	
	private SpringUtils() {}

	/**
	 * Returns a bean name by unqalifying and decapitalizing 
	 * the name of the given class. 
	 */
	public String toBeanName(Class c) {
		return Introspector.decapitalize(ClassUtils.inst.unqualify(c));
	}
}