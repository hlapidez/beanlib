/*
 * Copyright 2005 The Apache Software Foundation.
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
package net.sf.beanlib.hibernate;

import net.sf.cglib.proxy.Enhancer;

/**
 * @author Joe D. Velopar
 */
public class UnEnhancer
{
    private UnEnhancer() {}
    /**
     * Digs out the pre CGLIB/Javassist enhanced class, if any.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> unenhance(Class c) 
    {
        boolean enhanced = true;
        
        while (c != null && enhanced)
        {
            enhanced = Enhancer.isEnhanced(c);
            
            if (!enhanced)
            {
                String className = c.getName();
                // pattern found in javassist 3.4 and 3.6's ProxyFactory 
                enhanced = className.startsWith("org.javassist.tmp.")
                        || className.indexOf("_$$_javassist_") != -1;
            }
            if (enhanced)
                c = c.getSuperclass();
        }
        return (Class<T>)c;
    }

}
