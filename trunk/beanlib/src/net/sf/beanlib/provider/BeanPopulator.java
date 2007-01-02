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

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

import net.sf.beanlib.BeanlibException;
import net.sf.beanlib.PublicReaderMethodFinder;
import net.sf.beanlib.PublicSetterMethodCollector;
import net.sf.beanlib.api.BeanMethodCollector;
import net.sf.beanlib.api.BeanMethodFinder;
import net.sf.beanlib.api.BeanPopulatable;
import net.sf.beanlib.api.BeanPopulationExceptionHandler;
import net.sf.beanlib.api.BeanSourceHandler;
import net.sf.beanlib.api.DetailedBeanPopulatable;
import net.sf.beanlib.api.Transformable;
import net.sf.beanlib.spi.BeanPopulatorSpi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean Populator.  Not thread safe.
 * 
 * @author Joe D. Velopar
 */
public class BeanPopulator implements BeanPopulatorSpi 
{
    public static final Factory factory = new Factory();
    
    public static class Factory implements BeanPopulatorSpi.Factory {
        private Factory() {}
        
        public BeanPopulatorSpi newBeanPopulator(Object from, Object to)
        {
            return new BeanPopulator(from, to);
        }
    }

    private final Log log = LogFactory.getLog(this.getClass());

	private final Object fromBean;
	private final Object toBean;

	private BeanMethodCollector setterMethodCollector = PublicSetterMethodCollector.inst;
	private BeanMethodFinder readerMethodFinder = PublicReaderMethodFinder.inst;

	private BeanSourceHandler beanSourceHandler;
	private Transformable transformer;
	
	private DetailedBeanPopulatable detailedBeanPopulatable = DetailedBeanPopulatable.JAVABEAN_POPULATE;
	private BeanPopulatable beanPopulatable;
	
	private BeanPopulationExceptionHandler beanPopulationExceptionHandler = BeanPopulationExceptionHandler.ABORT;
	
	private boolean debug;
	
	/**
	 * @param fromBean from bean
	 * @param toBean to bean
	 */
	private BeanPopulator(Object fromBean, Object toBean ) {
		this.fromBean = fromBean;
		this.toBean = toBean;
	}
	
	/** 
	 * Returns the to-bean with setter methods invoked with values retreived 
	 * from the getter methods of the from-bean.
	 */
	public Object populate() 
	{
		// invoking all declaring setter methods of toBean from all matching getter methods of fromBean
        for (Method m : setterMethodCollector.collect(toBean))
            processSetterMethod(m);
		return toBean;
	}
	/**
	 * Processes a specific setter method for the toBean.
	 * 
	 * @param setterMethod a specific method of the toBean
	 */
	private void processSetterMethod(Method setterMethod)
	{
		String methodName = setterMethod.getName();
		final String propertyString = methodName.substring(setterMethodCollector.getMethodPrefix().length());
		
		if (debug) {
			if (log.isInfoEnabled())
				log.info(new StringBuilder("processSetterMethod: processing propertyString=")
						.append(propertyString).append("")
						.append(", fromClass=").append(fromBean.getClass())
						.append(", toClass=").append(toBean.getClass())
						.toString());
		}
		Method readerMethod = readerMethodFinder.find(propertyString, fromBean);
		
		if (readerMethod == null)
			return;
		// Reader method of fromBean found
		Class<?> paramType = setterMethod.getParameterTypes()[0];
		String propertyName = Introspector.decapitalize(propertyString);
		try {
			doit(setterMethod, readerMethod, paramType, propertyName);
		} catch (InvocationTargetException ex) {
			this.beanPopulationExceptionHandler
				.initFromBean(fromBean).initToBean(toBean)
				.initPropertyName(propertyName)
				.initReaderMethod(readerMethod).initSetterMethod(setterMethod)
				.handleException(ex.getTargetException(), log);
		} catch (Exception ex) {
			this.beanPopulationExceptionHandler
				.initFromBean(fromBean).initToBean(toBean)
				.initPropertyName(propertyName)
				.initReaderMethod(readerMethod).initSetterMethod(setterMethod)
				.handleException(ex, log);
		} 
	}

