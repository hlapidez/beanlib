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

import net.sf.beanlib.provider.collector.PublicSetterMethodCollector;
import net.sf.beanlib.provider.finder.PublicReaderMethodFinder;

/**
 * Base configuration for a Bean Populator.
 * 
 * @author Joe D. Velopar
 */
public class BeanPopulatorBaseConfig implements Cloneable
{
    // BeanPopulatorBaseSpi configurations
    private BeanMethodCollector setterMethodCollector = PublicSetterMethodCollector.inst;
    private BeanMethodFinder readerMethodFinder = PublicReaderMethodFinder.inst;
    private BeanSourceHandler beanSourceHandler;
    private BeanPopulatable beanPopulatable;
    private DetailedBeanPopulatable detailedBeanPopulatable = DetailedBeanPopulatable.JAVABEAN_POPULATE;
    private BeanPopulationExceptionHandler beanPopulationExceptionHandler = BeanPopulationExceptionHandler.ABORT;
    private boolean debug;
    
    public BeanPopulatable getBeanPopulatable() {
        return beanPopulatable;
    }
    public void setBeanPopulatable(BeanPopulatable beanPopulatable) {
        this.beanPopulatable = beanPopulatable;
    }
    public BeanPopulationExceptionHandler getBeanPopulationExceptionHandler() {
        return beanPopulationExceptionHandler;
    }
    public void setBeanPopulationExceptionHandler(
            BeanPopulationExceptionHandler beanPopulationExceptionHandler) {
        this.beanPopulationExceptionHandler = beanPopulationExceptionHandler;
    }
    public BeanSourceHandler getBeanSourceHandler() {
        return beanSourceHandler;
    }
    public void setBeanSourceHandler(BeanSourceHandler beanSourceHandler) {
        this.beanSourceHandler = beanSourceHandler;
    }
    public boolean isDebug() {
        return debug;
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    public DetailedBeanPopulatable getDetailedBeanPopulatable() {
        return detailedBeanPopulatable;
    }
    public void setDetailedBeanPopulatable(
            DetailedBeanPopulatable detailedBeanPopulatable) {
        this.detailedBeanPopulatable = detailedBeanPopulatable;
    }
    public BeanMethodFinder getReaderMethodFinder() {
        return readerMethodFinder;
    }
    public void setReaderMethodFinder(BeanMethodFinder readerMethodFinder) {
        this.readerMethodFinder = readerMethodFinder;
    }
    public BeanMethodCollector getSetterMethodCollector() {
        return setterMethodCollector;
    }
    public void setSetterMethodCollector(BeanMethodCollector setterMethodCollector) {
        this.setterMethodCollector = setterMethodCollector;
    }
    
    @Override
    public BeanPopulatorBaseConfig clone() {
        try {
            return (BeanPopulatorBaseConfig)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(this.getClass() + " must implement " + Cloneable.class);
        }
    }
}