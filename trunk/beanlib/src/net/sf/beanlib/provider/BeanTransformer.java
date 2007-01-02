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
package net.sf.beanlib.provider;

import java.util.IdentityHashMap;
import java.util.Map;

import net.sf.beanlib.BeanlibException;
import net.sf.beanlib.api.BeanMethodCollector;
import net.sf.beanlib.api.BeanMethodFinder;
import net.sf.beanlib.api.BeanPopulatable;
import net.sf.beanlib.api.BeanPopulationExceptionHandler;
import net.sf.beanlib.api.BeanSourceHandler;
import net.sf.beanlib.api.DetailedBeanPopulatable;
import net.sf.beanlib.provider.replicator.ArrayReplicator;
import net.sf.beanlib.provider.replicator.BeanReplicator;
import net.sf.beanlib.provider.replicator.CollectionReplicator;
import net.sf.beanlib.provider.replicator.ImmutableReplicator;
import net.sf.beanlib.provider.replicator.MapReplicator;
import net.sf.beanlib.provider.replicator.ReplicatorTemplate;
import net.sf.beanlib.provider.replicator.UnsupportedBlobReplicator;
import net.sf.beanlib.spi.BeanPopulatorBaseConfig;
import net.sf.beanlib.spi.BeanPopulatorSpi;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.CustomBeanTransformerSpi;
import net.sf.beanlib.spi.replicator.ArrayReplicatorSpi;
import net.sf.beanlib.spi.replicator.BeanReplicatorSpi;
import net.sf.beanlib.spi.replicator.BlobReplicatorSpi;
import net.sf.beanlib.spi.replicator.CollectionReplicatorSpi;
import net.sf.beanlib.spi.replicator.ImmutableReplicatorSpi;
import net.sf.beanlib.spi.replicator.MapReplicatorSpi;

/**
 * Bean Transformer.
 * 
 * @author Joe D. Velopar
 */
public class BeanTransformer extends ReplicatorTemplate implements BeanTransformerSpi
{
    public static final Factory factory = new Factory();
    
    public static class Factory implements BeanTransformerSpi.Factory {
        private Factory() {}
        
        public BeanTransformerSpi newBeanTransformer(BeanPopulatorSpi.Factory beanPopulatorFactory) 
        {
            return new BeanTransformer(beanPopulatorFactory);
        }
    }
    
    private final BeanPopulatorSpi.Factory beanPopulatorFactory;
    
    private BeanTransformer(BeanPopulatorSpi.Factory beanPopulatorFactory) {
        this.beanPopulatorFactory = beanPopulatorFactory;
    }
    
    public BeanPopulatorSpi.Factory getBeanPopulatorSpiFactory() {
        return this.beanPopulatorFactory;
    }
    
    // Contains those objects that have been replicated.
    private Map<Object,Object> clonedMap = new IdentityHashMap<Object,Object>();
    
    private BeanPopulatorBaseConfig baseConfig = new BeanPopulatorBaseConfig();
    
    /** Custom Transformer. */
    private CustomBeanTransformerSpi customTransformer = CustomBeanTransformerSpi.NO_OP;
    
    private ImmutableReplicatorSpi immutableReplicatable = ImmutableReplicator.factory.newReplicatable(this);
    private CollectionReplicatorSpi collectionReplicatable = CollectionReplicator.factory.newReplicatable(this);
    private MapReplicatorSpi mapReplicatable = MapReplicator.factory.newReplicatable(this);
    private ArrayReplicatorSpi arrayReplicatable = ArrayReplicator.factory.newReplicatable(this);
    private BlobReplicatorSpi blobReplicatable = UnsupportedBlobReplicator.factory.newReplicatable(this);
    private BeanReplicatorSpi objectReplicatable = BeanReplicator.factory.newReplicatable(this);
    
    public final void reset() {
        clonedMap = new IdentityHashMap<Object,Object>();
    }
    
    public final <T> T transform(Object from, Class<T> toClass) {
        try {
            if (customTransformer.isTransformable(from, toClass, this))
                return customTransformer.transform(from, toClass);
            return replicate(from, toClass);
        } catch (SecurityException e) {
            throw new BeanlibException(e);
        }
    }
    
