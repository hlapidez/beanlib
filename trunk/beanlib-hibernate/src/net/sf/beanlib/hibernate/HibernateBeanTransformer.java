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

import static net.sf.beanlib.utils.ClassUtils.immutable;
import static net.sf.beanlib.utils.ClassUtils.isJavaPackage;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.sf.beanlib.ProtectedSetterMethodCollector;
import net.sf.beanlib.api.BeanMethodCollector;
import net.sf.beanlib.api.BeanMethodFinder;
import net.sf.beanlib.api.BeanPopulatable;
import net.sf.beanlib.api.BeanSourceHandler;
import net.sf.beanlib.api.DetailedBeanPopulatable;
import net.sf.beanlib.provider.BeanPopulator;
import net.sf.beanlib.utils.BlobUtils;
import net.sf.cglib.proxy.Enhancer;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hibernate Bean Transformer.
 * 
 * @author Joe D. Velopar
 */
public abstract class HibernateBeanTransformer implements HibernateBeanTransformableSpi
{
    protected final Log log = LogFactory.getLog(this.getClass());
    
    // Contains those hibernate objects that have been replicated.
    private Map<Object,Object> clonedMap = new IdentityHashMap<Object,Object>();
    
    private DetailedBeanPopulatable detailedBeanPopulatable;
    private BeanPopulatable beanPopulatable; 
    private BeanSourceHandler beanSourceHandler;
    private BeanMethodFinder readerMethodFinder;
    private BeanMethodCollector setterMethodCollector = ProtectedSetterMethodCollector.inst;
    
    /** Custom Transformer. */
    private CustomHibernateBeanTransformable customTransformer = CustomHibernateBeanTransformable.NO_OP;

    private boolean debug;    
    
    public abstract void hibernateInitialize(Object obj);
    public abstract Blob hibernateCreateBlob(byte[] byteArray);
    
    public final void reset() {
        clonedMap = new IdentityHashMap<Object,Object>();
    }
    
    public final <T> T transform(Object from, Class<T> toClass) {
        try {
            if (customTransformer.isTransformable(from, toClass, this))
                return customTransformer.transform(from, toClass);
            return this.replicate(from, toClass);
        } catch (InstantiationException e) {
            log.error("", e);
            throw new BeanlibHibernateException(e);
        } catch (IllegalAccessException e) {
            log.error("", e);
            throw new BeanlibHibernateException(e);
        } catch (ClassNotFoundException e) {
            log.error("", e);
            throw new BeanlibHibernateException(e);
        } catch (SQLException e) {
            log.error("", e);
            throw new BeanlibHibernateException(e);
        } catch (IOException e) {
            log.error("", e);
            throw new BeanlibHibernateException(e);
        } catch (SecurityException e) {
            log.error("", e);
            throw new BeanlibHibernateException(e);
        } catch (NoSuchMethodException e) {
            log.error("", e);
            throw new BeanlibHibernateException(e);
        } catch (InvocationTargetException e) {
            log.error("", e);
            throw new BeanlibHibernateException(e);
        }
    }
    private Object replicate(Object from)
        throws SecurityException, InstantiationException, IllegalAccessException, ClassNotFoundException, 
        SQLException, IOException, NoSuchMethodException, InvocationTargetException 
    {
        if (from == null)
            return null;
        return replicate(from, from.getClass());
    }
    /**
     * Recursively replicate, if necessary, the given objects.
     * Currently an object is replicated if it is an instance
     * of Collection, Map, Timestamp, Date, Blob, Hibernate entity, 
     * JavaBean, or an array.
     */
    @SuppressWarnings("unchecked")
    private <T> T replicate(Object from, Class<T> toClass) 
        throws InstantiationException, IllegalAccessException, ClassNotFoundException, 
        SQLException, IOException, SecurityException, NoSuchMethodException, InvocationTargetException 
    {
        if (from == null)
            return null;
        if (immutable(toClass))
            return (T)from;
        Class<?> fromClass = from.getClass();
        // Collection
        if (from instanceof Collection) {
            if (!toClass.isAssignableFrom(fromClass))
                return null;
            return (T)replicateCollection((Collection<?>)from);
        }
        // Map
        if (from instanceof Map) {
            if (!toClass.isAssignableFrom(fromClass))
                return null;
            return (T)replicateMap((Map)from);
        }
        // Timestamp
        if (from instanceof Timestamp) {
            if (!toClass.isAssignableFrom(fromClass))
                return null;
            Timestamp ts = (Timestamp)from;
            return (T)new Timestamp(ts.getTime());
        }
        // Date
        if (from instanceof Date) {
            if (!toClass.isAssignableFrom(fromClass))
                return null;
            Date date = (Date)from;
            return (T)new Date(date.getTime());
        }
        T to = (T)clonedMap.get(from);
        
        if (to != null) {
            if (!toClass.isAssignableFrom(fromClass))
                return null;
            return to;    // already transformed.
        }
        // Array
        if (fromClass.isArray()) {
            if (!toClass.isAssignableFrom(fromClass))
                return null;
            // both are arrays
            return (T)replicateArray(from);
        }
        String fromPackageName = ClassUtils.getPackageName(from.getClass());

        if (fromPackageName.startsWith("java.")) {
            if (!toClass.isAssignableFrom(fromClass))
                return null;
            // Sorry, don't really know what it is ... soldier on...
        }
        
        if (fromPackageName.startsWith("net.sf.cglib.")) {
            // Want to skip the cglib stuff.
            return null;
        }
        // Blob
        if (from instanceof Blob) {
            if (!toClass.isAssignableFrom(fromClass))
                return null;
            Blob fromBlob = (Blob)from;
            to = (T)replicateBlob(fromBlob);
            clonedMap.put(from, to);
            return to;
        }
        // Assume non-array application classes.  TODO: is this assumption valid ?
        to = replicateApplicationObject(from, toClass);
        return to;
    }
    
