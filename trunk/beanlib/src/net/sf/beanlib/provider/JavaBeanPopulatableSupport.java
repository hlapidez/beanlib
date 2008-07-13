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
package net.sf.beanlib.provider;

import java.lang.reflect.Method;

import net.sf.beanlib.spi.DetailedBeanPopulatable;

/**
 * Default implementation of {@link DetailedBeanPopulatable}.
 * 
 * @author Joe D. Velopar
 */
public class JavaBeanPopulatableSupport implements DetailedBeanPopulatable {
	public static final JavaBeanPopulatableSupport inst = new JavaBeanPopulatableSupport();
	
	private JavaBeanPopulatableSupport() {}

	public boolean shouldPopulate(
            @SuppressWarnings("unused") String propertyName, 
            @SuppressWarnings("unused") Object fromBean, 
            Method readerMethod, 
            @SuppressWarnings("unused") Object toBean, 
            Method setterMethod) 
	{
		Class<?> returnType = readerMethod.getReturnType();
		Class<?> paramType = setterMethod.getParameterTypes()[0];
		return paramType.isAssignableFrom(returnType);
	}
}