    public final BeanTransformerSpi initCustomTransformer(CustomBeanTransformerSpi customTransformer) {
        this.customTransformer = customTransformer;
        return this;
    }
    
    public final BeanTransformerSpi initBeanPopulatable(BeanPopulatable beanPopulatable) {
        baseConfig.setBeanPopulatable(beanPopulatable);
        return this;
    }

    public final BeanTransformerSpi initBeanSourceHandler(BeanSourceHandler beanSourceHandler) {
        baseConfig.setBeanSourceHandler(beanSourceHandler);
        return this;
    }

    public final BeanTransformerSpi initDebug(boolean debug) {
        baseConfig.setDebug(debug);
        return this;
    }

    public final BeanTransformerSpi initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable) 
    {
        baseConfig.setDetailedBeanPopulatable(detailedBeanPopulatable);
        return this;
    }

    public final BeanTransformerSpi initReaderMethodFinder(BeanMethodFinder readerMethodFinder) {
        baseConfig.setReaderMethodFinder(readerMethodFinder);
        return this;
    }

    public final BeanTransformerSpi initSetterMethodCollector(BeanMethodCollector setterMethodCollector) {
        baseConfig.setSetterMethodCollector(setterMethodCollector);
        return this;
    }
    
    public BeanTransformerSpi initCollectionReplicatable(CollectionReplicatorSpi.Factory factory) {
        this.collectionReplicatable = factory.newReplicatable(this);
        return this;
    }
    
    public CollectionReplicatorSpi getCollectionReplicatable() {
        return collectionReplicatable;
    }
    
    public BeanTransformerSpi initMapReplicatable(MapReplicatorSpi.Factory factory) {
        this.mapReplicatable = factory.newReplicatable(this);
        return this;
    }
    
    public MapReplicatorSpi getMapReplicatable() {
        return mapReplicatable;
    }
    
    @SuppressWarnings("unchecked")
    public <K,V> Map<K,V> getClonedMap() {
        return (Map<K,V>)clonedMap;
    }
    
    public BeanTransformerSpi initImmutableReplicatable(ImmutableReplicatorSpi.Factory immutableReplicatableFactory) 
    {
        this.immutableReplicatable = immutableReplicatableFactory.newReplicatable(this);
        return this;
    }

    public ImmutableReplicatorSpi getImmutableReplicatable() {
        return this.immutableReplicatable;
    }

    public BeanTransformerSpi initArrayReplicatable(ArrayReplicatorSpi.Factory arrayReplicatableFactory) {
        this.arrayReplicatable = arrayReplicatableFactory.newReplicatable(this);
        return this;
    }

    public ArrayReplicatorSpi getArrayReplicatable() {
        return this.arrayReplicatable;
    }

    public BeanTransformerSpi initBlobReplicatable(BlobReplicatorSpi.Factory blobReplicatableFactory) {
        this.blobReplicatable = blobReplicatableFactory.newReplicatable(this);
        return this;
    }

    public BlobReplicatorSpi getBlobReplicatable() {
        return blobReplicatable;
    }

    public BeanTransformerSpi initObjectReplicatable(BeanReplicatorSpi.Factory objectReplicatableFactory) {
        this.objectReplicatable = objectReplicatableFactory.newReplicatable(this);
        return this;
    }

    public BeanReplicatorSpi getObjectReplicatable() {
        return objectReplicatable;
    }

    public BeanTransformerSpi initBeanPopulationExceptionHandler(BeanPopulationExceptionHandler beanPopulationExceptionHandler) {
        baseConfig.setBeanPopulationExceptionHandler(beanPopulationExceptionHandler);
        return this;
    }

    public BeanTransformerSpi initBeanPopulatorBaseConfig(BeanPopulatorBaseConfig baseConfig) 
    {
        this.baseConfig = baseConfig;
        return this;
    }

    public BeanPopulatorBaseConfig getBeanPopulatorBaseConfig() {
        return baseConfig;
    }
}
