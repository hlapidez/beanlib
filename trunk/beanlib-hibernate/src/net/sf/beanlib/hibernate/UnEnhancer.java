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

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

/**
 * @author Joe D. Velopar
 */
public class UnEnhancer
{
    private UnEnhancer() {}
    
    private static final String JAVASSIST_STARTWITH = "org.javassist.tmp."; 
    private static final String JAVASSIST_INDEXOF = "_$$_javassist_";

    /** 
     * Returns true if the given class is found to be a javassist enhanced class; 
     * false otherwise. 
     */ 
    private static boolean isJavassistEnhanced(Class c) {
        String className = c.getName();
        // pattern found in javassist 3.4 and 3.6's ProxyFactory 
        return className.startsWith(JAVASSIST_STARTWITH)
            || className.indexOf(JAVASSIST_INDEXOF) != -1
            ;
    }
    
    /**
     * Digs out the pre CGLIB/Javassist enhanced class, if any.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> unenhanceClass(Class c) 
    {
        boolean enhanced = true;
        
        while (c != null && enhanced)
        {
            enhanced =  Enhancer.isEnhanced(c)
                     || isJavassistEnhanced(c)
                     ;
            if (enhanced)
                c = c.getSuperclass();
        }
        return (Class<T>)c;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getActualClass(Object object) 
    {
        Class c = object.getClass();
        boolean enhanced = true;
        
        while (c != null && enhanced)
        {
            enhanced =  Enhancer.isEnhanced(c)
                     || isJavassistEnhanced(c)
                     ;
            if (enhanced) 
            {
                if (object instanceof HibernateProxy) 
                {
                    HibernateProxy hibernateProxy = (HibernateProxy)object; 
                    LazyInitializer lazyInitializer = hibernateProxy.getHibernateLazyInitializer();
                    try {
                        // suggested by Chris (harris3@sourceforge.net)
                        Object impl = lazyInitializer.getImplementation();
                        
                        if (impl != null)
                            return (Class<T>)impl.getClass();
                    } catch(HibernateException ex) {
                        Logger.getLogger(UnEnhancer.class).warn("Unable to retrieve the underlying persistent object", ex);
                    }
                    return lazyInitializer.getPersistentClass();
                }
                c = c.getSuperclass();
            }
        }
        return (Class<T>)c;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T unenhanceObject(T object) {
        if (object == null)
            return null;
        Class c = object.getClass();
        boolean enhanced = true;
        
        while (c != null && enhanced)
        {
            enhanced =  Enhancer.isEnhanced(c)
                     || isJavassistEnhanced(c)
                     ;
            if (enhanced) 
            {
                if (object instanceof HibernateProxy) 
                {
                    HibernateProxy hibernateProxy = (HibernateProxy)object; 
                    LazyInitializer lazyInitializer = hibernateProxy.getHibernateLazyInitializer();
                    return (T)lazyInitializer.getImplementation();
                }
                c = c.getSuperclass();
            }
        }
        return object;
    }
}
