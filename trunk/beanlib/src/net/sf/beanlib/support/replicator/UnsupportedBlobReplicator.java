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
package net.sf.beanlib.support.replicator;

import java.sql.Blob;

import net.sf.beanlib.spi.BeanTransformableSpi;
import net.sf.beanlib.spi.replicator.BlobReplicatable;

/**
 * @author Joe D. Velopar
 */
public class UnsupportedBlobReplicator implements BlobReplicatable 
{
    public static final Factory factory = new Factory();
    
    public static class Factory implements BlobReplicatable.Factory {
        private Factory() {}
        
        public BlobReplicatable newReplicatable(BeanTransformableSpi beanTransformer) {
            return new UnsupportedBlobReplicator();
        }
    }
    
    private UnsupportedBlobReplicator() {}
    
    public <T> T replicateBlob(Blob fromBlob, Class<T> toClass) {
        throw new UnsupportedOperationException("You need to supply your own BlobReplicatable.");
    }
}
