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
package net.sf.beanlib.api;

/**
 * Object Transformer.
 * 
 * @author Joe D. Velopar
 */
public interface Transformable {
	/**
	 * Returns an object transformed from the input object with the given target class.
	 * 
	 * @param in the input object to be transformed.
	 * @param toClass the target class to be transformed to.
	 * @return the transformed object.
	 */
	public <T> T transform(Object in, Class<T> toClass);
}
