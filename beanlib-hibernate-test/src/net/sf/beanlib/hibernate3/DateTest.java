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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Date;

import junit.framework.JUnit4TestAdapter;
import net.sf.beanlib.hibernate.HibernateBeanReplicator;
import net.sf.beanlib.spi.BeanTransformerSpi;
import net.sf.beanlib.spi.CustomBeanTransformerSpi;

import org.junit.Test;

/**
 * @author Joe D. Velopar
 */
public class DateTest {
    
    private static class Pojo {
        private Date date = new Timestamp(new Date().getTime());
        private String text = "whatever";

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
        
    }
    
    @Test 
    public void testConvertTimestampToDate() 
    {
        Pojo source = new Pojo();
        // Replicate Timestamp into Date
        HibernateBeanReplicator replicator =
            new Hibernate3BeanReplicator()
            .initCustomTransformer(
                new CustomBeanTransformerSpi.Factory() 
                {
                    public CustomBeanTransformerSpi newCustomBeanTransformer(BeanTransformerSpi beanTransformer) 
                    {
                        return new CustomBeanTransformerSpi() 
                        {
                            public <T> boolean isTransformable(Object from, Class<T> toClass) {
                                return from instanceof Date && toClass == Date.class;
                            }
    
                            public <T> T transform(Object in, Class<T> toClass) {
                                Date d = (Date)in;
                                return (T)new Date(d.getTime());
                            }
                        };
                    }
                });
        Pojo clone = replicator.deepCopy(source);
        assertNotSame(clone, source);
        assertEquals(clone.getText(), source.getText());
        assertSame(source.getDate().getClass(), Timestamp.class);
        assertSame(clone.getDate().getClass(), Date.class);
        assertTrue(clone.getDate().getTime() == source.getDate().getTime());
    }
    
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(DateTest.class);
    }
}
