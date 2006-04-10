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
package net.sf.beanlib.hibernate;

import java.sql.Blob;
import java.util.Map;

import net.sf.beanlib.api.BeanMethodCollector;
import net.sf.beanlib.api.BeanMethodFinder;
import net.sf.beanlib.api.BeanPopulatable;
import net.sf.beanlib.api.BeanSourceHandler;
import net.sf.beanlib.api.DetailedBeanPopulatable;

/**
 * Hibernate Bean Transformable Serivce Provider Interface.
 * 
 * @author Joe D. Velopar
 */
public interface HibernateBeanTransformableSpi extends HibernateBeanTransformable {
	public BeanPopulatable getBeanPopulatable();
	public DetailedBeanPopulatable getDetailedBeanPopulatable();
	public BeanSourceHandler getBeanSourceHandler();
	public BeanMethodFinder getReaderMethodFinder();
	public BeanMethodCollector getSetterMethodCollector();
	public CustomHibernateBeanTransformable getCustomTransformer();
	public boolean isDebug();
	
	public Map getClonedMap();
	public void hibernateInitialize(Object obj);
	public Blob hibernateCreateBlob(byte[] byteArray);
}
