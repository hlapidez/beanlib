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
package net.sf.beanlib.utils;

/**
 * Class Utilities.
 * 
 * @author Joe D. Velopar
 */
public enum ClassUtils {
    ;
    /** Returns the unqalified class name. */
    public static String unqualify(Class c) {
        if (c == null)
            return null;
        String fqcn = c.getName();
        int idx = fqcn.lastIndexOf('.');
        return idx == -1 ? fqcn : fqcn.substring(idx+1);
    }
    
    /** 
     * Returns true if the given class is known to be immutable; false otherwise. 
     */
    public static boolean immutable(Class c) {
        if (c == null)
            return false;
        return c == String.class
            || c.isPrimitive()
            || c.isEnum()
            || Number.class.isAssignableFrom(c) && isJavaPackage(c)
            ;
    }
    
    /**
     * Returns true if the given class is under a package that starts with "java.". 
     */
    public static boolean isJavaPackage(Class c) {
        if (c == null)
            return false;
        Package p = c.getPackage();
        return p != null && p.getName().startsWith("java.");
    }
}
