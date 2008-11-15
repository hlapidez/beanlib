/*
 * Copyright 2007 The Apache Software Foundation.
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
package net.sf.beanlib.spi;


/**
 * The base interface of the Bean Populator SPI.
 * This SPI provides various options to control the propagation behavior of JavaBean properties.
 * 
 * @author Joe D. Velopar
 */
public interface BeanPopulatorBaseSpi 
{
    /**
     * Used to configure a bean populatable to 
     * determine whether a specific JavaBean property should be propagated
     * from a source bean to a target bean.
     * 
     * @param beanPopulatable is similar to {@link DetailedBeanPopulatable} but with a simpler API
     * that is used to control whether a specific JavaBean property should be propagated
     * from a source bean to a target bean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public BeanPopulatorBaseSpi initBeanPopulatable(BeanPopulatable beanPopulatable);

    /**
     * Used to configure a detailed bean populatable (which has more context information) 
     * to determine whether a specific JavaBean property
     * should be propagated from the source bean to the target bean.
     * 
     * @param detailedBeanPopulatable is used to control whether a specific JavaBean property
     * should be propagated from the source bean to the target bean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public BeanPopulatorBaseSpi initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable);
    
    /**
     * Used to configure a call-back 
     * (to produce whatever side-effects deemed necessary) that is invoked
     * after the property value has been retrieved from the source bean, 
     * but before being propagated across to the target bean.
     * 
     * @param beanSourceHandler can be used to act as a call-back 
     * (to produce whatever side-effects deemed necessary)
     * after the property value has been retrieved from the source bean, 
     * but before being propagated across to the target bean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public BeanPopulatorBaseSpi initBeanSourceHandler(BeanSourceHandler beanSourceHandler);
    
    /**
     * Used to configure a finder to find the property getter methods of a source JavaBean.
     * 
     * @param readerMethodFinder can be used to find the property getter methods of a source JavaBean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public BeanPopulatorBaseSpi initReaderMethodFinder(BeanMethodFinder readerMethodFinder);
    
    /**
     * Used to configure a collector to collect the property setter methods of a target JavaBean.
     * 
     * @param setterMethodCollector can be used to collect the property setter methods of a target JavaBean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public BeanPopulatorBaseSpi initSetterMethodCollector(BeanMethodCollector setterMethodCollector);

    /**
     * Used to configure a handler to handle any exception thrown.
     * 
     * @param beanPopulationExceptionHandler can be used to handle any exception thrown.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public BeanPopulatorBaseSpi initBeanPopulationExceptionHandler(BeanPopulationExceptionHandler beanPopulationExceptionHandler);

    /**
     * Used to control whether debug messages should be logged.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public BeanPopulatorBaseSpi initDebug(boolean debug);
    
    /**
     * Used to conveniently provide all the other configuration options as a single 
     * configuration object.
     * 
     * @param baseConfig is used to conveniently group all the other initializable options into a single unit.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public BeanPopulatorBaseSpi initBeanPopulatorBaseConfig(BeanPopulatorBaseConfig baseConfig);
}
