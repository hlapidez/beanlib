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
import net.sf.beanlib.spi.BeanTransformableSpi;
import net.sf.beanlib.spi.CustomBeanTransformable;
import net.sf.beanlib.spi.replicator.ArrayReplicatable;
import net.sf.beanlib.spi.replicator.BeanReplicatable;
import net.sf.beanlib.spi.replicator.BlobReplicatable;
import net.sf.beanlib.spi.replicator.CollectionReplicatable;
import net.sf.beanlib.spi.replicator.ImmutableReplicatable;
import net.sf.beanlib.spi.replicator.MapReplicatable;

/**
 * Bean Transformer.
 * 
 * @author Joe D. Velopar
 */
public class BeanTransformer extends ReplicatorTemplate implements BeanTransformableSpi
{
    // Contains those objects that have been replicated.
    private Map<Object,Object> clonedMap = new IdentityHashMap<Object,Object>();
    
    private DetailedBeanPopulatable detailedBeanPopulatable;
    private BeanPopulatable beanPopulatable; 
    private BeanSourceHandler beanSourceHandler;
    private BeanMethodFinder readerMethodFinder;
    private BeanMethodCollector setterMethodCollector = ProtectedSetterMethodCollector.inst;
    
    /** Custom Transformer. */
    private CustomBeanTransformable customTransformer = CustomBeanTransformable.NO_OP;

    private boolean debug;
    
    private ImmutableReplicatable immutableReplicatable = ImmutableReplicator.factory.newReplicatable(this);
    private CollectionReplicatable collectionReplicatable = CollectionReplicator.factory.newReplicatable(this);
    private MapReplicatable mapReplicatable = MapReplicator.factory.newReplicatable(this);
    private ArrayReplicatable arrayReplicatable = ArrayReplicator.factory.newReplicatable(this);
    private BlobReplicatable blobReplicatable = UnsupportedBlobReplicator.factory.newReplicatable(this);
    private BeanReplicatable objectReplicatable = BeanReplicator.factory.newReplicatable(this);
    
    public BeanTransformer() {
    }
    
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
    
    public final BeanTransformableSpi initCustomTransformer(CustomBeanTransformable customTransformer) {
        this.customTransformer = customTransformer;
        return this;
    }
    
    public final BeanTransformableSpi initBeanPopulatable(BeanPopulatable beanPopulatable) {
        this.beanPopulatable = beanPopulatable;
        return this;
    }
    public final BeanSourceHandler getBeanSourceHandler() {
        return beanSourceHandler;
    }
    public final BeanTransformableSpi initBeanSourceHandler(BeanSourceHandler beanSourceHandler) {
        this.beanSourceHandler = beanSourceHandler;
        return this;
    }
    public final boolean isDebug() {
        return debug;
    }
    public final BeanTransformableSpi initDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
    public final DetailedBeanPopulatable getDetailedBeanPopulatable() {
        return detailedBeanPopulatable;
    }
    public final BeanTransformableSpi initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable) 
    {
        this.detailedBeanPopulatable = detailedBeanPopulatable;
        return this;
    }
    public final BeanMethodFinder getReaderMethodFinder() {
        return readerMethodFinder;
    }
    public final BeanTransformableSpi initReaderMethodFinder(BeanMethodFinder readerMethodFinder) {
        this.readerMethodFinder = readerMethodFinder;
        return this;
    }
    public final BeanMethodCollector getSetterMethodCollector() {
        return setterMethodCollector;
    }
    public final BeanTransformableSpi initSetterMethodCollector(BeanMethodCollector setterMethodCollector) {
        this.setterMethodCollector = setterMethodCollector;
        return this;
    }
    
    public BeanTransformableSpi initCollectionReplicatable(CollectionReplicatable.Factory factory) {
        this.collectionReplicatable = factory.newReplicatable(this);
        return this;
    }
    
    public CollectionReplicatable getCollectionReplicatable() {
        return collectionReplicatable;
    }
    
    public BeanTransformableSpi initMapReplicatable(MapReplicatable.Factory factory) {
        this.mapReplicatable = factory.newReplicatable(this);
        return this;
    }
    
    public MapReplicatable getMapReplicatable() {
        return mapReplicatable;
    }
    
    @SuppressWarnings("unchecked")
    public <K,V> Map<K,V> getClonedMap() {
        return (Map<K,V>)clonedMap;
    }
    public CustomBeanTransformable getCustomTransformer() {
        return customTransformer;
    }

    public BeanTransformableSpi initImmutableReplicatable(ImmutableReplicatable.Factory immutableReplicatableFactory) 
    {
        this.immutableReplicatable = immutableReplicatableFactory.newReplicatable(this);
        return this;
    }

    public ImmutableReplicatable getImmutableReplicatable() {
        return this.immutableReplicatable;
    }

    public BeanTransformableSpi initArrayReplicatable(ArrayReplicatable.Factory arrayReplicatableFactory) {
        this.arrayReplicatable = arrayReplicatableFactory.newReplicatable(this);
        return this;
    }

    public ArrayReplicatable getArrayReplicatable() {
        return this.arrayReplicatable;
    }

    public BeanTransformableSpi initBlobReplicatable(BlobReplicatable.Factory blobReplicatableFactory) {
        this.blobReplicatable = blobReplicatableFactory.newReplicatable(this);
        return this;
    }

    public BlobReplicatable getBlobReplicatable() {
        return blobReplicatable;
    }

    public BeanTransformableSpi initObjectReplicatable(BeanReplicatable.Factory objectReplicatableFactory) {
        this.objectReplicatable = objectReplicatableFactory.newReplicatable(this);
        return this;
    }

    public BeanReplicatable getObjectReplicatable() {
        return objectReplicatable;
    }
}
