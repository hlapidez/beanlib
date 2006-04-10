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
package net.sf.beanlib.hibernate3;

import java.sql.Blob;

import net.sf.beanlib.hibernate.HibernateBeanTransformer;

import org.hibernate.Hibernate;


/**
 * Hibernate 3 Bean Transformer.
 * 
 * @author Joe D. Velopar
 */
public class Hibernate3BeanTransformer extends HibernateBeanTransformer
{
	@Override
    public void hibernateInitialize(Object obj) {
		// Note this Hibernate is from the Hibernate 3 package
        // http://sourceforge.net/forum/forum.php?thread_id=1470862&forum_id=470286
        if (!Hibernate.isInitialized(obj))
            Hibernate.initialize(obj);
	}
	@Override
    public Blob hibernateCreateBlob(byte[] byteArray) {
		// Note this Hibernate is from the Hibernate 3 package
		return Hibernate.createBlob(byteArray);
	}
}
