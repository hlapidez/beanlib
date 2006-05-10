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

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Properties;

import net.sf.beanlib.hibernate3.DtoCentricHibernate3Template.DtoCentricCloseSuppressingInvocationHandler;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Settings;
import org.hibernate.dialect.Dialect;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.IdentifierGeneratorFactory;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.impl.SessionImpl;
import org.hibernate.type.TypeFactory;

/**
 * Hibernate 3 DB Sequence Generator.
 * 
 * @author Joe D. Velopar
 */
public class Hibernate3SequenceGenerator
{
    private Hibernate3SequenceGenerator() {
    }
    
    /** Returns the next sequence id from the specified sequence and session. */
    public static long nextval(String sequenceName, Session session) 
    {
        SessionImpl sessionImpl = null;
        
        if (Proxy.isProxyClass(session.getClass())) 
        {
            // Dig out the underlying session.
            InvocationHandler ih = Proxy.getInvocationHandler(session);
            DtoCentricCloseSuppressingInvocationHandler dch = (DtoCentricCloseSuppressingInvocationHandler)ih;
            session = dch.getTarget();
        }
        sessionImpl = (SessionImpl)session;
        IdentifierGenerator idGenerator = createIdentifierGenerator(sequenceName, session);
        Serializable id = idGenerator.generate(sessionImpl, null);
        return (Long)id;
    }
    
    /** Returns the identifier generator created for the specified sequence and session. */
    private static IdentifierGenerator createIdentifierGenerator(String sequenceName, Session session) 
    {
        SessionFactory sessionFactory = session.getSessionFactory();
        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl)sessionFactory;
        Settings settings = sessionFactoryImpl.getSettings();
        Dialect dialect = settings.getDialect();

        Properties params = new Properties();
        params.setProperty("sequence", sequenceName);
        
        return IdentifierGeneratorFactory.create(
                "sequence",
                TypeFactory.heuristicType("long"),
                params,
                dialect
            );
    }
}
