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
package net.sf.beanlib.provider.replicator;

import net.sf.beanlib.BeanlibException;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.replicator.BeanReplicatorSpi;

/**
 * Default implementation of {@link net.sf.beanlib.spi.replicator.BeanReplicatorSpi}.
 * 
 * @author Joe D. Velopar
 */
public class BeanReplicator extends ReplicatorTemplate implements BeanReplicatorSpi
{
    private static final Factory factory = new Factory();
    
    /**
     * Factory for {@link BeanReplicator}
     * 
     * @author Joe D. Velopar
     */
    private static class Factory implements BeanReplicatorSpi.Factory {
        private Factory() {}
        
        public BeanReplicator newReplicatable(BeanTransformerSpi beanTransformer) {
            return new BeanReplicator(beanTransformer);
        }
    }

    public static BeanReplicator newReplicatable(BeanTransformerSpi beanTransformer) {
        return factory.newReplicatable(beanTransformer);
    }
    
    protected BeanReplicator(BeanTransformerSpi beanTransformer) 
    {
        super(beanTransformer);
    }
    
    public <V,T> T replicateBean(V from, Class<T> toClass)
    {
        T to;
        try {
            to = createToInstance(chooseClass(from.getClass(), toClass));
        } catch (SecurityException e) {
            throw new BeanlibException(e);
        } catch (InstantiationException e) {
            throw new BeanlibException(e);
        } catch (IllegalAccessException e) {
            throw new BeanlibException(e);
        } catch (NoSuchMethodException e) {
            throw new BeanlibException(e);
        }
        putTargetCloned(from, to);
        // recursively populate member objects.
        populateBean(from, to);
        return to;
    }
    
    @SuppressWarnings("unchecked")
    private <T> Class<T> chooseClass(Class<?> fromClass, Class<T> toClass) {
        return (Class<T>)(toClass.isAssignableFrom(fromClass) ? fromClass : toClass);
    }
}
