/*
 * Copyright 2006 The Apache Software Foundation.
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

package net.sf.beanlib.util.concurrent;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * A {@link java.util.concurrent.ConcurrentLinkedQueue ConcurrentLinkedQueue} 
 * that additionally supports operations
 * that wait for the queue to become non-empty when retrieving an element.
 * <p>
 * Note there is currently no such class in Java 6.
 * <p>
 * In contrast to {@link java.util.concurrent.LinkedBlockingQueue LinkedBlockingQueue}
 * which is always bounded, a ConcurrentLinkedBlockingQueue is unbounded.
 * 
 * @author Hanson Char
 * @param <E> the type of elements held in this collection
 */
public class ConcurrentLinkedBlockingQueue<E> extends AbstractQueue<E>
        implements java.io.Serializable 
{
    private static final long serialVersionUID = -191767472599610115L;

    private static class ThreadMarker {
        final Thread thread;
        // assumed parked until found otherwise.
        volatile boolean parked = true;
        
        ThreadMarker(Thread thread)
        {
            this.thread = thread;
        }
    }
    
    private final ConcurrentLinkedQueue<ThreadMarker> parkq = new ConcurrentLinkedQueue<ThreadMarker>();
    
    private final ConcurrentLinkedQueue<E> q;

    public ConcurrentLinkedBlockingQueue() {
        q = new ConcurrentLinkedQueue<E>();
    }

    public ConcurrentLinkedBlockingQueue(Collection<? extends E> c) {
        q = new ConcurrentLinkedQueue<E>(c);
    }

    @Override
    public Iterator<E> iterator() {
        return q.iterator();
    }

    @Override
    public int size() {
        return q.size();
    }

    public boolean offer(E e) {
        boolean b = q.offer(e);
        
        for (;;)
        {
            ThreadMarker marker = parkq.poll();
            
            if (marker == null)
                return b;
            if (marker.parked) 
            {
                LockSupport.unpark(marker.thread);
                return b;
            }
        }
    }

    public E peek() {
        return q.peek();
    }

    public E poll() {
        return q.poll();
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary until
     * an element becomes available.
     * 
     * @return the head of this queue
     */
    public E take() 
    {
        for (;;) {
            E e = q.poll();

            if (e != null)
                return e;
            ThreadMarker m = new ThreadMarker(Thread.currentThread()); 
            parkq.offer(m);
            // check again in case there is data race
            e = q.poll();

            if (e != null) 
            {
                // data race indeed
                m.parked = false;
                return e;
            }
            LockSupport.park();
        }
    }
    
    /**
     * Retrieves and removes the head of this queue, waiting up to the specified
     * wait time if necessary for an element to become available.
     * 
     * @param timeout
     *            how long to wait before giving up, in units of <tt>unit</tt>
     * @param unit
     *            a <tt>TimeUnit</tt> determining how to interpret the
     *            <tt>timeout</tt> parameter
     * @return the head of this queue, or <tt>null</tt> if the specified
     *         waiting time elapses before an element is available
     */
    public E poll(long timeout, TimeUnit unit) 
    {
        if (timeout < 0)
            return take();  // treat -ve timeout same as to wait forever
        long t0=0;
        
        for (;;) {
            E e = q.poll();

            if (e != null)
                return e;
            if (t0 > 0 && System.nanoTime() >= (t0 + unit.toNanos(timeout)))
                return null;    // time out
            ThreadMarker m = new ThreadMarker(Thread.currentThread());
            
            parkq.offer(m);
            e = q.poll();

            if (e != null) {
                m.parked = false;
                return e;
            }
            t0 = System.nanoTime();
            LockSupport.parkNanos(unit.toNanos(timeout));
        }
    }
}