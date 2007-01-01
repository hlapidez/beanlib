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

import net.sf.beanlib.transform.spi.BeanTransformableSpi;
import net.sf.beanlib.transform.spi.ImmutableReplicatable;

/**
 * @author Joe D. Velopar
 */
public class ImmutableReplicator implements ImmutableReplicatable
{
    public ImmutableReplicatable initBeanTransformableSpi(BeanTransformableSpi beanTransformableSpi) 
    {
        return this;
    }

    public <V, T> T replicateImmutable(V immutableFrom, Class<T> toClass) 
    {
        if (immutableFrom == null)
            return null;
        if (toClass.isAssignableFrom(immutableFrom.getClass()))
            return toClass.cast(immutableFrom);
        return null;
    }
}
