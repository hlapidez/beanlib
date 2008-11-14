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
 * Bean Populator SPI.
 * This SPI provides various options to control the propagation behavior of JavaBean properties.
 * 
 * @see BeanPopulatorBaseSpi
 * 
 * @author Joe D. Velopar
 */
public interface BeanPopulatorSpi extends BeanPopulatorBaseSpi 
{
    /**
     * Bean Populator Factory SPI.
     * 
     * @author Joe D. Velopar
     */
    public static interface Factory {
        /** Returns a bean populator, given the from bean and to bean. */ 
        public BeanPopulatorSpi newBeanPopulator(Object from, Object to);
    }

    /**
     * @param transformer is used to transform every property value read from a source JavaBean
     * into a value to be to set the corresponding property of a target JavaBean.
     * 
     * @return the current object (ie this) for method chaining purposes.
     */
    public BeanPopulatorSpi initTransformer(Transformable transformer);

    /**
     * Returns the transformer used by this bean populator.
     */
    public Transformable getTransformer();
    
    /** 
     * Propagate every property from the source JavaBean to the target JavaBean. 
     */
    public <T> T populate(); 
    
    /**
     * Overrides for co-variant return type.
     * 
     * @see BeanPopulatorBaseSpi#initBeanPopulatable(BeanPopulatable)
     */
    public BeanPopulatorSpi initBeanPopulatable(BeanPopulatable beanPopulatable);
    
    /**
     * Overrides for co-variant return type.
     * 
     * @see BeanPopulatorBaseSpi#initDetailedBeanPopulatable(DetailedBeanPopulatable)
     */
    public BeanPopulatorSpi initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable);
    
    /**
     * Overrides for co-variant return type.
     * 
     * @see BeanPopulatorBaseSpi#initBeanSourceHandler(BeanSourceHandler)
     */
    public BeanPopulatorSpi initBeanSourceHandler(BeanSourceHandler beanSourceHandler);
    
    /**
     * Overrides for co-variant return type.
     * 
     * @see BeanPopulatorBaseSpi#initReaderMethodFinder(BeanMethodFinder)
     */
    public BeanPopulatorSpi initReaderMethodFinder(BeanMethodFinder readerMethodFinder);
    
    /**
     * Overrides for co-variant return type.
     * 
     * @see BeanPopulatorBaseSpi#initSetterMethodCollector(BeanMethodCollector)
     */
    public BeanPopulatorSpi initSetterMethodCollector(BeanMethodCollector setterMethodCollector);
    
    /**
     * Overrides for co-variant return type.
     * 
     * @see BeanPopulatorBaseSpi#initDebug(boolean)
     */
    public BeanPopulatorSpi initDebug(boolean debug);
    
    /**
     * Overrides for co-variant return type.
     * 
     * @see BeanPopulatorBaseSpi#initBeanPopulationExceptionHandler(BeanPopulationExceptionHandler)
     */
    public BeanPopulatorSpi initBeanPopulationExceptionHandler(BeanPopulationExceptionHandler beanPopulationExceptionHandler);
    
    /**
     * Overrides for co-variant return type.
     * 
     * @see BeanPopulatorBaseSpi#initBeanPopulatorBaseConfig(BeanPopulatorBaseConfig)
     */
    public BeanPopulatorSpi initBeanPopulatorBaseConfig(BeanPopulatorBaseConfig baseConfig);
}
