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

import static net.sf.beanlib.utils.ClassUtils.immutable;

import java.lang.reflect.Array;

import net.sf.beanlib.transform.spi.ArrayReplicatable;
import net.sf.beanlib.transform.spi.BeanTransformableSpi;

/**
 * @author Joe D. Velopar
 */
public class ArrayReplicator extends ReplicatorTemplate implements ArrayReplicatable 
{
    // must be invoked as the first method on this object
    public ArrayReplicatable initBeanTransformableSpi(BeanTransformableSpi beanTransformableSpi) 
    {
        super.setBeanTransformableSpi(beanTransformableSpi);
        return this;
    }
    
    public <V,T> T replicateArray(V[] from, Class<T> toClass)
    {
        Class fromClass = from.getClass();
        Class fromComponentType = fromClass.getComponentType();
        // primitive array
        if (immutable(fromComponentType))
        {
            int len = Array.getLength(from);
            Object to = Array.newInstance(fromComponentType, len);
            System.arraycopy(from, 0, to, 0, len);
            putTargetCloned(from, to);
            return toClass.cast(to);
        }
        // non-primitive array
        int len = Array.getLength(from);
        Object to = Array.newInstance(fromComponentType, len);
        putTargetCloned(from, to);
        Object[] fromArray = from;
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
