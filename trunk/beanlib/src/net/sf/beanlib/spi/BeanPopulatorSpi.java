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
        public BeanPopulatorSpi newBeanPopulator(Object from, Object to);
    }
    
    public BeanPopulatorSpi initTransformer(Transformable transformer);

    public Transformable getTransformer();
    
    // Overrides for co-variant return types.
    public BeanPopulatorSpi initBeanPopulatable(BeanPopulatable beanPopulatable);
    public BeanPopulatorSpi initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable);
    public BeanPopulatorSpi initBeanSourceHandler(BeanSourceHandler beanSourceHandler);
    public BeanPopulatorSpi initReaderMethodFinder(BeanMethodFinder readerMethodFinder);
    public BeanPopulatorSpi initSetterMethodCollector(BeanMethodCollector setterMethodCollector);
    public BeanPopulatorSpi initDebug(boolean debug);
    public BeanPopulatorSpi initBeanPopulationExceptionHandler(BeanPopulationExceptionHandler beanPopulationExceptionHandler);
    public BeanPopulatorSpi initBeanPopulatorBaseConfig(BeanPopulatorBaseConfig baseConfig);
    
    public <T> T populate(); 
}
