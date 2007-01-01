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
import net.sf.beanlib.spi.replicator.ArrayReplicatable;
import net.sf.beanlib.spi.replicator.BeanReplicatable;
import net.sf.beanlib.spi.replicator.BlobReplicatable;
import net.sf.beanlib.spi.replicator.CollectionReplicatable;
import net.sf.beanlib.spi.replicator.ImmutableReplicatable;
import net.sf.beanlib.spi.replicator.MapReplicatable;

/**
 * Bean Transformable SPI.
 * 
 * @author Joe D. Velopar
 */
public interface BeanTransformableSpi extends Transformable {
	public BeanTransformableSpi initBeanPopulatable(BeanPopulatable beanPopulatable);
	public BeanTransformableSpi initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable);
	public BeanTransformableSpi initBeanSourceHandler(BeanSourceHandler beanSourceHandler);
	public BeanTransformableSpi initReaderMethodFinder(BeanMethodFinder readerMethodFinder);
	public BeanTransformableSpi initSetterMethodCollector(BeanMethodCollector setterMethodCollector);
	public BeanTransformableSpi initCustomTransformer(CustomBeanTransformable customTransformer);
	public BeanTransformableSpi initDebug(boolean debug);
	public void reset();
    
    public BeanPopulatable getBeanPopulatable();
    public DetailedBeanPopulatable getDetailedBeanPopulatable();
    public BeanSourceHandler getBeanSourceHandler();
    public BeanMethodFinder getReaderMethodFinder();
    public BeanMethodCollector getSetterMethodCollector();
    public CustomBeanTransformable getCustomTransformer();
    public boolean isDebug();
    
    public <K,V> Map<K,V> getClonedMap();
    public BeanTransformableSpi initImmutableReplicatable(ImmutableReplicatable.Factory immutableReplicatableFactory);
    public BeanTransformableSpi initCollectionReplicatable(CollectionReplicatable.Factory collectionReplicatableFactory);
    public BeanTransformableSpi initMapReplicatable(MapReplicatable.Factory mapReplicatableFactory);
    public BeanTransformableSpi initArrayReplicatable(ArrayReplicatable.Factory arrayReplicatableFactory);
    public BeanTransformableSpi initBlobReplicatable(BlobReplicatable.Factory blobReplicatableFactory);
    public BeanTransformableSpi initObjectReplicatable(BeanReplicatable.Factory objectReplicatableFactory);
    
    public ImmutableReplicatable getImmutableReplicatable();
    public CollectionReplicatable getCollectionReplicatable();
    public MapReplicatable getMapReplicatable();
    public ArrayReplicatable getArrayReplicatable();
    public BlobReplicatable getBlobReplicatable();
    public BeanReplicatable getObjectReplicatable();
}
