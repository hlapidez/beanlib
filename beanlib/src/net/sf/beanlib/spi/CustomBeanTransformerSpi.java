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
package net.sf.beanlib.spi;

import net.sf.beanlib.api.Transformable;

/**
 * Interface for Custom Bean Transformer.
 * Can be used to customize the transformation process provided by {@link BeanTransformerSpi}.
 * 
 * @author Joe D. Velopar
 */
public interface CustomBeanTransformerSpi extends Transformable {
	public static final CustomBeanTransformerSpi NO_OP = new CustomBeanTransformerSpi() {
		public boolean isTransformable(
                @SuppressWarnings("unused") Object in, 
                @SuppressWarnings("unused") Class toClass, 
                @SuppressWarnings("unused") BeanTransformerSpi hibernateBeanTransformer) 
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
	public boolean isTransformable(Object from, Class toClass, BeanTransformerSpi hibernateBeanTransformer);
}
