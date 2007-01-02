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
package net.sf.beanlib.api;

import java.lang.reflect.Method;

import net.sf.beanlib.BeanlibException;

import org.apache.commons.logging.Log;

/**
 * @author Joe D. Velopar
 */
public class BeanPopulationExceptionHandlerAdapter implements BeanPopulationExceptionHandler 
{
    protected String propertyName;
    protected Object fromBean;
    protected Method readerMethod;
    protected Object toBean;
    protected Method setterMethod;

    public void handleException(Throwable t, Log log)
    {
        log.error("\n" 
                + "propertyName=" + propertyName + "\n"
                + "readerMethod=" + readerMethod + "\n"
                + "setterMethod=" + setterMethod + "\n"
                + "fromBean=" + fromBean + "\n"
                + "toBean=" + toBean + "\n"
                , t);

        if (t instanceof RuntimeException)
            throw (RuntimeException) t;
        if (t instanceof Error)
            throw (Error) t;
        throw new BeanlibException(t);
    }

    public BeanPopulationExceptionHandlerAdapter initPropertyName(String propertyName) 
    {
        this.propertyName = propertyName;
        return this;
    }

    public BeanPopulationExceptionHandlerAdapter initFromBean(Object fromBean) {
        this.fromBean = fromBean;
        return this;
    }

    public BeanPopulationExceptionHandlerAdapter initReaderMethod(
            Method readerMethod) {
        this.readerMethod = readerMethod;
        return this;
    }

    public BeanPopulationExceptionHandlerAdapter initToBean(Object toBean) {
        this.toBean = toBean;
        return this;
    }

    public BeanPopulationExceptionHandlerAdapter initSetterMethod(
            Method setterMethod) {
        this.setterMethod = setterMethod;
        return this;
    }
}
