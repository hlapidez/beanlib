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

import net.sf.beanlib.spi.BeanMethodCollector;
import net.sf.beanlib.spi.BeanMethodFinder;
import net.sf.beanlib.spi.BeanPopulatable;
import net.sf.beanlib.spi.BeanSourceHandler;
import net.sf.beanlib.spi.DetailedBeanPopulatable;
import net.sf.beanlib.spi.Transformable;

/**
 * Hibernate Bean Transformable.
 * 
 * @author Joe D. Velopar
 */
public interface HibernateBeanTransformable extends Transformable {
	public HibernateBeanTransformable initBeanPopulatable(BeanPopulatable beanPopulatable);
	public HibernateBeanTransformable initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable);
	public HibernateBeanTransformable initBeanSourceHandler(BeanSourceHandler beanSourceHandler);
	public HibernateBeanTransformable initReaderMethodFinder(BeanMethodFinder readerMethodFinder);
	public HibernateBeanTransformable initSetterMethodCollector(BeanMethodCollector setterMethodCollector);
	public HibernateBeanTransformable initCustomTransformer(CustomHibernateBeanTransformable customTransformer);
	public HibernateBeanTransformable initDebug(boolean debug);
	public void reset();
}
