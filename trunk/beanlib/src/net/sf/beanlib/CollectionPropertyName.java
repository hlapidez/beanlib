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
package net.sf.beanlib;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Used to uniquely identify a Collection or Map property of a JavaBean object.
 * 
 * @author Joe D. Velopar
 */
public class CollectionPropertyName {
	public static final CollectionPropertyName[] EMPTY_ARRAY = {}; 
	private final Class declaringClass;
	private final String collectionProperty;
	private final int hashCode;
	/** Convenient factory method. */
	public static CollectionPropertyName[] createCollectionPropertyNames(Class declaringClass, String[] collectionProperties)
	{
		Set<CollectionPropertyName> set = new HashSet<CollectionPropertyName>();
		
        for (String s : collectionProperties)
            set.add(new CollectionPropertyName(declaringClass, s));
		return set.toArray(EMPTY_ARRAY);
	}
	/**
	 * @param declaringClass declaring class of the Collection or Map property.
	 * @param collectionProperty Collection or Map property name.
	 */
	public CollectionPropertyName(Class declaringClass, String collectionProperty) {
		this.declaringClass = declaringClass;
		this.collectionProperty = collectionProperty;
		this.hashCode = BeanGetter.inst.getBeanHashCode(this);
	}
	public Class getDeclaringClass() {
		return declaringClass;
	}
	public String getCollectionProperty() {
		return collectionProperty;
	}
	@Override
    public boolean equals(Object obj) {
		if (!(obj instanceof CollectionPropertyName))
			return false;
		CollectionPropertyName that = (CollectionPropertyName)obj;
		return new EqualsBuilder()
				.append(this.declaringClass, that.declaringClass)
				.append(this.collectionProperty, that.collectionProperty)
				.isEquals()
				;
	}
	@Override
    public int hashCode() {
		return this.hashCode;
	}
}
