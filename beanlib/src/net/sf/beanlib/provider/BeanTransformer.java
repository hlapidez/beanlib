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

import net.jcip.annotations.NotThreadSafe;
import net.sf.beanlib.BeanlibException;
import net.sf.beanlib.PropertyInfo;
import net.sf.beanlib.provider.replicator.ArrayReplicator;
import net.sf.beanlib.provider.replicator.BeanReplicator;
import net.sf.beanlib.provider.replicator.CollectionReplicator;
import net.sf.beanlib.provider.replicator.DateReplicator;
import net.sf.beanlib.provider.replicator.ImmutableReplicator;
import net.sf.beanlib.provider.replicator.MapReplicator;
import net.sf.beanlib.provider.replicator.ReplicatorTemplate;
import net.sf.beanlib.provider.replicator.UnsupportedBlobReplicator;
import net.sf.beanlib.spi.BeanMethodCollector;
import net.sf.beanlib.spi.BeanMethodFinder;
import net.sf.beanlib.spi.BeanPopulatable;
import net.sf.beanlib.spi.BeanPopulationExceptionHandler;
import net.sf.beanlib.spi.BeanPopulatorBaseConfig;
import net.sf.beanlib.spi.BeanPopulatorSpi;
import net.sf.beanlib.spi.BeanSourceHandler;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.ChainedCustomBeanTransformer;
import net.sf.beanlib.spi.CustomBeanTransformerSpi;
import net.sf.beanlib.spi.DetailedBeanPopulatable;
import net.sf.beanlib.spi.TrivialCustomBeanTransformerFactories;
import net.sf.beanlib.spi.replicator.ArrayReplicatorSpi;
import net.sf.beanlib.spi.replicator.BeanReplicatorSpi;
import net.sf.beanlib.spi.replicator.BlobReplicatorSpi;
import net.sf.beanlib.spi.replicator.CollectionReplicatorSpi;
import net.sf.beanlib.spi.replicator.DateReplicatorSpi;
import net.sf.beanlib.spi.replicator.ImmutableReplicatorSpi;
import net.sf.beanlib.spi.replicator.MapReplicatorSpi;

/**
 * Bean Transformer.
 *
 * @author Joe D. Velopar
 */
@NotThreadSafe
public class BeanTransformer extends ReplicatorTemplate implements BeanTransformerSpi
{
    /** Constructs with the default {@link BeanPopulator#factory}. */
    public BeanTransformer() {
        this(BeanPopulator.factory);
    }
    
    /** 
     * Convenient constructor that both defaults to use {@link BeanPopulator#factory},
     * and allows plugging in one or more custom bean transformer factories that will be chained together.  
     */
    public BeanTransformer(final CustomBeanTransformerSpi.Factory ... customBeanTransformerFactories) {
        this(BeanPopulator.factory);
        
        if (customBeanTransformerFactories != null && customBeanTransformerFactories.length > 0) 
        {
            this.initCustomTransformerFactory(customBeanTransformerFactories.length == 1
                            ? customBeanTransformerFactories[0]
                            : new ChainedCustomBeanTransformer.Factory(customBeanTransformerFactories))
                            ;
        }
    }
    
    private final BeanPopulatorSpi.Factory beanPopulatorFactory;
    
    protected BeanTransformer(BeanPopulatorSpi.Factory beanPopulatorFactory) {
        this.beanPopulatorFactory = beanPopulatorFactory;
    }
    
    public BeanPopulatorSpi.Factory getBeanPopulatorSpiFactory() {
        return beanPopulatorFactory;
    }
    
    // Contains those objects that have been replicated.
    private Map<Object,Object> clonedMap = new IdentityHashMap<Object,Object>();
    
    private BeanPopulatorBaseConfig baseConfig = new BeanPopulatorBaseConfig();
    
    /** Custom Transformer.  Defaults is a NO_OP. */
    private CustomBeanTransformerSpi customTransformer = TrivialCustomBeanTransformerFactories.getNoopCustomTransformer();
    
