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
package net.sf.beanlib.jaxb2;

import junit.framework.JUnit4TestAdapter;
import net.sf.beanlib.BeanPopulator;

import org.junit.Test;

/**
 * @author Hanson Char
 */
public class JaxbBeanPopulatorTest 
{
    @Test
    public void test() {
        Object from = new Object();
        Object to = new Object();
        new BeanPopulator(from, to)
            .initDetailedBeanPopulatable(null)  // always populate
            .initSetterMethodCollector(FluentSetterMethodCollector.inst)
            .initTransformer(null)  // TODO
            ;
    }

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(JaxbBeanPopulatorTest.class);
    }
}
