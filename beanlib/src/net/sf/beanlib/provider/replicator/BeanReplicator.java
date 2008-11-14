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

import net.jcip.annotations.ThreadSafe;
import net.sf.beanlib.BeanlibException;
import net.sf.beanlib.provider.BeanTransformer;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.replicator.BeanReplicatorSpi;

/**
 * Default implementation of {@link net.sf.beanlib.spi.replicator.BeanReplicatorSpi}.
 * 
 * @author Joe D. Velopar
 */
public class BeanReplicator extends ReplicatorTemplate implements BeanReplicatorSpi
{
    /**
     * Convenient constructor for passing in a bean transformer.
     */
    public BeanReplicator(BeanTransformerSpi beanTransformer) 
    {
        super(beanTransformer);
    }
    
    /**
     * Convenient constructor to make use of the default {@link BeanTransformer}.
     */
    public BeanReplicator() {
        super(new BeanTransformer());
    }
    
    /**
     * Replicates a given JavaBean object.
     * 
     * @param <V> from type
     * @param from from bean to be replicated.
     */
    @SuppressWarnings("unchecked")
    public <V> V replicateBean(V from) {
        return replicateBean(from, (Class<V>)from.getClass());
    }
    
    /**
     * Replicates the properties of a JavaBean object to an instance of a target class,
     * which is selected from the given "from" and "to" classes, giving
     * priority to the one which is more specific whenever possible.
     * 
     * @param <V> from type
     * @param <T> target type
     * @param from from bean (after unenhancement) to be replicated
     * @param toClass target class to be instantiated
     */
    public <V,T> T replicateBean(V from, Class<T> toClass) {
        return this.replicateBean(from, toClass, from);
    }
    
    /**
     * Replicates the properties of a JavaBean object to an instance of a target class,
     * which is selected from the given "from" and "to" classes, giving
     * priority to the one which is more specific whenever possible.
     * 
     * @param <V> from type
     * @param <T> target type
     * @param from from bean (after unenhancement) to be replicated
     * @param toClass target class to be instantiated
     * @param originalFrom the original from bean before any "unehancement"
     * @return an instance of the  replicated bean
     */
    protected <V,T> T replicateBean(V from, Class<T> toClass, V originalFrom)
    {
        Class fromClass = from.getClass();
        String fromClassName = fromClass.getName();
        
        if (fromClassName.startsWith("net.sf.cglib.")) {
            // Want to skip the cglib stuff.
            return null;
        }
        if (fromClassName.startsWith("java.")) {
            if (!toClass.isAssignableFrom(fromClass))
                return null;
            // Sorry, don't really know what it is ... soldier on...
        }
        T to;
        try {
            to = createToInstance(from, toClass);
        } catch (SecurityException e) {
            throw new BeanlibException(e);
        } catch (InstantiationException e) {
            throw new BeanlibException(e);
        } catch (IllegalAccessException e) {
            throw new BeanlibException(e);
        } catch (NoSuchMethodException e) {
            throw new BeanlibException(e);
        }
        putTargetCloned(originalFrom, to);
        // recursively populate member objects.
        populateBean(from, to);
        return to;
    }
}