    private <T> T replicateApplicationObject(Object from, Class<T> toClass) 
        throws InstantiationException, IllegalAccessException, NoSuchMethodException 
    {
        T to = createToInstance(chooseClass(from.getClass(), toClass));
        clonedMap.put(from, to);
        // recursively populate member objects.
        populate(from, to);
        return to;
    }
    
    /** 
     * Returns the fromClass if it is assignable to the toClass;  
     * Otherwise returns the toClass. 
     */
    @SuppressWarnings("unchecked")
    private <T> Class<T> chooseClass(Class<?> fromClass, Class<T> toClass) {
        return (Class<T>)(toClass.isAssignableFrom(fromClass) ? fromClass : toClass);
    }
    
    private Object replicateArray(Object from) 
        throws InstantiationException, IllegalAccessException, ClassNotFoundException, 
        SQLException, IOException, SecurityException, NoSuchMethodException, InvocationTargetException 
    {
        Class fromClass = from.getClass();
        Class fromComponentType = fromClass.getComponentType();
        // primitive array
        if (immutable(fromComponentType))
        {
            int len = Array.getLength(from);
            Object to = Array.newInstance(fromComponentType, len);
            System.arraycopy(from, 0, to, 0, len);
            clonedMap.put(from, to);
            return to;
        }
        // non-primitive array
        int len = Array.getLength(from);
        Object to = Array.newInstance(fromComponentType, len);
        clonedMap.put(from, to);
        Object[] fromArray = (Object[])from;
        Object[] toArray = (Object[])to;
        // recursively populate member objects.
        for (int i=fromArray.length-1; i >= 0; i--) {
            Object fromElement = fromArray[i];
            Object toElement = replicate(fromElement);
            toArray[i] = toElement;
        }
        return toArray;
    }

