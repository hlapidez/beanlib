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
package net.sf.beanlib.provider.replicator;

import java.sql.Blob;

import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.replicator.BlobReplicatorSpi;

/**
 * @author Joe D. Velopar
 */
public class UnsupportedBlobReplicator implements BlobReplicatorSpi 
{
    private static final Factory factory = new Factory();
    
    private static class Factory implements BlobReplicatorSpi.Factory {
        private Factory() {}
        
        public UnsupportedBlobReplicator newReplicatable(BeanTransformerSpi beanTransformer) {
            return new UnsupportedBlobReplicator();
        }
    }
    
    public static UnsupportedBlobReplicator newReplicatable(BeanTransformerSpi beanTransformer) {
        return factory.newReplicatable(beanTransformer);
    }
    
    private UnsupportedBlobReplicator() {}
    
    public <T> T replicateBlob(Blob fromBlob, Class<T> toClass) {
        throw new UnsupportedOperationException("You need to supply your own BlobReplicatable.");
    }
}
