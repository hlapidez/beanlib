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
package net.sf.beanlib.hibernate3;

import net.jcip.annotations.NotThreadSafe;
import net.sf.beanlib.hibernate.HibernateBeanReplicator;
import net.sf.beanlib.hibernate.HibernatePropertyFilter;
import net.sf.beanlib.spi.BeanPopulatorBaseSpi;
import net.sf.beanlib.spi.CustomBeanTransformerSpi;

/**
 * Hibernate 3 Bean Replicator.
 * <p> 
 * This class can be used to conveniently replicate Hibernate (v3.x) objects 
 * that follow the JavaBean getter/setter convention.
 *   
 * The replication is typically recursive in that 
 * the whole object graph of the input object is replicated into an equivalent output object graph, 
 * resolving circular references, and eager fetching proxied instances as necessary.
 * 
 * However, the exact behavior of the replication process including<ul>
 * <li>to what extent the input object graph should be traversed and/or replicated; and </li>
 * <li>whether proxied instances should be eagerly fetched or not</li>
 * </ul> 
 * can be controlled and/or supplemented by the client code via various options:
 * <p>
 * <ol>
 * <li>All the configurable options of {@link BeanPopulatorBaseSpi} are available, as
 * the replication of JavaBean properties inevitably involves bean population.</li>
 * <p>
 * <li>The set of entity bean classes for matching properties that will be replicated;</li>
 * <p>
 * <li>The set of collection and map properties that will be replicated;</li>
 * <p>
 * <li>A {@link net.sf.beanlib.spi.PropertyFilter vetoer} used to veto the propagation of a property</li>
 * <p>
 * <li>For anything else that the existing implementation fails to transform, client can provide
 * one or multiple custom transformer factories via  
 * {@link #initCustomTransformerFactory(net.sf.beanlib.spi.CustomBeanTransformerSpi.Factory...)}.
 * </li>
 * </ol>
 * 
 * @see CustomBeanTransformerSpi
 * @see HibernateBeanReplicator
 * 
 * @author Joe D. Velopar
 */
@NotThreadSafe
public class Hibernate3BeanReplicator extends HibernateBeanReplicator
{
	public Hibernate3BeanReplicator() {
		super(new Hibernate3BeanTransformer()
		        .initPropertyFilter(new HibernatePropertyFilter()));
	}
}
