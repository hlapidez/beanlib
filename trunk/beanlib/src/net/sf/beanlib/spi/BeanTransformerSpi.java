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

import net.sf.beanlib.spi.replicator.ArrayReplicatorSpi;
import net.sf.beanlib.spi.replicator.BeanReplicatorSpi;
import net.sf.beanlib.spi.replicator.BlobReplicatorSpi;
import net.sf.beanlib.spi.replicator.CollectionReplicatorSpi;
import net.sf.beanlib.spi.replicator.DateReplicatorSpi;
import net.sf.beanlib.spi.replicator.ImmutableReplicatorSpi;
import net.sf.beanlib.spi.replicator.MapReplicatorSpi;

/**
 * Bean Transformer SPI.
 * 
 * @author Joe D. Velopar
 */
public interface BeanTransformerSpi extends Transformable, BeanPopulatorBaseSpi 
{
    /**
     * Bean Transformer Factory SPI.
     * 
     * @author Joe D. Velopar
     */
    public static interface Factory {
        public BeanTransformerSpi newBeanTransformer(BeanPopulatorSpi.Factory beanPopulatorFactory);
    }
    
    // Overrides here for co-variant return type.
    /** Don't invoke this method, except from within the BeanPopulatorSpi implementation class. */
	public BeanTransformerSpi initBeanPopulatable(BeanPopulatable beanPopulatable);
    /** Don't invoke this method, except from within the BeanPopulatorSpi implementation class. */
	public BeanTransformerSpi initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable);
    /** Don't invoke this method, except from within the BeanPopulatorSpi implementation class. */
	public BeanTransformerSpi initBeanSourceHandler(BeanSourceHandler beanSourceHandler);
    /** Don't invoke this method, except from within the BeanPopulatorSpi implementation class. */
	public BeanTransformerSpi initReaderMethodFinder(BeanMethodFinder readerMethodFinder);
    /** Don't invoke this method, except from within the BeanPopulatorSpi implementation class. */
	public BeanTransformerSpi initSetterMethodCollector(BeanMethodCollector setterMethodCollector);
    /** Don't invoke this method, except from within the BeanPopulatorSpi implementation class. */
    public BeanTransformerSpi initBeanPopulationExceptionHandler(BeanPopulationExceptionHandler beanPopulationExceptionHandler);
    /** Don't invoke this method, except from within the BeanPopulatorSpi implementation class. */
	public BeanTransformerSpi initDebug(boolean debug);
    /** Don't invoke this method, except from within the BeanPopulatorSpi implementation class. */
    public BeanTransformerSpi initBeanPopulatorBaseConfig(BeanPopulatorBaseConfig baseConfig);

    /** Don't invoke this method, except from within the replicator implementation class. */
    public BeanPopulatorBaseConfig getBeanPopulatorBaseConfig();
    
    public BeanTransformerSpi initCustomTransformer(CustomBeanTransformerSpi.Factory customTransformerFactory);
    
    public void reset();
    
    public <K,V> Map<K,V> getClonedMap();
    public BeanTransformerSpi initImmutableReplicatable(ImmutableReplicatorSpi.Factory immutableReplicatableFactory);
    public BeanTransformerSpi initCollectionReplicatable(CollectionReplicatorSpi.Factory collectionReplicatableFactory);
    public BeanTransformerSpi initMapReplicatable(MapReplicatorSpi.Factory mapReplicatableFactory);
    public BeanTransformerSpi initArrayReplicatable(ArrayReplicatorSpi.Factory arrayReplicatableFactory);
    public BeanTransformerSpi initBlobReplicatable(BlobReplicatorSpi.Factory blobReplicatableFactory);
    public BeanTransformerSpi initDateReplicatable(DateReplicatorSpi.Factory dateReplicatableFactory);
    public BeanTransformerSpi initBeanReplicatable(BeanReplicatorSpi.Factory beanReplicatableFactory);
    
    public ImmutableReplicatorSpi getImmutableReplicatable();
    public CollectionReplicatorSpi getCollectionReplicatable();
    public MapReplicatorSpi getMapReplicatable();
    public ArrayReplicatorSpi getArrayReplicatable();
    public BlobReplicatorSpi getBlobReplicatable();
    public DateReplicatorSpi getDateReplicatable();
    public BeanReplicatorSpi getBeanReplicatable();
    
    public BeanPopulatorSpi.Factory getBeanPopulatorSpiFactory();
}