    private ImmutableReplicatorSpi immutableReplicatable = ImmutableReplicator.newImmutableReplicatable(this);
    private CollectionReplicatorSpi collectionReplicatable = CollectionReplicator.newCollectionReplicatable(this);
    private MapReplicatorSpi mapReplicatable = MapReplicator.newMapReplicatable(this);
    private ArrayReplicatorSpi arrayReplicatable = ArrayReplicator.newArrayReplicatable(this);
    private BlobReplicatorSpi blobReplicatable = UnsupportedBlobReplicator.newBlobReplicatable(this);
    private DateReplicatorSpi dateReplicatable = DateReplicator.newDateReplicatable(this);
    private BeanReplicatorSpi beanReplicatable = new BeanReplicator(this);
    
    public final void reset() {
        clonedMap = new IdentityHashMap<Object,Object>();
    }
    
    @Override
    public final <T> T transform(Object from, Class<T> toClass, PropertyInfo propertyInfo) 
    {
        try {
            if (customTransformer.isTransformable(from, toClass, propertyInfo))
                return customTransformer.transform(from, toClass, propertyInfo);
            return replicate(from, toClass);
        } catch (SecurityException e) {
            throw new BeanlibException(e);
        }
    }
    
    /**
     * A misnormer.  Use {@link #initCustomTransformerFactory(net.sf.beanlib.spi.CustomBeanTransformerSpi.Factory)} instead.
     */
    @Deprecated
    public final BeanTransformer initCustomTransformer(CustomBeanTransformerSpi.Factory customTransformer) {
        this.customTransformer = customTransformer.newCustomBeanTransformer(this);
        return this;
    }
    
    /** Initializes with a custom transformer factory. */
    public final BeanTransformer initCustomTransformerFactory(CustomBeanTransformerSpi.Factory customTransformer) {
        this.customTransformer = customTransformer.newCustomBeanTransformer(this);
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
        this.collectionReplicatable = factory.newCollectionReplicatable(this);
        return this;
    }
    
    public CollectionReplicatorSpi getCollectionReplicatable() {
        return collectionReplicatable;
    }
    
    public BeanTransformer initMapReplicatable(MapReplicatorSpi.Factory factory) {
        this.mapReplicatable = factory.newMapReplicatable(this);
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
        this.immutableReplicatable = immutableReplicatableFactory.newImmutableReplicatable(this);
        return this;
    }

    public ImmutableReplicatorSpi getImmutableReplicatable() {
        return immutableReplicatable;
    }

    public BeanTransformer initArrayReplicatable(ArrayReplicatorSpi.Factory arrayReplicatableFactory) {
        this.arrayReplicatable = arrayReplicatableFactory.newArrayReplicatable(this);
        return this;
    }

    public ArrayReplicatorSpi getArrayReplicatable() {
        return arrayReplicatable;
    }

    public BeanTransformer initBlobReplicatable(BlobReplicatorSpi.Factory blobReplicatableFactory) {
        this.blobReplicatable = blobReplicatableFactory.newBlobReplicatable(this);
        return this;
    }

    public BlobReplicatorSpi getBlobReplicatable() {
        return blobReplicatable;
    }

    public BeanTransformer initBeanReplicatable(BeanReplicatorSpi.Factory objectReplicatableFactory) {
        this.beanReplicatable = objectReplicatableFactory.newBeanReplicatable(this);
        return this;
    }

    public BeanReplicatorSpi getBeanReplicatable() {
        return beanReplicatable;
    }

    public BeanTransformerSpi initDateReplicatable(DateReplicatorSpi.Factory dateReplicatableFactory) {
        this.dateReplicatable = dateReplicatableFactory.newDateReplicatable(this);
        return this;
    }

    public DateReplicatorSpi getDateReplicatable() {
        return dateReplicatable;
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
