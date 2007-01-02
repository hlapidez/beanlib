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
    private static final Factory factory = new Factory();
    
    private static class Factory implements BeanTransformerSpi.Factory {
        private Factory() {}
        
        public BeanTransformer newBeanTransformer(BeanPopulatorSpi.Factory beanPopulatorFactory) 
        {
            return new BeanTransformer(beanPopulatorFactory);
        }
    }
    
    public static BeanTransformer newBeanTransformer(BeanPopulatorSpi.Factory beanPopulatorFactory)
    {
        return factory.newBeanTransformer(beanPopulatorFactory);
    }
    
    private final BeanPopulatorSpi.Factory beanPopulatorFactory;
    
    private BeanTransformer(BeanPopulatorSpi.Factory beanPopulatorFactory) {
        this.beanPopulatorFactory = beanPopulatorFactory;
    }
    
    public BeanPopulatorSpi.Factory getBeanPopulatorSpiFactory() {
        return beanPopulatorFactory;
    }
    
    // Contains those objects that have been replicated.
    private Map<Object,Object> clonedMap = new IdentityHashMap<Object,Object>();
    
    private BeanPopulatorBaseConfig baseConfig = new BeanPopulatorBaseConfig();
    
    /** Custom Transformer. */
    private CustomBeanTransformerSpi customTransformer = CustomBeanTransformerSpi.NO_OP;
    
    private ImmutableReplicatorSpi immutableReplicatable = ImmutableReplicator.newReplicatable(this);
    private CollectionReplicatorSpi collectionReplicatable = CollectionReplicator.newReplicatable(this);
    private MapReplicatorSpi mapReplicatable = MapReplicator.newReplicatable(this);
    private ArrayReplicatorSpi arrayReplicatable = ArrayReplicator.newReplicatable(this);
    private BlobReplicatorSpi blobReplicatable = UnsupportedBlobReplicator.newReplicatable(this);
    private BeanReplicatorSpi beanReplicatable = BeanReplicator.newReplicatable(this);
    
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
    
    public final BeanTransformer initCustomTransformer(CustomBeanTransformerSpi customTransformer) {
        this.customTransformer = customTransformer;
        return this;
    }
    
    public final BeanTransformer initBeanPopulatable(BeanPopulatable beanPopulatable) {
        baseConfig.setBeanPopulatable(beanPopulatable);
        return this;
    }

    public final BeanTransformer initBeanSourceHandler(BeanSourceHandler beanSourceHandler) {
        baseConfig.setBeanSourceHandler(beanSourceHandler);
        return this;
    }

    public final BeanTransformer initDebug(boolean debug) {
        baseConfig.setDebug(debug);
        return this;
    }

    public final BeanTransformer initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable) 
    {
        baseConfig.setDetailedBeanPopulatable(detailedBeanPopulatable);
        return this;
    }

    public final BeanTransformer initReaderMethodFinder(BeanMethodFinder readerMethodFinder) {
        baseConfig.setReaderMethodFinder(readerMethodFinder);
        return this;
    }

    public final BeanTransformer initSetterMethodCollector(BeanMethodCollector setterMethodCollector) {
        baseConfig.setSetterMethodCollector(setterMethodCollector);
        return this;
    }
    
    public BeanTransformer initCollectionReplicatable(CollectionReplicatorSpi.Factory factory) {
        this.collectionReplicatable = factory.newReplicatable(this);
        return this;
    }
    
    public CollectionReplicatorSpi getCollectionReplicatable() {
        return collectionReplicatable;
    }
    
    public BeanTransformer initMapReplicatable(MapReplicatorSpi.Factory factory) {
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
    
    public BeanTransformer initImmutableReplicatable(ImmutableReplicatorSpi.Factory immutableReplicatableFactory) 
    {
        this.immutableReplicatable = immutableReplicatableFactory.newReplicatable(this);
        return this;
    }

    public ImmutableReplicatorSpi getImmutableReplicatable() {
        return immutableReplicatable;
    }

    public BeanTransformer initArrayReplicatable(ArrayReplicatorSpi.Factory arrayReplicatableFactory) {
        this.arrayReplicatable = arrayReplicatableFactory.newReplicatable(this);
        return this;
    }

    public ArrayReplicatorSpi getArrayReplicatable() {
        return arrayReplicatable;
    }

    public BeanTransformer initBlobReplicatable(BlobReplicatorSpi.Factory blobReplicatableFactory) {
        this.blobReplicatable = blobReplicatableFactory.newReplicatable(this);
        return this;
    }

    public BlobReplicatorSpi getBlobReplicatable() {
        return blobReplicatable;
    }

    public BeanTransformer initBeanReplicatable(BeanReplicatorSpi.Factory objectReplicatableFactory) {
        this.beanReplicatable = objectReplicatableFactory.newReplicatable(this);
        return this;
    }

    public BeanReplicatorSpi getBeanReplicatable() {
        return beanReplicatable;
    }

    public BeanTransformer initBeanPopulationExceptionHandler(BeanPopulationExceptionHandler beanPopulationExceptionHandler) {
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