	private <T> void doit(Method setterMethod, Method readerMethod, Class<T> paramType, String propertyName) 
		throws InvocationTargetException, IllegalAccessException 
	{
		if (detailedBeanPopulatable != null) {
			if (!detailedBeanPopulatable.shouldPopulate(propertyName, fromBean, readerMethod, toBean, setterMethod))
				return;
		}
		if (beanPopulatable != null) {
			if (!beanPopulatable.shouldPopulate(propertyName, readerMethod))
				return;
		}
		Object propertyValue = this.invokeMethodAsPrivileged(fromBean, readerMethod, null);
		
		if (beanSourceHandler != null)
			beanSourceHandler.handleBeanSource(propertyValue);
        
		if (transformer != null)
			propertyValue = transformer.transform(propertyValue, paramType);
        
		if (debug) {
			if (log.isInfoEnabled())
				log.info("processSetterMethod: setting propertyName=" + propertyName);
		}
		// Invoke setter method  
		Object[] args = {propertyValue};
		this.invokeMethodAsPrivileged(toBean, setterMethod, args);
		return;
	}
	/** 
	 * Invoke the given method as a privileged action, if necessary. 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	private Object invokeMethodAsPrivileged(final Object target, final Method method, final Object[] args) 
		throws InvocationTargetException, IllegalAccessException 
	{
		if (Modifier.isPublic(method.getModifiers()))
			return method.invoke(target, args);
		return AccessController.doPrivileged(
			new PrivilegedAction<Object>() {
				public Object run() {
					method.setAccessible(true);
					try {
						return method.invoke(target, args);
					} catch (IllegalArgumentException e) {
						log.error("", e);
						throw new BeanlibException(e);
					} catch (IllegalAccessException e) {
						log.error("", e);
						throw new BeanlibException(e);
					} catch (InvocationTargetException e) {
						log.error("", e.getTargetException());
						throw new BeanlibException(e.getTargetException());
					}
				}
		});
	}
	public BeanPopulatable getBeanPopulatable() {
		return beanPopulatable;
	}
	
	/**
	 * @param beanPopulatable 	optionally used to control if a specific value read should get populated across
	 */
	public BeanPopulator initBeanPopulatable(BeanPopulatable beanPopulatable) {
		this.beanPopulatable = beanPopulatable;
		return this;
	}
	public BeanSourceHandler getBeanSourceHandler() {
		return beanSourceHandler;
	}
	
	/**
	 * @param beanSourceHandler optionally used to do something with the value read before the population
	 */
	public BeanPopulator initBeanSourceHandler(BeanSourceHandler beanSourceHandler) {
		this.beanSourceHandler = beanSourceHandler;
		return this;
	}
	public boolean isDebug() {
		return debug;
	}
	
	/**
	 * @param debug debug mode.
	 */
	public BeanPopulator initDebug(boolean debug) {
		this.debug = debug;
		return this;
	}
	public DetailedBeanPopulatable getDetailedBeanPopulatable() {
		return detailedBeanPopulatable;
	}
	
	/**
	 * @param detailedBeanPopulatable optionally used to override the population decisions.
	 */
	public BeanPopulator initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable) 
	{
		this.detailedBeanPopulatable = detailedBeanPopulatable;
		return this;
	}
    
	public BeanMethodFinder getReaderMethodFinder() {
		return readerMethodFinder;
	}
    
	public BeanPopulator initReaderMethodFinder(BeanMethodFinder readerMethodFinder) {
		if (readerMethodFinder != null)
			this.readerMethodFinder = readerMethodFinder;
		return this;
	}

    public BeanMethodCollector getSetterMethodCollector() {
		return setterMethodCollector;
	}
    
	public BeanPopulator initSetterMethodCollector(BeanMethodCollector setterMethodCollector) {
		if (setterMethodCollector != null)
			this.setterMethodCollector = setterMethodCollector;
		return this;
	}
    
	public Transformable getTransformer() {
		return transformer;
	}

	/**
	 * @param transformer optionally used to transform the value for the population
	 */
	public BeanPopulator initTransformer(Transformable transformer) {
		this.transformer = transformer;
		return this;
	}
	public BeanPopulator initBeanPopulationExceptionHandler(BeanPopulationExceptionHandler beanPopulationExceptionHandler) {
		this.beanPopulationExceptionHandler = beanPopulationExceptionHandler;
		return this;
	}
}