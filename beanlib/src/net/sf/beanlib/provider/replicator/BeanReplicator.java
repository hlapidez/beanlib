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
    private static final Factory factory = new Factory();
    
    /**
     * Factory for {@link BeanReplicator}
     * 
     * @author Joe D. Velopar
     */
    @ThreadSafe
    private static class Factory implements BeanReplicatorSpi.Factory {
        private Factory() {}
        
        public BeanReplicator newBeanReplicatable(BeanTransformerSpi beanTransformer) {
            return new BeanReplicator(beanTransformer);
        }
    }

    public static BeanReplicator newBeanReplicatable(BeanTransformerSpi beanTransformer) {
        return factory.newBeanReplicatable(beanTransformer);
    }

    /**
     * Convenient factory method to use the default {@link BeanTransformer}.
     */
    public static BeanReplicator newBeanReplicatable() {
        return factory.newBeanReplicatable(BeanTransformer.newBeanTransformer());
    }
    
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
        super(BeanTransformer.newBeanTransformer());
    }
    
    /** Convenient method to replicate a bean to the same target class. */
    public <V> V replicateBean(V from) {
        return replicateBean(from, from.getClass());
    }
    
    public <V,T> T replicateBean(V from, Class<T> toClass)
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
