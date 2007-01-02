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

import net.sf.beanlib.api.BeanMethodCollector;
import net.sf.beanlib.api.BeanMethodFinder;
import net.sf.beanlib.api.BeanPopulatable;
import net.sf.beanlib.api.BeanSourceHandler;
import net.sf.beanlib.api.DetailedBeanPopulatable;

/**
 * The base of the Bean Populator SPI.
 * 
 * @author Joe D. Velopar
 */
public interface BeanPopulatorBaseSpi 
{
	public BeanPopulatorBaseSpi initBeanPopulatable(BeanPopulatable beanPopulatable);
	public BeanPopulatorBaseSpi initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable);
	public BeanPopulatorBaseSpi initBeanSourceHandler(BeanSourceHandler beanSourceHandler);
	public BeanPopulatorBaseSpi initReaderMethodFinder(BeanMethodFinder readerMethodFinder);
	public BeanPopulatorBaseSpi initSetterMethodCollector(BeanMethodCollector setterMethodCollector);
	public BeanPopulatorBaseSpi initDebug(boolean debug);
    
    public BeanPopulatable getBeanPopulatable();
    public DetailedBeanPopulatable getDetailedBeanPopulatable();
    public BeanSourceHandler getBeanSourceHandler();
    public BeanMethodFinder getReaderMethodFinder();
    public BeanMethodCollector getSetterMethodCollector();
    public boolean isDebug();
}
