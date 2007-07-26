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

import static net.sf.beanlib.utils.ClassUtils.immutable;

import java.lang.reflect.Array;

import net.jcip.annotations.ThreadSafe;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.replicator.ArrayReplicatorSpi;

/**
 * Default implementation of {@link net.sf.beanlib.spi.replicator.ArrayReplicatorSpi}.
 * 
 * @author Joe D. Velopar
 */
public class ArrayReplicator extends ReplicatorTemplate implements ArrayReplicatorSpi 
{
    private static final Factory factory = new Factory();
    
    /**
     * Factory for {@link ArrayReplicator}
     * 
     * @author Joe D. Velopar
     */
    @ThreadSafe
    private static class Factory implements ArrayReplicatorSpi.Factory {
        private Factory() {}
        
        public ArrayReplicator newArrayReplicatable(BeanTransformerSpi beanTransformer) {
            return new ArrayReplicator(beanTransformer);
        }
    }
    
    public static ArrayReplicator newArrayReplicatable(BeanTransformerSpi beanTransformer) {
        return factory.newArrayReplicatable(beanTransformer);
    }
    
    protected ArrayReplicator(BeanTransformerSpi beanTransformer) {
        super(beanTransformer);
    }
    
    public <T> T replicateArray(Object arrayToCopy, Class<T> toClass)
    {
        if (!toClass.isAssignableFrom(arrayToCopy.getClass()))
            return null;
        Class fromClass = arrayToCopy.getClass();
        Class fromComponentType = fromClass.getComponentType();
        // primitive array
        if (immutable(fromComponentType))
        {
            int len = Array.getLength(arrayToCopy);
            Object to = Array.newInstance(fromComponentType, len);
            System.arraycopy(arrayToCopy, 0, to, 0, len);
            putTargetCloned(arrayToCopy, to);
            return toClass.cast(to);
        }
        // non-primitive array
        int len = Array.getLength(arrayToCopy);
        Object to = Array.newInstance(fromComponentType, len);
        putTargetCloned(arrayToCopy, to);
        Object[] fromArray = (Object[])arrayToCopy;
        Object[] toArray = (Object[])to;
        // recursively populate member objects.
        for (int i=fromArray.length-1; i >= 0; i--) {
            Object fromElement = fromArray[i];
            Object toElement = replicate(fromElement);
            toArray[i] = toElement;
        }
        return toClass.cast(toArray);
    }
}
