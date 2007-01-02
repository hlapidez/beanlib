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

import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.replicator.ImmutableReplicatorSpi;

/**
 * @author Joe D. Velopar
 */
public class ImmutableReplicator implements ImmutableReplicatorSpi
{
    private static final Factory factory = new Factory();
    
    private static class Factory implements ImmutableReplicatorSpi.Factory {
        private Factory() {}
        
        public ImmutableReplicator newReplicatable(BeanTransformerSpi beanTransformer) {
            return new ImmutableReplicator();
        }
    }

    public static ImmutableReplicator newReplicatable(BeanTransformerSpi beanTransformer) {
        return factory.newReplicatable(beanTransformer);
    }
    
    protected ImmutableReplicator() {}

    public <V, T> T replicateImmutable(V immutableFrom, Class<T> toClass) 
    {
        if (immutableFrom == null)
            return null;
        if (toClass.isAssignableFrom(immutableFrom.getClass()))
            return toClass.cast(immutableFrom);
        return null;
    }
}
