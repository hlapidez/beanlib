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
        /** 
         * Returns a bean transformer, given a bean populator factory.
         */
        public BeanTransformerSpi newBeanTransformer(BeanPopulatorSpi.Factory beanPopulatorFactory);
    }
    
    /** 
     * Initializes with a custom transformer factory.
     */
    public BeanTransformerSpi initCustomTransformerFactory(CustomBeanTransformerSpi.Factory customTransformerFactory);

    // Used to resolve Object Identities and circular references
    public void reset();
    public <K,V> Map<K,V> getClonedMap();
    
    // Configure the replicator factories for some major/common types
    public BeanTransformerSpi initImmutableReplicatableFactory(ImmutableReplicatorSpi.Factory immutableReplicatableFactory);
    public BeanTransformerSpi initCollectionReplicatableFactory(CollectionReplicatorSpi.Factory collectionReplicatableFactory);
    public BeanTransformerSpi initMapReplicatableFactory(MapReplicatorSpi.Factory mapReplicatableFactory);
    public BeanTransformerSpi initArrayReplicatableFactory(ArrayReplicatorSpi.Factory arrayReplicatableFactory);
    public BeanTransformerSpi initBlobReplicatableFactory(BlobReplicatorSpi.Factory blobReplicatableFactory);
    public BeanTransformerSpi initDateReplicatableFactory(DateReplicatorSpi.Factory dateReplicatableFactory);
    public BeanTransformerSpi initBeanReplicatableFactory(BeanReplicatorSpi.Factory beanReplicatableFactory);
    
    public ImmutableReplicatorSpi getImmutableReplicatable();
    public CollectionReplicatorSpi getCollectionReplicatable();
    public MapReplicatorSpi getMapReplicatable();
    public ArrayReplicatorSpi getArrayReplicatable();
    public BlobReplicatorSpi getBlobReplicatable();
    public DateReplicatorSpi getDateReplicatable();
    public BeanReplicatorSpi getBeanReplicatable();
    
    public BeanPopulatorSpi.Factory getBeanPopulatorSpiFactory();
    public BeanPopulatorBaseConfig getBeanPopulatorBaseConfig();
    
    // -------------------------- BeanPopulatorBaseSpi -------------------------- 

    // Overrides here for co-variant return type.
    // Don't invoke this method, except from within the BeanPopulatorSpi implementation class.
    public BeanTransformerSpi initBeanPopulatable(BeanPopulatable beanPopulatable);
    // Overrides here for co-variant return type.
    // Don't invoke this method, except from within the BeanPopulatorSpi implementation class.
    public BeanTransformerSpi initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable);
    // Overrides here for co-variant return type.
    // Don't invoke this method, except from within the BeanPopulatorSpi implementation class.
    public BeanTransformerSpi initBeanSourceHandler(BeanSourceHandler beanSourceHandler);
    // Overrides here for co-variant return type.
    // Don't invoke this method, except from within the BeanPopulatorSpi implementation class.
    public BeanTransformerSpi initReaderMethodFinder(BeanMethodFinder readerMethodFinder);
    // Overrides here for co-variant return type.
    // Don't invoke this method, except from within the BeanPopulatorSpi implementation class.
    public BeanTransformerSpi initSetterMethodCollector(BeanMethodCollector setterMethodCollector);
    // Overrides here for co-variant return type.
    // Don't invoke this method, except from within the BeanPopulatorSpi implementation class.
    public BeanTransformerSpi initBeanPopulationExceptionHandler(BeanPopulationExceptionHandler beanPopulationExceptionHandler);
    // Overrides here for co-variant return type.
    // Don't invoke this method, except from within the BeanPopulatorSpi implementation class.
    public BeanTransformerSpi initDebug(boolean debug);
    // Overrides here for co-variant return type.
    // Don't invoke this method, except from within the BeanPopulatorSpi implementation class.
    public BeanTransformerSpi initBeanPopulatorBaseConfig(BeanPopulatorBaseConfig baseConfig);
}
