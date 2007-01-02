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
import net.sf.beanlib.ProtectedSetterMethodCollector;
import net.sf.beanlib.api.BeanMethodCollector;
import net.sf.beanlib.api.BeanMethodFinder;
import net.sf.beanlib.api.BeanPopulatable;
import net.sf.beanlib.api.BeanSourceHandler;
import net.sf.beanlib.api.DetailedBeanPopulatable;
import net.sf.beanlib.provider.replicator.ArrayReplicator;
import net.sf.beanlib.provider.replicator.BeanReplicator;
import net.sf.beanlib.provider.replicator.CollectionReplicator;
import net.sf.beanlib.provider.replicator.ImmutableReplicator;
import net.sf.beanlib.provider.replicator.MapReplicator;
import net.sf.beanlib.provider.replicator.ReplicatorTemplate;
import net.sf.beanlib.provider.replicator.UnsupportedBlobReplicator;
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
    
    private DetailedBeanPopulatable detailedBeanPopulatable;
    private BeanPopulatable beanPopulatable; 
    private BeanSourceHandler beanSourceHandler;
    private BeanMethodFinder readerMethodFinder;
    private BeanMethodCollector setterMethodCollector = ProtectedSetterMethodCollector.inst;
    
    /** Custom Transformer. */
    private CustomBeanTransformerSpi customTransformer = CustomBeanTransformerSpi.NO_OP;

    private boolean debug;
    
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

    public final BeanPopulatable getBeanPopulatable() {
        return beanPopulatable;
    }
    
    public final BeanTransformerSpi initCustomTransformer(CustomBeanTransformerSpi customTransformer) {
        this.customTransformer = customTransformer;
        return this;
    }
    
    public final BeanTransformerSpi initBeanPopulatable(BeanPopulatable beanPopulatable) {
        this.beanPopulatable = beanPopulatable;
        return this;
    }
    public final BeanSourceHandler getBeanSourceHandler() {
        return beanSourceHandler;
    }
    public final BeanTransformerSpi initBeanSourceHandler(BeanSourceHandler beanSourceHandler) {
        this.beanSourceHandler = beanSourceHandler;
        return this;
    }
    public final boolean isDebug() {
        return debug;
    }
    public final BeanTransformerSpi initDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
    public final DetailedBeanPopulatable getDetailedBeanPopulatable() {
        return detailedBeanPopulatable;
    }
    public final BeanTransformerSpi initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable) 
    {
        this.detailedBeanPopulatable = detailedBeanPopulatable;
        return this;
    }
    public final BeanMethodFinder getReaderMethodFinder() {
        return readerMethodFinder;
    }
    public final BeanTransformerSpi initReaderMethodFinder(BeanMethodFinder readerMethodFinder) {
        this.readerMethodFinder = readerMethodFinder;
        return this;
    }
    public final BeanMethodCollector getSetterMethodCollector() {
        return setterMethodCollector;
    }
    public final BeanTransformerSpi initSetterMethodCollector(BeanMethodCollector setterMethodCollector) {
        this.setterMethodCollector = setterMethodCollector;
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
    public CustomBeanTransformerSpi getCustomTransformer() {
        return customTransformer;
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
}
