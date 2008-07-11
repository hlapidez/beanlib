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

import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;
import net.sf.beanlib.BeanlibException;
import net.sf.beanlib.PropertyInfo;
import net.sf.beanlib.spi.BeanMethodCollector;
import net.sf.beanlib.spi.BeanMethodFinder;
import net.sf.beanlib.spi.BeanPopulatable;
import net.sf.beanlib.spi.BeanPopulationExceptionHandler;
import net.sf.beanlib.spi.BeanPopulatorBaseConfig;
import net.sf.beanlib.spi.BeanPopulatorSpi;
import net.sf.beanlib.spi.BeanSourceHandler;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.DetailedBeanPopulatable;
import net.sf.beanlib.spi.Transformable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean Populator.
 * 
 * @author Joe D. Velopar
 */
@NotThreadSafe
public class BeanPopulator implements BeanPopulatorSpi 
{
    public static final Factory factory = new Factory();
    
    /**
     * Bean Populator Factory.
     * 
     * @author Joe D. Velopar
     */
    @ThreadSafe
    public static class Factory implements BeanPopulatorSpi.Factory {
        private Factory() {}
        
        public BeanPopulator newBeanPopulator(Object from, Object to)
        {
            return new BeanPopulator(from, to);
        }
    }

    public static BeanPopulator newBeanPopulator(Object from, Object to) {
        return factory.newBeanPopulator(from, to);
    }

    private final Log log = LogFactory.getLog(this.getClass());

    private final Object fromBean;
    private final Object toBean;

    private BeanPopulatorBaseConfig baseConfig = new BeanPopulatorBaseConfig();
    
    private Transformable transformer = BeanTransformer.newBeanTransformer(factory);
    
    /**
     * @param fromBean from bean
     * @param toBean to bean
     */
    private BeanPopulator(Object fromBean, Object toBean ) {
        this.fromBean = fromBean;
        this.toBean = toBean;
    }

    private BeanTransformerSpi getTransformerSpi() {
        return (BeanTransformerSpi)(transformer instanceof BeanTransformerSpi ? transformer : null); 
    }
    /** 
     * Returns the to-bean with setter methods invoked with values retreived 
     * from the getter methods of the from-bean.
     */
    @SuppressWarnings("unchecked")
    public <T> T populate() 
    {
        if (getTransformerSpi() != null)
            getTransformerSpi().getClonedMap().put(fromBean, toBean);
        // invoking all declaring setter methods of toBean from all matching getter methods of fromBean
        for (Method m : baseConfig.getSetterMethodCollector().collect(toBean))
            processSetterMethod(m);
        return (T)toBean;
    }
    /**
     * Processes a specific setter method for the toBean.
     * 
     * @param setterMethod a specific method of the toBean
     */
    private void processSetterMethod(Method setterMethod)
    {
        String methodName = setterMethod.getName();
        final String propertyString = methodName.substring(baseConfig.getSetterMethodCollector().getMethodPrefix().length());
        
        if (baseConfig.isDebug()) {
            if (log.isInfoEnabled())
                log.info(new StringBuilder("processSetterMethod: processing propertyString=")
                        .append(propertyString).append("")
                        .append(", fromClass=").append(fromBean.getClass())
                        .append(", toClass=").append(toBean.getClass())
                        .toString());
        }
        Method readerMethod = baseConfig.getReaderMethodFinder().find(propertyString, fromBean);
        
        if (readerMethod == null)
            return;
        // Reader method of fromBean found
        Class<?> paramType = setterMethod.getParameterTypes()[0];
        String propertyName = Introspector.decapitalize(propertyString);
        try {
            doit(setterMethod, readerMethod, paramType, propertyName);
//        } catch (InvocationTargetException ex) {
//            baseConfig.getBeanPopulationExceptionHandler()
//                .initFromBean(fromBean).initToBean(toBean)
//                .initPropertyName(propertyName)
//                .initReaderMethod(readerMethod).initSetterMethod(setterMethod)
//                .handleException(ex.getTargetException(), log);
        } catch (Exception ex) {
            baseConfig.getBeanPopulationExceptionHandler()
                .initFromBean(fromBean).initToBean(toBean)
                .initPropertyName(propertyName)
                .initReaderMethod(readerMethod).initSetterMethod(setterMethod)
                .handleException(ex, log);
        } 
    }