    // Thanks to Tammo van Lessen for reporting a bug related to cloning an empty collection:
    // http://sourceforge.net/tracker/index.php?func=detail&aid=1432471&group_id=140152&atid=745596
    private Collection<?> replicateCollection(Collection<?> from) 
        throws InstantiationException, IllegalAccessException, 
        ClassNotFoundException, SQLException, IOException, SecurityException, NoSuchMethodException, InvocationTargetException 
    {
        Collection<Object> toCollection = getToCollectionCloned(from);
        
        if (toCollection == null) {
            toCollection = this.createToCollection(from);
            clonedMap.put(from, toCollection);
            Collection fromCollection = from;
            hibernateInitialize(fromCollection);
            // recursively populate member objects.
            for (Object fromMember : fromCollection) {
                Object toMember = replicate(fromMember);
                toCollection.add(toMember);
            }
        }
        // Return null if the set is empty
        return toCollection;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<Object> getToCollectionCloned(Collection<?> from)
    {
        return (Collection<Object>)clonedMap.get(from);
    }
    
    private Map<?,?> replicateMap(Map<?,?> from) 
        throws InstantiationException, IllegalAccessException, 
        ClassNotFoundException, SQLException, IOException, SecurityException, NoSuchMethodException, InvocationTargetException
    {
        Map<Object,Object> toMap = getToMapCloned(from);
        
        if (toMap != null) {
            // Already transformed.
            return toMap;
        }
        toMap = this.createToMap(from);
        clonedMap.put(from, toMap);
        Map fromMap = from;
        hibernateInitialize(fromMap);
        // recursively populate member objects.
        for (Iterator itr=fromMap.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry fromEntry = (Map.Entry)itr.next();
            Object fromKey = fromEntry.getKey();
            Object fromValue = fromEntry.getValue();
            Object toKey = replicate(fromKey);
            Object toValue = replicate(fromValue);
            toMap.put(toKey, toValue);
        }
        return toMap;
    }
    
    @SuppressWarnings("unchecked")
    private Map<Object, Object> getToMapCloned(Map<?, ?> from)
    {
        return (Map<Object,Object>)clonedMap.get(from);
    }

    private void populate(Object fromMember, Object toMember) {
        BeanPopulator.factory.newBeanPopulator(fromMember, toMember)
                .initBeanPopulatable(beanPopulatable)
                .initBeanSourceHandler(beanSourceHandler)
                .initDebug(debug)
                .initDetailedBeanPopulatable(detailedBeanPopulatable)
                .initReaderMethodFinder(readerMethodFinder)
                .initSetterMethodCollector(setterMethodCollector)
                .initTransformer(this)
                .populate();
    }
    
    private Blob replicateBlob(Blob fromBlob) {
        byte[] byteArray = BlobUtils.inst.toByteArray(fromBlob);
        return hibernateCreateBlob(byteArray);
    }

    /** Returns a replicated comparator of the given sorted map, or null if there is no comparator. */
    @SuppressWarnings("unchecked")
    private Comparator<Object> createToComparator(SortedMap fromSortedMap) 
        throws InstantiationException, IllegalAccessException, NoSuchMethodException 
    {
        Comparator fromComparator = fromSortedMap.comparator();
        Comparator toComparator = fromComparator == null 
                                ? null 
                                : this.replicateApplicationObject(fromComparator, Comparator.class)
                                ;
        return toComparator;
    }

    /** Returns a replicated comparator of the given sorted set, or null if there is no comparator. */
    private Comparator createToComparator(SortedSet fromSortedSet) 
        throws InstantiationException, IllegalAccessException, NoSuchMethodException 
    {
        Comparator fromComparator = fromSortedSet.comparator();
        Comparator toComparator = fromComparator == null 
                                ? null 
                                : this.replicateApplicationObject(fromComparator, Comparator.class)
                                ;
        return toComparator;
    }
    
    // Use the same comparator or otherwise ClassCastException.
    // http://sourceforge.net/forum/forum.php?thread_id=1462253&forum_id=470286
    // Thanks to Jam Flava for finding this bug.
    @SuppressWarnings("unchecked")
    private Collection<Object> createToCollection(Collection<?> from) 
        throws InstantiationException, IllegalAccessException, SecurityException, 
                NoSuchMethodException, InvocationTargetException
    {
        Class fromClass = from.getClass();
        
        if (isJavaPackage(fromClass)) {
            if (from instanceof SortedSet) {
                SortedSet fromSortedSet = (SortedSet)from;
                Comparator toComparator = createToComparator(fromSortedSet);
                
                if (toComparator != null)
                    return this.createToSortedSetWithComparator(fromSortedSet, toComparator);
            }
            return createToInstanceAsCollection(from);
        }
        if (from instanceof SortedSet) {
            SortedSet fromSortedSet = (SortedSet)from;
            Comparator toComparator = createToComparator(fromSortedSet);
            
            if (isHibernatePackage(fromClass))
                return new TreeSet<Object>(toComparator);
            Constructor constructor = fromClass.getConstructor(Comparator.class);
            Object[] initargs = {toComparator};
            return (Collection<Object>) constructor.newInstance(initargs);
        }
        if (from instanceof Set) {
            if (isHibernatePackage(fromClass))
                return new HashSet<Object>();
            return (Collection<Object>)fromClass.newInstance();
        }
        if (from instanceof List) {
            if (isHibernatePackage(fromClass))
                return new ArrayList<Object>(from.size());
            return (Collection<Object>)fromClass.newInstance();
        }
        // don't know what collection, so use list
        log.warn("Don't know what collection object:" + fromClass + ", so assume List.");
        return new ArrayList<Object>(from.size());
    }
    
    @SuppressWarnings("unchecked")
    private Collection<Object> createToInstanceAsCollection(Collection<?> from) 
        throws InstantiationException, IllegalAccessException, NoSuchMethodException
    {
        return (Collection<Object>)createToInstance(from);
    }

    @SuppressWarnings("unchecked")
    private SortedMap<Object,Object> createToSortedMapWithComparator(SortedMap from, Comparator comparator) 
        throws NoSuchMethodException, SecurityException
    {
        return (SortedMap<Object,Object>)createToInstanceWithComparator(from, comparator);
    }

    @SuppressWarnings("unchecked")
    private SortedSet<Object> createToSortedSetWithComparator(SortedSet from, Comparator comparator) 
        throws NoSuchMethodException, SecurityException
    {
        return (SortedSet<Object>)createToInstanceWithComparator(from, comparator);
    }
    
    private Map<Object,Object> createToMap(Map<?,?> from) 
        throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException 
    {
        Class fromClass = from.getClass();
        
        if (isJavaPackage(fromClass)) {
            if (from instanceof SortedMap) {
                SortedMap fromSortedMap = (SortedMap<?,?>)from;
                Comparator<Object> toComparator = createToComparator(fromSortedMap);
                
                if (toComparator != null)
                    return this.createToSortedMapWithComparator(fromSortedMap, toComparator);
            }
            return createToInstanceAsMap(from);
        }
        if (from instanceof SortedMap) {
            SortedMap fromSortedMap = (SortedMap<?,?>)from;
            Comparator<Object> toComparator = createToComparator(fromSortedMap);
            return new TreeMap<Object,Object>(toComparator);
        }
        return new HashMap<Object,Object>();
    }
    
    @SuppressWarnings("unchecked")
    private Map<Object, Object> createToInstanceAsMap(Map<?, ?> from) 
        throws InstantiationException, IllegalAccessException, NoSuchMethodException
    {
        return (Map<Object,Object>)createToInstance(from);
    }
    
    /** Returns true if the given class has a package name that starts with "org.hibernate."; false otherwise. */
    private boolean isHibernatePackage(Class c) {
        Package p = c.getPackage();
        return p != null && p.getName().startsWith("org.hibernate.");
    }
    /** 
     * Creates a non cglib enhanced instance of the given object.
     */
    private Object createToInstance(Object from) 
        throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException 
    {
        return createToInstance(from.getClass());
    }
    
    /** 
     * Creates a non cglib enhanced instance of the given class, which could itself be the class of a cglib enhanced object.
     */
    @SuppressWarnings("unchecked")
    private <T> T createToInstance(Class<T> fromClass) 
        throws InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException 
    {
        Class toClass = fromClass;
        
        if (Enhancer.isEnhanced(toClass)) {
            // figure out the pre-enhanced class
            toClass = toClass.getSuperclass();
        }
        return (T)newInstanceAsPrivileged(toClass);
    }
    
    private Object createToInstanceWithComparator(Object from, Comparator comparator)
        throws SecurityException, NoSuchMethodException
    {
        return createToInstanceWithComparator(from.getClass(), comparator);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T createToInstanceWithComparator(Class<T> fromClass, Comparator comparator) 
        throws SecurityException, NoSuchMethodException
    {
        Class toClass = fromClass;
        
        if (Enhancer.isEnhanced(toClass)) {
            // figure out the pre-enhanced class
            toClass = toClass.getSuperclass();
        }
        return (T)newInstanceWithComparatorAsPrivileged(toClass, comparator);
    }

    /** 
     * Creates a new instance of the given class via the no-arg constructor,
     * invoking the constructor as a privileged action if it is protected or private.
     * 
     * @param c given class
     * @return a new instance of the given class via the no-arg constructor
     */ 
    private Object newInstanceAsPrivileged(Class c) 
        throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        final Constructor constructor = c.getDeclaredConstructor();
        
        if (Modifier.isPublic(constructor.getModifiers()))
            return c.newInstance();
        return AccessController.doPrivileged(
            new PrivilegedAction<Object>() {
                public Object run() {
                    constructor.setAccessible(true);
                    try {
                        return  constructor.newInstance();
                    } catch (IllegalArgumentException e) {
                        log.error("", e);
                        throw new BeanlibHibernateException(e);
                    } catch (IllegalAccessException e) {
                        log.error("", e);
                        throw new BeanlibHibernateException(e);
                    } catch (InvocationTargetException e) {
                        log.error("", e);
                        throw new BeanlibHibernateException(e);
                    } catch (InstantiationException e) {
                        log.error("", e);
                        throw new BeanlibHibernateException(e);
                    }
                }
        });
    }

    private Object newInstanceWithComparatorAsPrivileged(Class c, final Comparator comparator) 
        throws SecurityException, NoSuchMethodException
    {
        final Constructor constructor = c.getDeclaredConstructor(Comparator.class);
        
        if (Modifier.isPublic(constructor.getModifiers())) {
            try {
                return constructor.newInstance(comparator);
            } catch (IllegalArgumentException e) {
                log.error("", e);
                throw new BeanlibHibernateException(e);
            } catch (InstantiationException e) {
                log.error("", e);
                throw new BeanlibHibernateException(e);
            } catch (IllegalAccessException e) {
                log.error("", e);
                throw new BeanlibHibernateException(e);
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException(); 
                log.error("", t);
                throw new BeanlibHibernateException(t);
            }
        }
        return AccessController.doPrivileged(
            new PrivilegedAction<Object>() {
                public Object run() {
                    constructor.setAccessible(true);
                    try {
                        return  constructor.newInstance(comparator);
                    } catch (IllegalArgumentException e) {
                        log.error("", e);
                        throw new BeanlibHibernateException(e);
                    } catch (IllegalAccessException e) {
                        log.error("", e);
                        throw new BeanlibHibernateException(e);
                    } catch (InvocationTargetException e) {
                        Throwable t = e.getTargetException(); 
                        log.error("", t);
                        throw new BeanlibHibernateException(t);
                    } catch (InstantiationException e) {
                        log.error("", e);
                        throw new BeanlibHibernateException(e);
                    }
                }
        });
    }

    public final BeanPopulatable getBeanPopulatable() {
        return beanPopulatable;
    }
    
    public final HibernateBeanTransformable initCustomTransformer(CustomHibernateBeanTransformable customTransformer) {
        this.customTransformer = customTransformer;
        return this;
    }
    
    public final HibernateBeanTransformable initBeanPopulatable(BeanPopulatable beanPopulatable) {
        this.beanPopulatable = beanPopulatable;
        return this;
    }
    public final BeanSourceHandler getBeanSourceHandler() {
        return beanSourceHandler;
    }
    public final HibernateBeanTransformable initBeanSourceHandler(BeanSourceHandler beanSourceHandler) {
        this.beanSourceHandler = beanSourceHandler;
        return this;
    }
    public final boolean isDebug() {
        return debug;
    }
    public final HibernateBeanTransformable initDebug(boolean debug) {
        this.debug = debug;
        return this;
    }
    public final DetailedBeanPopulatable getDetailedBeanPopulatable() {
        return detailedBeanPopulatable;
    }
    public final HibernateBeanTransformable initDetailedBeanPopulatable(DetailedBeanPopulatable detailedBeanPopulatable) 
    {
        this.detailedBeanPopulatable = detailedBeanPopulatable;
        return this;
    }
    public final BeanMethodFinder getReaderMethodFinder() {
        return readerMethodFinder;
    }
    public final HibernateBeanTransformable initReaderMethodFinder(BeanMethodFinder readerMethodFinder) {
        this.readerMethodFinder = readerMethodFinder;
        return this;
    }
    public final BeanMethodCollector getSetterMethodCollector() {
        return setterMethodCollector;
    }
    public final HibernateBeanTransformable initSetterMethodCollector(BeanMethodCollector setterMethodCollector) {
        this.setterMethodCollector = setterMethodCollector;
        return this;
    }
    public Map getClonedMap() {
        return clonedMap;
    }
    public CustomHibernateBeanTransformable getCustomTransformer() {
        return customTransformer;
    }
}
