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

import java.util.Map;

import net.sf.beanlib.api.BeanMethodCollector;
import net.sf.beanlib.api.BeanMethodFinder;
import net.sf.beanlib.api.BeanPopulatable;
import net.sf.beanlib.api.BeanSourceHandler;
import net.sf.beanlib.api.DetailedBeanPopulatable;
import net.sf.beanlib.api.Transformable;
import net.sf.beanlib.spi.replicator.ArrayReplicatorSpi;
import net.sf.beanlib.spi.replicator.BeanReplicatorSpi;
import net.sf.beanlib.spi.replicator.BlobReplicatorSpi;
import net.sf.beanlib.spi.replicator.CollectionReplicatorSpi;
import net.sf.beanlib.spi.replicator.ImmutableReplicatorSpi;
import net.sf.beanlib.spi.replicator.MapReplicatorSpi;

/**
 * Bean Transformable SPI.
 * 
 * @author Joe D. Velopar
 */
public interface BeanTransformerSpi extends Transformable {
	public BeanTransformerSpi initBeanPopulatable(BeanPopulatable beanPopulatable);
	public BeanTransformerSpi initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable);
	public BeanTransformerSpi initBeanSourceHandler(BeanSourceHandler beanSourceHandler);
	public BeanTransformerSpi initReaderMethodFinder(BeanMethodFinder readerMethodFinder);
	public BeanTransformerSpi initSetterMethodCollector(BeanMethodCollector setterMethodCollector);
	public BeanTransformerSpi initCustomTransformer(CustomBeanTransformerSpi customTransformer);
	public BeanTransformerSpi initDebug(boolean debug);
	public void reset();
    
    public BeanPopulatable getBeanPopulatable();
    public DetailedBeanPopulatable getDetailedBeanPopulatable();
    public BeanSourceHandler getBeanSourceHandler();
    public BeanMethodFinder getReaderMethodFinder();
    public BeanMethodCollector getSetterMethodCollector();
    public CustomBeanTransformerSpi getCustomTransformer();
    public boolean isDebug();
    
    public <K,V> Map<K,V> getClonedMap();
    public BeanTransformerSpi initImmutableReplicatable(ImmutableReplicatorSpi.Factory immutableReplicatableFactory);
    public BeanTransformerSpi initCollectionReplicatable(CollectionReplicatorSpi.Factory collectionReplicatableFactory);
    public BeanTransformerSpi initMapReplicatable(MapReplicatorSpi.Factory mapReplicatableFactory);
    public BeanTransformerSpi initArrayReplicatable(ArrayReplicatorSpi.Factory arrayReplicatableFactory);
    public BeanTransformerSpi initBlobReplicatable(BlobReplicatorSpi.Factory blobReplicatableFactory);
    public BeanTransformerSpi initObjectReplicatable(BeanReplicatorSpi.Factory objectReplicatableFactory);
    
    public ImmutableReplicatorSpi getImmutableReplicatable();
    public CollectionReplicatorSpi getCollectionReplicatable();
    public MapReplicatorSpi getMapReplicatable();
    public ArrayReplicatorSpi getArrayReplicatable();
    public BlobReplicatorSpi getBlobReplicatable();
    public BeanReplicatorSpi getObjectReplicatable();
}