    private <T> void doit(
            Method setterMethod, Method readerMethod, Class<T> paramType, final String propertyName)
//        throws InvocationTargetException, IllegalAccessException 
    {
        if (baseConfig.getDetailedBeanPopulatable() != null) {
            if (!baseConfig.getDetailedBeanPopulatable()
                           .shouldPopulate(propertyName, fromBean, readerMethod, toBean, setterMethod))
                return;
        }
        if (baseConfig.getBeanPopulatable() != null) {
            if (!baseConfig.getBeanPopulatable().shouldPopulate(propertyName, readerMethod))
                return;
        }
        Object propertyValue = this.invokeMethodAsPrivileged(fromBean, readerMethod, null);
        
        if (baseConfig.getBeanSourceHandler() != null)
            baseConfig.getBeanSourceHandler().handleBeanSource(fromBean, readerMethod, propertyValue);
        
        if (transformer != null) {
            PropertyInfo propertyInfo = new PropertyInfo(propertyName, fromBean, toBean);
            propertyValue = transformer.transform(propertyValue, paramType, propertyInfo);
        }
        
        if (baseConfig.isDebug()) {
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
    {
        if (Modifier.isPublic(method.getModifiers())) {
            try {
                return method.invoke(target, args);
            } catch(IllegalAccessException ex) {
                // drop thru to try again
            } catch (InvocationTargetException e) {
                throw new BeanlibException(e.getTargetException());
            } catch(RuntimeException ex) {
                // the try-catch is unnecessary i know, 
                // but just so we can set a break point here
                throw ex;
            }
        }
        return AccessController.doPrivileged(
            new PrivilegedAction<Object>() {
                public Object run() {
                    method.setAccessible(true);
                    try {
                        return method.invoke(target, args);
                    } catch (IllegalArgumentException e) {
                        throw new BeanlibException(e);
                    } catch (IllegalAccessException e) {
                        throw new BeanlibException(e);
                    } catch (InvocationTargetException e) {
                        throw new BeanlibException(e.getTargetException());
                    }
                }
        });
    }
    
    /**
     * @param beanPopulatable     optionally used to control if a specific value read should get populated across
     */
    public BeanPopulator initBeanPopulatable(BeanPopulatable beanPopulatable) {
        baseConfig.setBeanPopulatable(beanPopulatable);

        if (this.getTransformerSpi() != null)
            this.getTransformerSpi().initBeanPopulatable(beanPopulatable);
        return this;
    }
    
    /**
     * @param beanSourceHandler optionally used to do something with the value read before the population
     */
    public BeanPopulator initBeanSourceHandler(BeanSourceHandler beanSourceHandler) {
        baseConfig.setBeanSourceHandler(beanSourceHandler);
        
        if (this.getTransformerSpi() != null)
            this.getTransformerSpi().initBeanSourceHandler(beanSourceHandler);
        return this;
    }
    
    /**
     * @param debug debug mode.
     */
    public BeanPopulator initDebug(boolean debug) {
        baseConfig.setDebug(debug);

        if (this.getTransformerSpi() != null)
            this.getTransformerSpi().initDebug(debug);
        return this;
    }
    
    /**
     * @param detailedBeanPopulatable optionally used to override the population decisions.
     */
    public BeanPopulator initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable) 
    {
        baseConfig.setDetailedBeanPopulatable(detailedBeanPopulatable);

        if (this.getTransformerSpi() != null)
            this.getTransformerSpi().initDetailedBeanPopulatable(detailedBeanPopulatable);
        return this;
    }
    
    public BeanPopulator initReaderMethodFinder(BeanMethodFinder readerMethodFinder) {
        if (readerMethodFinder != null) {
            baseConfig.setReaderMethodFinder(readerMethodFinder);

            if (this.getTransformerSpi() != null)
                this.getTransformerSpi().initReaderMethodFinder(readerMethodFinder);
        }
        return this;
    }

    public BeanPopulator initSetterMethodCollector(BeanMethodCollector setterMethodCollector) {
        if (setterMethodCollector != null) {
            baseConfig.setSetterMethodCollector(setterMethodCollector);

            if (this.getTransformerSpi() != null)
                this.getTransformerSpi().initSetterMethodCollector(setterMethodCollector);
        }
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
        
        if (this.getTransformerSpi() != null)
            this.getTransformerSpi().initBeanPopulatorBaseConfig(baseConfig);
        return this;
    }
    
    public BeanPopulator initBeanPopulationExceptionHandler(BeanPopulationExceptionHandler beanPopulationExceptionHandler) {
        baseConfig.setBeanPopulationExceptionHandler(beanPopulationExceptionHandler);
        
        if (this.getTransformerSpi() != null)
            this.getTransformerSpi().initBeanPopulationExceptionHandler(beanPopulationExceptionHandler);
        return this;
    }

    public BeanPopulator initBeanPopulatorBaseConfig(BeanPopulatorBaseConfig baseConfig) {
        this.baseConfig = baseConfig;
        return this;
    }
}
