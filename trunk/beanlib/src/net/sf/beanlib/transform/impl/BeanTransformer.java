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
package net.sf.beanlib.transform.impl;

import java.util.IdentityHashMap;
import java.util.Map;

import net.sf.beanlib.BeanlibException;
import net.sf.beanlib.ProtectedSetterMethodCollector;
import net.sf.beanlib.api.BeanMethodCollector;
import net.sf.beanlib.api.BeanMethodFinder;
import net.sf.beanlib.api.BeanPopulatable;
import net.sf.beanlib.api.BeanSourceHandler;
import net.sf.beanlib.api.DetailedBeanPopulatable;
import net.sf.beanlib.transform.spi.ArrayReplicatable;
import net.sf.beanlib.transform.spi.BeanReplicatable;
import net.sf.beanlib.transform.spi.BeanTransformableSpi;
import net.sf.beanlib.transform.spi.BlobReplicatable;
import net.sf.beanlib.transform.spi.CollectionReplicatable;
import net.sf.beanlib.transform.spi.CustomBeanTransformable;
import net.sf.beanlib.transform.spi.ImmutableReplicatable;
import net.sf.beanlib.transform.spi.MapReplicatable;

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
    
    private ImmutableReplicatable immutableReplicatable = new ImmutableReplicator().initBeanTransformableSpi(this);
    private CollectionReplicatable collectionReplicatable = new CollectionReplicator().initBeanTransformableSpi(this);
    private MapReplicatable mapReplicatable = new MapReplicator().initBeanTransformableSpi(this);
    private ArrayReplicatable arrayReplicatable = new ArrayReplicator().initBeanTransformableSpi(this);
    private BlobReplicatable blobReplicatable = new UnsupportedBlobReplicator();
    private BeanReplicatable objectReplicatable = new BeanReplicator();
    
    public BeanTransformer() {
        super.setBeanTransformableSpi(this);
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
    
    public BeanTransformableSpi initCollectionReplicatable(CollectionReplicatable collectionReplicatable) {
        this.collectionReplicatable = collectionReplicatable.initBeanTransformableSpi(this);
        return this;
    }
    
    public CollectionReplicatable getCollectionReplicatable() {
        return collectionReplicatable;
    }
    
    public BeanTransformableSpi initMapReplicatable(MapReplicatable mapReplicatable) {
        this.mapReplicatable = mapReplicatable.initBeanTransformableSpi(this);
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

    public BeanTransformableSpi initImmutableReplicatable(ImmutableReplicatable immutableReplicatable) 
    {
        this.immutableReplicatable = immutableReplicatable;
        return this;
    }

    public ImmutableReplicatable getImmutableReplicatable() {
        return this.immutableReplicatable;
    }

    public BeanTransformableSpi initArrayReplicatable(ArrayReplicatable arrayReplicatable) {
        this.arrayReplicatable = arrayReplicatable;
        return this;
    }

    public ArrayReplicatable getArrayReplicatable() {
        return this.arrayReplicatable;
    }

    public BeanTransformableSpi initBlobReplicatable(BlobReplicatable blobReplicatable) {
        this.blobReplicatable = blobReplicatable;
        return this;
    }

    public BlobReplicatable getBlobReplicatable() {
        return blobReplicatable;
    }

    public BeanTransformableSpi initObjectReplicatable(BeanReplicatable objectReplicatable) {
        this.objectReplicatable = objectReplicatable;
        return this;
    }

    public BeanReplicatable getObjectReplicatable() {
        return objectReplicatable;
    }
}
