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
package net.sf.beanlib.api;

import java.lang.reflect.Method;

import net.sf.beanlib.BeanlibException;

import org.apache.commons.logging.Log;

/** 
 * Used to handle exception thrown during the population of a bean property.
 * 
 *  @author Joe D. Velopar
 */
public interface BeanPopulationExceptionHandler {
	/** Exception handler which aborts by throwing or re-throwing a RuntimeException or Error. */
	public static final BeanPopulationExceptionHandler ABORT = new BeanPopulationExceptionHandler() {
		public void handleException(Throwable t, Log log) {
			log.error("", t);
			
			if (t instanceof RuntimeException)
				throw (RuntimeException)t;
			if (t instanceof Error)
				throw (Error)t;
			throw new BeanlibException(t);
		}
		public BeanPopulationExceptionHandler initPropertyName(@SuppressWarnings("unused") String propertyName) {return this;}
		public BeanPopulationExceptionHandler initFromBean(@SuppressWarnings("unused") Object fromBean) {return this;}
		public BeanPopulationExceptionHandler initReaderMethod(@SuppressWarnings("unused") Method readerMethod) {return this;}
		public BeanPopulationExceptionHandler initToBean(@SuppressWarnings("unused") Object toBean) {return this;}
		public BeanPopulationExceptionHandler initSetterMethod(@SuppressWarnings("unused") Method setterMethod) {return this;}
	};
	/** Exception handler which always continue by logging and then igoring the exception or error. */
	public static final BeanPopulationExceptionHandler CONTINUE = new BeanPopulationExceptionHandler() {
		public void handleException(Throwable t, Log log) {
			log.warn("", t);
		}
		public BeanPopulationExceptionHandler initPropertyName(@SuppressWarnings("unused") String propertyName) {return this;}
		public BeanPopulationExceptionHandler initFromBean(@SuppressWarnings("unused") Object fromBean) {return this;}
		public BeanPopulationExceptionHandler initReaderMethod(@SuppressWarnings("unused") Method readerMethod) {return this;}
		public BeanPopulationExceptionHandler initToBean(@SuppressWarnings("unused") Object toBean) {return this;}
		public BeanPopulationExceptionHandler initSetterMethod(@SuppressWarnings("unused") Method setterMethod) {return this;}
	};
	
	/** Handles the exception thrown during the population of a bean property. */
	public void handleException(Throwable t, Log log);
	/** Sets the name of the property being populated. */
	public BeanPopulationExceptionHandler initPropertyName(String propertyName);
	/** Sets the bean from which the property is populated from, ie the source bean. */
	public BeanPopulationExceptionHandler initFromBean(Object fromBean);
	/** Sets the reader method used to retreive the value from the source bean. */
	public BeanPopulationExceptionHandler initReaderMethod(Method readerMethod);
	/** Sets the bean to which the property is populated to, ie the target bean. */
	public BeanPopulationExceptionHandler initToBean(Object toBean);
	/** Sets the setter method used to set the value to the target bean. */
	public BeanPopulationExceptionHandler initSetterMethod(Method setterMethod);
}
