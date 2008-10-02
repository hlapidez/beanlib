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
package net.sf.beanlib.spi;

import java.lang.reflect.Method;

import net.sf.beanlib.provider.JavaBeanPopulatableSupport;

/**
 * Used to control if a JavaBean property should be populated.
 * 
 * @author Joe D. Velopar
 */
public interface DetailedBeanPopulatable {
    public static final DetailedBeanPopulatable ALWAYS_POPULATE = new DetailedBeanPopulatable() {
        public boolean shouldPopulate(
            @SuppressWarnings("unused") String propertyName, 
            @SuppressWarnings("unused") Object fromBean, 
            @SuppressWarnings("unused") Method readerMethod, 
            @SuppressWarnings("unused") Object toBean, 
            @SuppressWarnings("unused") Method setterMethod) 
        {
            return true;
        }
    };
    public static final DetailedBeanPopulatable JAVABEAN_POPULATE = JavaBeanPopulatableSupport.inst;
    /**
     * Returns true if the given JavaBean property should be populated;
     * false otherwise.
     * @param propertyName JavaBean property name.
     * @param fromBean from bean.
     * @param readerMethod reader method of the JavaBean property name.
     * @param toBean to bean.
     * @param setterMethod setter method of the JavaBean property name.
     * @return true if the given JavaBean property should be populated.
     */
    public boolean shouldPopulate(String propertyName, 
            Object fromBean, Method readerMethod, 
            Object toBean, Method setterMethod);
}
