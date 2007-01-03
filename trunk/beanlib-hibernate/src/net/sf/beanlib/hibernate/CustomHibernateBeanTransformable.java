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

import net.sf.beanlib.spi.Transformable;

/**
 * Custom Hibernate Bean Transformable.
 * Can be used to customize the transformation process provided by {@link HibernateBeanTransformableSpi}.
 * 
 * @author Joe D. Velopar
 */
public interface CustomHibernateBeanTransformable extends Transformable {
	public static final CustomHibernateBeanTransformable NO_OP = new CustomHibernateBeanTransformable() {
		public boolean isTransformable(
                @SuppressWarnings("unused") Object in, 
                @SuppressWarnings("unused") Class toClass, 
                @SuppressWarnings("unused") HibernateBeanTransformableSpi hibernateBeanTransformer) 
        { 
            return false; 
        }
		public <T> T transform(@SuppressWarnings("unused") Object in, @SuppressWarnings("unused") Class<T> toClass) { return null; }
	};
	
	/** 
	 * Returns true if the given object is to be transformed by this transformer;
	 * false otherwise.
	 * 
	 *  @param from given object to be transformed
	 *  @param toClass target class to transform to
	 *  @param hibernateBeanTransformer Hibernate Bean Transformer  
	 */
	public boolean isTransformable(Object from, Class toClass, HibernateBeanTransformableSpi hibernateBeanTransformer);
}
