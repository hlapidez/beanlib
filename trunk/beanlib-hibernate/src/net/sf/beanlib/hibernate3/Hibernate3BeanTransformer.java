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
package net.sf.beanlib.hibernate3;

import net.jcip.annotations.ThreadSafe;
import net.sf.beanlib.hibernate.UnEnhancer;
import net.sf.beanlib.provider.BeanPopulator;
import net.sf.beanlib.provider.BeanTransformer;
import net.sf.beanlib.spi.BeanPopulatorSpi;
import net.sf.beanlib.spi.BeanTransformerSpi;

/**
 * Hibernate 3 specific Bean Transformer.
 * 
 * @author Joe D. Velopar
 */
public class Hibernate3BeanTransformer extends BeanTransformer
{
    private static final Factory factory = new Factory();
    
    /**
     * Hibernate Bean Transformer Factory.
     * 
     * @author Joe D. Velopar
     */
    @ThreadSafe
    private static class Factory implements BeanTransformerSpi.Factory {
        private Factory() {}
        
        public Hibernate3BeanTransformer newBeanTransformer(BeanPopulatorSpi.Factory beanPopulatorFactory) 
        {
            Hibernate3BeanTransformer transformer = new Hibernate3BeanTransformer(beanPopulatorFactory);
            transformer.initCollectionReplicatable(
                    Hibernate3CollectionReplicator.getFactory());
            transformer.initMapReplicatable(
                    Hibernate3MapReplicator.getFactory());
            transformer.initBlobReplicatable(
                    Hibernate3BlobReplicator.getFactory());
            transformer.initBeanReplicatable(
                    Hibernate3JavaBeanReplicator.getFactory());
            return transformer;
        }
    }
    
    /** Convenient factory method that defaults to use {@link BeanPopulator#factory}. */
    public static Hibernate3BeanTransformer newBeanTransformer()
    {
        return factory.newBeanTransformer(BeanPopulator.factory);
    }

    protected Hibernate3BeanTransformer(BeanPopulatorSpi.Factory beanPopulatorFactory) {
        super(beanPopulatorFactory);
    }
    
    @Override
    protected <T> T createToInstance(Object from, Class<T> toClass)
        throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException 
    {
        // figure out the pre-enhanced class
        Class<T> actualClass = UnEnhancer.getActualClass(from);
        Class<T> targetClass = chooseClass(actualClass, toClass);
        return newInstanceAsPrivileged(targetClass);
    }

    @Override
    protected Object replicate(Object from)
    {
        return super.replicate(
                    UnEnhancer.unenhanceObject(from));
    }
}
