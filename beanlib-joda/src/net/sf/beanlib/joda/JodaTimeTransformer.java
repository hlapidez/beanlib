/*
 * Copyright 2008 The Apache Software Foundation.
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
package net.sf.beanlib.joda;

import java.util.HashMap;
import java.util.Map;

import net.sf.beanlib.PropertyInfo;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.CustomBeanTransformerSpi;
import net.sf.beanlib.spi.TrivialCustomBeanTransformerFactories;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

/**
 * A simple Joda Time Transformer.
 * 
 * @author Hanson Char
 */
public class JodaTimeTransformer implements CustomBeanTransformerSpi 
{
    public static class Factory implements CustomBeanTransformerSpi.Factory {
        public CustomBeanTransformerSpi newCustomBeanTransformer(BeanTransformerSpi contextBeanTransformer) {
            return singleton;
        }
    }
    
    private static final JodaTimeTransformer singleton = new JodaTimeTransformer();
    
    private final Map<Class<?>,CustomBeanTransformerSpi> map = new HashMap<Class<?>,CustomBeanTransformerSpi>();

    private JodaTimeTransformer() {
        map.put(DateTime.class, TrivialCustomBeanTransformerFactories.getIdentityCustomTransformer());
        map.put(DateMidnight.class, TrivialCustomBeanTransformerFactories.getIdentityCustomTransformer());
        map.put(LocalDate.class, TrivialCustomBeanTransformerFactories.getIdentityCustomTransformer());
        map.put(LocalTime.class, TrivialCustomBeanTransformerFactories.getIdentityCustomTransformer());
        map.put(LocalDateTime.class, TrivialCustomBeanTransformerFactories.getIdentityCustomTransformer());
    }
    
    public <T> boolean isTransformable(Object from, Class<T> toClass, PropertyInfo propertyInfo) {
        if (from == null)
            return false;
        return map.containsKey(from.getClass());
    }

    public <T> T transform(Object in, Class<T> toClass, PropertyInfo propertyInfo) {
        CustomBeanTransformerSpi t = map.get(in.getClass());
        return t.transform(in, toClass, propertyInfo);
    }
}
