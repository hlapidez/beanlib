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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean Setter.
 * 
 * @author Joe D. Velopar
 */
public class BeanSetter {
	/** Singleton instance. */
//	private static final boolean debug = false;
	public static final BeanSetter inst = new BeanSetter();
	private final Log log = LogFactory.getLog(this.getClass());

	/** Singleton. */
	private BeanSetter() {
	}
	/** Sets the property with the given string value for the given java bean. */
	public void setProperty(
		Object bean,
		PropertyDescriptor pd,
		String value) {
		try {
			Method m = pd.getWriteMethod();
			m.invoke(bean, new Object[] { value });
			return;
		} catch (IllegalAccessException e) {
			log.error("", e);
			throw new BeanlibException(e);
		} catch (InvocationTargetException e) {
			log.error("", e.getTargetException());
			throw new BeanlibException(e.getTargetException());
		}
	}
}